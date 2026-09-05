package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The outward normal of a perpendicular baseboard return; UP means no return. */
final class BaseboardReturn {
    static final EnumProperty<Direction> FACING = EnumProperty.create("baseboard_return", Direction.class,
            Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);

    static Direction find(BlockGetter level, BlockPos pos, Direction facing) {
        if (facing.getAxis().isVertical()) return Direction.UP;
        BlockState front = level.getBlockState(pos.relative(facing));
        if (front.getBlock() instanceof BaseboardBlock) {
            Direction other = front.getValue(BaseboardBlock.FACING);
            if (other.getAxis() != facing.getAxis()) return other;
        }
        return Direction.UP;
    }

    static VoxelShape shape(Direction facing) {
        return switch (facing) {
            case SOUTH -> Block.box(0, 0, 0, 16, 4, 2);
            case NORTH -> Block.box(0, 0, 14, 16, 4, 16);
            case EAST -> Block.box(0, 0, 0, 2, 4, 16);
            case WEST -> Block.box(14, 0, 0, 16, 4, 16);
            default -> Shapes.empty();
        };
    }
}
