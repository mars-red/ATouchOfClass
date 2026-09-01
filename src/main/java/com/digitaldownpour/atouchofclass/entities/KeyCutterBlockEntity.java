package com.digitaldownpour.atouchofclass.entities;

import com.digitaldownpour.atouchofclass.KeyCutterMenu;
import com.digitaldownpour.atouchofclass.ModBlocks;
import com.digitaldownpour.atouchofclass.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class KeyCutterBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler items = new ItemStackHandler(2){
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
            }
        }
    };

    public KeyCutterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModEntities.KEY_CUTTER_BE.get(), pos, blockState);
    }

    public ItemStackHandler getItems()
    {
        return items;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new KeyCutterMenu(id, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.atouchofclass.key_cutter");
    }

    /*
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }

     */
}
