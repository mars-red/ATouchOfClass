package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Upright plaster column pieces; placement never changes their orientation. */
public class PlasterColumnBlock extends Block {

    public enum Variant implements StringRepresentable {
        SQUARE_COLUMN("square_column", false, false, false),
        SQUARE_BASE("square_base", false, true, false),
        SQUARE_CAPITAL("square_capital", false, true, true),
        ROUND_COLUMN("round_column", true, false, false),
        ROUND_BASE("round_base", true, true, false),
        ROUND_CAPITAL("round_capital", true, true, true);

        public static final StringRepresentable.EnumCodec<Variant> CODEC = StringRepresentable.fromEnum(Variant::values);
        private final String name;
        private final boolean round;
        private final boolean end;
        private final boolean inverted;

        Variant(String name, boolean round, boolean end, boolean inverted) {
            this.name = name;
            this.round = round;
            this.end = end;
            this.inverted = inverted;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    // Non-overlapping strips form an eight-pixel-wide stepped circle.
    private static final double[][] ROUND_SHAFT = {
            {6, 4, 10, 5},
            {5, 5, 11, 6},
            {4, 6, 12, 10},
            {5, 10, 11, 11},
            {6, 11, 10, 12}
    };
    private static final double[][] SQUARE_SHAFT = {{4, 4, 12, 12}};

    private final Variant variant;
    private final VoxelShape shape;

    public PlasterColumnBlock(Properties properties, Variant variant) {
        super(properties);
        this.variant = variant;
        VoxelShape combined = Shapes.empty();
        if (variant.end) {
            combined = Shapes.or(combined, piece(0, 0, 0, 16, 3, 16, variant.inverted));
            combined = Shapes.or(combined, piece(2, 3, 2, 14, 6, 14, variant.inverted));
            combined = Shapes.or(combined, piece(3, 6, 3, 13, 8, 13, variant.inverted));
        }
        for (double[] strip : variant.round ? ROUND_SHAFT : SQUARE_SHAFT) {
            combined = Shapes.or(combined, piece(strip[0], variant.end ? 8 : 0, strip[1],
                    strip[2], 16, strip[3], variant.inverted));
        }
        shape = combined.optimize();
    }

    private static VoxelShape piece(double x0, double y0, double z0, double x1, double y1, double z1,
                                    boolean inverted) {
        return Block.box(x0, inverted ? 16 - y1 : y0, z0, x1, inverted ? 16 - y0 : y1, z1);
    }


    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return shape;
    }
}
