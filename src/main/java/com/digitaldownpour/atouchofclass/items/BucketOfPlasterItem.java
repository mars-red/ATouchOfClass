package com.digitaldownpour.atouchofclass.items;

import com.digitaldownpour.atouchofclass.components.WallOverlayData;
import com.digitaldownpour.atouchofclass.components.WallOverlaySavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;

public class BucketOfPlasterItem extends BlockOverlayItemBase {
    public static final int APPLICATIONS = 32;

    private static final ResourceLocation PLASTER_OVERLAY = ResourceLocation.fromNamespaceAndPath(
            "minecraft",
            "block/white_concrete"
    );

    public BucketOfPlasterItem(Properties properties) {
        super(properties, PLASTER_OVERLAY);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return super.useOn(context);
        }

        ServerLevel level = (ServerLevel) context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        WallOverlayData overlay = WallOverlaySavedData.get(level)
                .getChunkSnapshot(new ChunkPos(pos))
                .get(pos);

        if (overlay != null && PLASTER_OVERLAY.equals(overlay.getFace(face))) {
            return InteractionResult.SUCCESS_SERVER;
        }

        InteractionResult result = super.useOn(context);
        if (result.consumesAction()) {
            consumeApplication(context);
        }
        return result;
    }

    @Override
    public ItemStack getCraftingRemainder(ItemStack stack) {
        if (isLastApplication(stack)) {
            return new ItemStack(Items.BUCKET);
        }

        ItemStack remainder = stack.copyWithCount(1);
        remainder.setDamageValue(stack.getDamageValue() + 1);
        return remainder;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return false;
    }

    private static void consumeApplication(UseOnContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player)
                || player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL) {
            return;
        }

        ItemStack stack = context.getItemInHand();
        if (isLastApplication(stack)) {
            player.setItemInHand(context.getHand(), new ItemStack(Items.BUCKET));
        } else {
            stack.setDamageValue(stack.getDamageValue() + 1);
        }
    }

    private static boolean isLastApplication(ItemStack stack) {
        return stack.getDamageValue() >= stack.getMaxDamage() - 1;
    }
}
