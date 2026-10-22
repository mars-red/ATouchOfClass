package com.digitaldownpour.atouchofclass.items;

import com.digitaldownpour.atouchofclass.blocks.FlowerBunchBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Clicking any part of a planter targets its planting space, never an adjacent trough. */
public class FlowerBunchItem extends BlockItem {
    public FlowerBunchItem(Block block, Properties properties) { super(block, properties); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var pos = context.getClickedPos();
        if (context.getLevel().getBlockState(pos).is(FlowerBunchBlock.PLANTERS)) {
            var hit = new BlockHitResult(Vec3.atBottomCenterOf(pos.above()), Direction.UP, pos, false);
            return super.useOn(new UseOnContext(context.getLevel(), context.getPlayer(), context.getHand(), context.getItemInHand(), hit));
        }
        return super.useOn(context);
    }
}
