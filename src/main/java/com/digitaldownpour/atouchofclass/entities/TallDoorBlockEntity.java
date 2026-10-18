package com.digitaldownpour.atouchofclass.entities;

import com.digitaldownpour.atouchofclass.ModComponents;
import com.digitaldownpour.atouchofclass.ModEntities;
import com.digitaldownpour.atouchofclass.blocks.TallDoorBlock;
import com.digitaldownpour.atouchofclass.components.KeyableInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        // This runs while the broken section's block entity is still accessible to loot.
        // Legacy saves may store the key only in the bottom section.
        if (level != null && state.getValue(TallDoorBlock.PART) != 0
                && level.getBlockEntity(pos.below(state.getValue(TallDoorBlock.PART))) instanceof TallDoorBlockEntity base) {
            setKeyableInfo(new KeyableInfo(base.getKeyCode(), base.getKeyableName()));
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putString("keyCode", keyCode);
        tag.putString("keyableName", keyableName);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        keyCode = tag.getStringOr("keyCode", "");
        keyableName = tag.getStringOr("keyableName", "");
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (!keyCode.isEmpty() || !keyableName.isEmpty()) {
            builder.set(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE.get(), new KeyableInfo(keyCode, keyableName));
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter input) {
        super.applyImplicitComponents(input);
        KeyableInfo info = input.get(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE.get());
        if (info != null) {
            keyCode = info.keyCode();
            keyableName = info.keyableName();
            syncToOtherParts();
        }
    }
}
