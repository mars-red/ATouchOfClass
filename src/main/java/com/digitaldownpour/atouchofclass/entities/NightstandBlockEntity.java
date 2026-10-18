package com.digitaldownpour.atouchofclass.entities;

import com.digitaldownpour.atouchofclass.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Shared 27-slot storage for all nightstand wood variants. */
public class NightstandBlockEntity extends RandomizableContainerBlockEntity {
    public static final int CONTAINER_SIZE = 27;

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            playSound(level, pos, SoundEvents.CHEST_OPEN);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            playSound(level, pos, SoundEvents.CHEST_CLOSE);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state,
                                          int oldCount, int newCount) {
        }

        @Override
        public boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof ChestMenu chestMenu
                    && chestMenu.getContainer() == NightstandBlockEntity.this;
        }
    };

    public NightstandBlockEntity(BlockPos pos, BlockState state) {
        super(ModEntities.NIGHTSTAND_BE.get(), pos, state);
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return ChestMenu.threeRows(id, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, items);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        if (!trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, items);
        }
    }

    @Override
    public void startOpen(ContainerUser user) {
        if (level != null && !remove && !user.getLivingEntity().isSpectator()) {
            openersCounter.incrementOpeners(user.getLivingEntity(), level, getBlockPos(), getBlockState(), user.getContainerInteractionRange());
        }
    }

    @Override
    public void stopOpen(ContainerUser user) {
        if (level != null && !remove && !user.getLivingEntity().isSpectator()) {
            openersCounter.decrementOpeners(user.getLivingEntity(), level, getBlockPos(), getBlockState());
        }
    }

    public void recheckOpen() {
        if (level != null && !remove) {
            openersCounter.recheckOpeners(level, getBlockPos(), getBlockState());
        }
    }

    private static void playSound(Level level, BlockPos pos, SoundEvent sound) {
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                sound, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
    }
}
