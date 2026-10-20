"""Keep solid steel surfaces opaque without changing any RGB texture detail."""
from pathlib import Path

from PIL import Image

TEXTURES = Path(__file__).absolute().parents[1] / 'src/main/resources/assets/atouchofclass/textures/block'
NAMES = (
    'brushed_stainless_steel',
    'brushed_stainless_steel_door',
    'brushed_stainless_steel_door_left_edge',
    'brushed_stainless_steel_door_right_edge',
    'elevator_button_interior',
    'elevator_call_button_inactive',
    'elevator_call_button_locked',
    'elevator_call_button_unlocked',
)


def main():
    for name in NAMES:
        path = TEXTURES / (name + '.png')
        with Image.open(path) as source:
            image = source.convert('RGBA')
        rgb = image.convert('RGB').tobytes()
        if image.getchannel('A').getextrema() != (255, 255):
            image.putalpha(255)
            image.save(path)
        with Image.open(path) as saved:
            assert saved.convert('RGBA').getchannel('A').getextrema() == (255, 255)
            assert saved.convert('RGB').tobytes() == rgb
    print(f'Validated {len(NAMES)} opaque metal textures; RGB pixels preserved.')


if __name__ == '__main__':
    main()
