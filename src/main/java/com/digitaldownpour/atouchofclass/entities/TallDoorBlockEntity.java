package com.digitaldownpour.atouchofclass.entities;

import com.digitaldownpour.atouchofclass.ModComponents;
import com.digitaldownpour.atouchofclass.ModEntities;
import com.digitaldownpour.atouchofclass.blocks.TallDoorBlock;
import com.digitaldownpour.atouchofclass.components.KeyableInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TallDoorBlockEntity extends BlockEntity {
    private String keyCode = "";
    private String keyableName = "";

    public TallDoorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModEntities.TALL_DOOR_BE.get(), pos, blockState);
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
        syncToOtherParts();
    }

    /**
     * Copy the key code/name to the other sections of this door, so whichever section a player breaks still carries
     * the correct data when the item is dropped (loot table copy_components reads the block
     * entity at the exact position that was broken).
     */
    private void syncToOtherParts() {
        if (level == null) return;
        BlockPos basePos = worldPosition.below(getBlockState().getValue(TallDoorBlock.PART));
        for (int i = 0; i < 3; i++) {
            BlockPos pos = basePos.above(i);
            BlockState state = level.getBlockState(pos);
            if (state.is(getBlockState().getBlock()) && state.getValue(TallDoorBlock.PART) == i
                    && level.getBlockEntity(pos) instanceof TallDoorBlockEntity other && other != this) {
                other.keyCode = this.keyCode;
                other.keyableName = this.keyableName;
                other.setChanged();
            }
        }
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

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (!keyCode.isEmpty() || !keyableName.isEmpty()) {
            builder.set(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE.get(), new KeyableInfo(keyCode, keyableName));
        }
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        KeyableInfo info = input.get(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE.get());
        if (info != null) {
            keyCode = info.keyCode();
            keyableName = info.keyableName();
            syncToOtherParts();
        }
    }
}
