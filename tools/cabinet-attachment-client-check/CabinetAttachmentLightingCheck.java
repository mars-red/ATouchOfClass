package com.digitaldownpour.atouchofclass.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

/** Compare actual vertex lighting with the normal block renderer in a shaded corner. */
final class CabinetAttachmentLightingCheck {
    static boolean verify(BlockState state, BlockStateModel model, List<BlockStateModelPart> parts) {
        var owner = new BlockPos(0, 63, 0); // Attachment crosses a section boundary.
        var pos = owner.above();
        var scene = corner(pos, state);
        var attachedScene = corner(pos, Blocks.AIR.defaultBlockState());
        var colors = Minecraft.getInstance().getBlockColors();
        List<Integer> smoothOutput = null, flatOutput = null;
        for (boolean smooth : new boolean[] {false, true}) {
            var expected = new ArrayList<Integer>();
            var actual = new ArrayList<Integer>();
            new ModelBlockRenderer(smooth, false, colors).tesselateBlock(capture(expected), 0, 16, 0,
                    scene, pos, state, model, state.getSeed(pos));
            CabinetAttachmentRenderer.tesselate(new ModelBlockRenderer(smooth, false, colors), capture(actual),
                    attachedScene, owner, state, CabinetAttachmentRenderer.snapshot(parts));
            if (!expected.equals(actual)) throw new AssertionError("Attached lighting differs from placed block: " + state + " smooth=" + smooth);
            if (actual.isEmpty()) throw new AssertionError("No lit geometry " + state);
            if (smooth) smoothOutput = actual; else flatOutput = actual;
        }
        return !smoothOutput.equals(flatOutput);
    }

    private static BlockAndTintGetter corner(BlockPos pos, BlockState state) {
        return new BlockAndTintGetter() {
            public CardinalLighting cardinalLighting() { return CardinalLighting.DEFAULT; }
            public LevelLightEngine getLightEngine() { return LevelLightEngine.EMPTY; }
            public int getBrightness(net.minecraft.world.level.LightLayer layer, BlockPos p) {
                return Math.floorMod(p.getX() * 3 + p.getY() + p.getZ() * 5 + (layer == net.minecraft.world.level.LightLayer.SKY ? 7 : 0), 16);
            }
            public int getBlockTint(BlockPos p, ColorResolver resolver) { return -1; }
            public BlockEntity getBlockEntity(BlockPos p) { return null; }
            public BlockState getBlockState(BlockPos p) {
                if (p.equals(pos)) return state;
                return p.getY() < 64 || p.getX() < 0 || p.getZ() < 0
                        ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState();
            }
            public FluidState getFluidState(BlockPos p) { return getBlockState(p).getFluidState(); }
            public int getHeight() { return 256; }
            public int getMinY() { return 0; }
        };
    }

    private static BlockQuadOutput capture(List<Integer> output) {
        return (x, y, z, quad, lighting) -> {
            output.add(Float.floatToIntBits(x));
            output.add(Float.floatToIntBits(y));
            output.add(Float.floatToIntBits(z));
            for (int i = 0; i < 4; i++) {
                output.add(lighting.getColor(i));
                output.add(lighting.getLightCoords(i));
            }
        };
    }
}
