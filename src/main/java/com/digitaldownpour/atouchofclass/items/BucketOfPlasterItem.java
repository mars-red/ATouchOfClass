package com.digitaldownpour.atouchofclass.items;

import com.digitaldownpour.atouchofclass.components.WallOverlayData;
import com.digitaldownpour.atouchofclass.components.WallOverlaySavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;

public class BucketOfPlasterItem extends BlockOverlayItemBase {
    public static final int APPLICATIONS = 32;

    private static final Identifier PLASTER_OVERLAY = Identifier.fromNamespaceAndPath(
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
                .getChunkSnapshot(ChunkPos.containing(pos))
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
    public ItemStackTemplate getCraftingRemainder(ItemInstance stack) {
        int damage = stack.getOrDefault(DataComponents.DAMAGE, 0);
        if (damage >= stack.getOrDefault(DataComponents.MAX_DAMAGE, APPLICATIONS) - 1) {
            return new ItemStackTemplate(Items.BUCKET);
        }

        DataComponentPatch.Builder patch = DataComponentPatch.builder();
        DataComponentPatch original = stack instanceof ItemStack itemStack ? itemStack.getComponentsPatch()
                : stack instanceof ItemStackTemplate template ? template.components() : DataComponentPatch.EMPTY;
        var components = original.split();
        patch.set(components.added());
        components.removed().forEach(patch::remove);
        patch.set(DataComponents.DAMAGE, damage + 1);
        return new ItemStackTemplate(this, patch.build());
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
