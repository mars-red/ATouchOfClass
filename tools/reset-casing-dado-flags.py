"""Remove casing dado flags in Java Edition region palettes. Python 3.9+, no dependencies.
Stop the server/game first. Default is a dry run; --apply backs up changed regions.
"""
import argparse
import gzip
import os
from pathlib import Path
import shutil
import struct
import tempfile
import time
import zlib

FLAGS = {'dado_top', 'dado_bottom', 'dado_corner_top', 'dado_corner_bottom'}


class NBT:
    def __init__(self, data):
        self.data, self.pos, self.edits, self.entries = data, 0, [], 0
        self.compound_spans = {}
        self.casing_seen = 0

    def take(self, n):
        if n < 0 or self.pos + n > len(self.data):
            raise ValueError('Truncated or invalid NBT')
        start = self.pos
        self.pos += n
        return self.data[start:self.pos]

    def number(self, fmt):
        return struct.unpack(fmt, self.take(struct.calcsize(fmt)))[0]

    def string(self):
        start = self.pos
        value = self.take(self.number('>H')).decode('utf-8', errors='replace')
        return value, start, self.pos

    def payload(self, tag, depth=0):
        if depth > 512:
            raise ValueError('NBT nesting too deep')
        sizes = {1: 1, 2: 2, 3: 4, 4: 8, 5: 4, 6: 8}
        if tag in sizes:
            self.take(sizes[tag])
        elif tag in (7, 11, 12):
            self.take(self.number('>i') * {7: 1, 11: 4, 12: 8}[tag])
        elif tag == 8:
            return self.string()
        elif tag == 9:
            child, count = self.number('>B'), self.number('>i')
            if count < 0:
                raise ValueError('Negative list length')
            for _ in range(count):
                self.payload(child, depth + 1)
        elif tag == 10:
            fields = {}
            spans = {}
            while True:
                field_start = self.pos
                child = self.number('>B')
                if child == 0:
                    break
                name = self.string()[0]
                fields[name] = self.payload(child, depth + 1)
                spans[name] = (field_start, self.pos)
            name = fields.get('id', fields.get('Name'))
            props = fields.get('properties', fields.get('Properties'))
            if isinstance(name, tuple) and name[0] == 'atouchofclass:casing' and isinstance(props, dict):
                self.casing_seen += 1
                changed = False
                for key in FLAGS:
                    if key in props:
                        start, end = self.compound_spans[id(props)][key]
                        self.edits.append((start, end, b''))
                        changed = True
                self.entries += changed
            self.compound_spans[id(fields)] = spans
            return fields
        else:
            raise ValueError('Unknown NBT tag %s' % tag)

    def repair(self):
        tag = self.number('>B')
        if tag != 10:
            raise ValueError('Expected compound NBT root')
        self.string()
        self.payload(tag)
        output = self.data
        for start, end, replacement in sorted(self.edits, reverse=True):
            output = output[:start] + replacement + output[end:]
        return output


def repair_region(path):
    original = path.read_bytes()
    if len(original) < 8192 or len(original) % 4096:
        raise ValueError('Invalid region size: %s' % path)
    output = bytearray(original)
    chunks = entries = seen = 0
    for slot in range(1024):
        location = int.from_bytes(original[slot*4:slot*4+4], 'big')
        sector, sectors = location >> 8, location & 255
        if not sector:
            continue
        start = sector * 4096
        if sector < 2 or not sectors or start + sectors*4096 > len(original):
            raise ValueError('Invalid chunk location in %s' % path)
        length = struct.unpack_from('>I', original, start)[0]
        if length < 1 or length + 4 > sectors*4096:
            raise ValueError('Invalid chunk length in %s' % path)
        kind = original[start+4]
        if kind & 128:
            raise ValueError('External chunk encountered in %s; no files written. External chunks are not supported.' % path)
        payload = original[start+5:start+4+length]
        if kind == 1:
            raw = gzip.decompress(payload)
        elif kind == 2:
            raw = zlib.decompress(payload)
        elif kind == 3:
            raw = payload
        else:
            raise ValueError('Unsupported compression %s in %s' % (kind, path))
        nbt = NBT(raw)
        repaired = nbt.repair()
        seen += nbt.casing_seen
        if not nbt.edits:
            continue
        packed = zlib.compress(repaired)
        record = struct.pack('>I', len(packed)+1) + b'\x02' + packed
        needed = (len(record)+4095)//4096
        offset = len(output)//4096
        if needed > 255 or offset > 0xffffff:
            raise ValueError('Repaired chunk exceeds region limits')
        output.extend(record + bytes(needed*4096-len(record)))
        output[slot*4:slot*4+4] = ((offset<<8)|needed).to_bytes(4, 'big')
        chunks += 1
        entries += nbt.entries
    return bytes(output), chunks, entries, seen


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('world', type=Path, help='World folder containing level.dat')
    parser.add_argument('--apply', action='store_true', help='Write repairs with backups; server must be stopped')
    args = parser.parse_args()
    world = args.world.resolve()
    if not (world/'level.dat').is_file():
        parser.error('World folder must contain level.dat')
    # Includes overworld, DIM-1/DIM1, and custom dimensions, but excludes entities/POI.
    regions = sorted(p for p in world.rglob('r.*.*.mca') if p.parent.name == 'region')
    planned = []
    total_seen = 0
    for path in regions:
        data, chunks, entries, seen = repair_region(path)
        total_seen += seen
        if chunks:
            planned.append((path, chunks, entries))
            print('%s: %s chunks, %s casing palette entries' % (path.relative_to(world), chunks, entries))
    print('%s: %s region files need repair (%s scanned).' % ('APPLY' if args.apply else 'DRY RUN', len(planned), len(regions)))
    print('Casing palette entries found: %s (entries can represent multiple placed blocks).' % total_seen)
    if not total_seen:
        print('No casing found. Check that this is the world folder used by the server.')
    if not args.apply or not planned:
        return
    backup = world.parent/(world.name+'.casing-backup-'+time.strftime('%Y%m%d-%H%M%S'))
    backup.mkdir(exist_ok=False)
    for path, _, _ in planned:
        destination = backup/path.relative_to(world)
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(path, destination)
    print('Backups: %s' % backup)
    for path, _, _ in planned:
        data, _, _, _ = repair_region(path)
        temp = None
        try:
            with tempfile.NamedTemporaryFile(dir=path.parent, prefix=path.name+'.repair-', delete=False) as stream:
                temp = stream.name
                stream.write(data)
                stream.flush()
                os.fsync(stream.fileno())
            os.replace(temp, path)
        finally:
            if temp and os.path.exists(temp):
                os.unlink(temp)
    print('Repair complete. Keep the backup until the world has been checked.')


if __name__ == '__main__':
    main()
