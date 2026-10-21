package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Placement-time appearance only; loot must not copy these properties to items. */
public class StochasticBlock extends Block {
    public static final int MAX_VARIANTS = 16;
    public static final IntegerProperty TEXTURE_VARIANT = IntegerProperty.create("texture_variant", 0, MAX_VARIANTS - 1);
    public static final IntegerProperty TEXTURE_ROTATION = IntegerProperty.create("texture_rotation", 0, 3);

    private final int variantCount;
    private final int[] rotations;

    /** Rotations are quarter turns in each face's local UV coordinates. */
    public StochasticBlock(Properties properties, int variantCount, int... rotations) {
        super(properties);
        if (variantCount < 1 || variantCount > MAX_VARIANTS) {
            throw new IllegalArgumentException("Expected 1.." + MAX_VARIANTS + " texture variants");
        }
        this.variantCount = variantCount;
        this.rotations = rotations.length == 0 ? new int[]{0, 1, 2, 3} : rotations.clone();
        for (int rotation : this.rotations) {
            if (rotation < 0 || rotation > 3) throw new IllegalArgumentException("Rotation must be 0..3");
        }
        registerDefaultState(stateDefinition.any().setValue(TEXTURE_VARIANT, 0).setValue(TEXTURE_ROTATION, 0));
    }

    public BlockState randomAppearance(RandomSource random) {
        return defaultBlockState().setValue(TEXTURE_VARIANT, random.nextInt(variantCount))
                .setValue(TEXTURE_ROTATION, rotations[random.nextInt(rotations.length)]);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // The server owns the roll; its normal block update corrects client prediction.
        return context.getLevel().isClientSide() ? defaultBlockState() : randomAppearance(context.getLevel().getRandom());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TEXTURE_VARIANT, TEXTURE_ROTATION);
    }
}
