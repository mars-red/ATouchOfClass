package com.digitaldownpour.atouchofclass.blocks;

import com.digitaldownpour.atouchofclass.entities.CabinetBlockEntity;
import com.digitaldownpour.atouchofclass.CabinetAttachments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.Direction;

/** A full-height cabinet with a one-unit recess at its player-facing front. */
public class CabinetBlock extends HorizontalFurnitureBlock implements EntityBlock {

    private static final double[][] NORTH_FACING_BOXES = {
            {0, 0, 1, 16, 16, 16}
    };

    public CabinetBlock(Properties properties) {
        super(properties.randomTicks(), NORTH_FACING_BOXES);
    }


    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var drops = new ArrayList<>(super.getDrops(state, params));
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CabinetBlockEntity cabinet
                && !cabinet.getAttachment().isAir()) {
            drops.addAll(cabinet.getAttachment().getDrops(params));
        }
        return drops;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        CabinetAttachments.weather(level, pos, random);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
        CabinetAttachments.refreshPairs(level, pos);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CabinetBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel && level.getBlockEntity(pos) instanceof CabinetBlockEntity cabinet) {
            player.openMenu(cabinet);
            player.awardStat(Stats.OPEN_CHEST);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof CabinetBlockEntity cabinet) {
            cabinet.recheckOpen();
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }
}
