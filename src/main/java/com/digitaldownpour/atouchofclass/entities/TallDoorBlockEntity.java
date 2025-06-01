package com.digitaldownpour.atouchofclass.entities;

import com.digitaldownpour.atouchofclass.components.KeyableInfo;
import com.digitaldownpour.atouchofclass.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TallDoorBlockEntity extends BlockEntity {
    private String keyCode = "";
    private String keyableName = "";

    public TallDoorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlocks.TALL_DOOR_BE.get(), pos, blockState);
    }

    public String getKeyCode() {
        return keyCode;
    }

    public String getKeyableName() {
        return keyableName;
    }

    public void setKeyableInfo(KeyableInfo v) {
        keyCode = v.keyCode();
        keyableName = v.keyableName();
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("keyCode", keyCode);
        tag.putString("keyableName", keyableName);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        keyCode = tag.getString("keyCode");
        keyableName = tag.getString("keyableName");
    }
}
