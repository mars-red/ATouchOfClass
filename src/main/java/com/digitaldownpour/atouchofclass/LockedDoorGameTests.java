package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.TallDoorBlock;
import com.digitaldownpour.atouchofclass.components.KeyableInfo;
import com.digitaldownpour.atouchofclass.entities.TallDoorBlockEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.UUID;
import net.minecraft.network.chat.Component;

public class LockedDoorGameTests {

    public static void rejectedDoorInteractionDoesNotPlaceHeldBlock(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "locked-door-test"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        BlockPos base = helper.absolutePos(new BlockPos(2, 2, 2));
        player.setPos(Vec3.atCenterOf(base.south(2)));

        for (var door : new TallDoorBlock[] {ModBlocks.TALL_OAK_DOOR.get(), ModBlocks.TALL_OAK_PATIO_DOOR.get()}) {
            level.setBlockAndUpdate(base.below(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(base, door.defaultBlockState());
            var blockEntity = GameTestChecks.requireType(level.getBlockEntity(base), TallDoorBlockEntity.class);
            blockEntity.setKeyableInfo(new KeyableInfo("locked-code", "Locked Door"));

            for (int part = 0; part < 3; part++) {
                BlockPos clickedPos = base.above(part);
                BlockPos placementPos = clickedPos.south();
                level.setBlockAndUpdate(placementPos, Blocks.AIR.defaultBlockState());
                var block = new ItemStack(Items.STONE, 2);
                player.setItemInHand(InteractionHand.MAIN_HAND, block);
                var hit = new BlockHitResult(Vec3.atCenterOf(clickedPos), Direction.SOUTH, clickedPos, false);
                var result = player.gameMode.useItemOn(player, level, block, InteractionHand.MAIN_HAND, hit);
                helper.assertTrue(result.consumesAction(), Component.literal("Locked door must consume held-block interactions"));
                helper.assertTrue(level.getBlockState(placementPos).isAir() && block.getCount() == 2, Component.literal("Rejected opening must not place or consume the held block"));
                helper.assertTrue(!level.getBlockState(base).getValue(TallDoorBlock.OPEN), Component.literal("Held block must not open the locked door from any section"));
            }

            var key = new ItemStack(ModItems.DOOR_KEY.get());
            key.set(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE, new KeyableInfo("wrong-code", "Wrong Key"));
            player.setItemInHand(InteractionHand.MAIN_HAND, key);
            var baseHit = new BlockHitResult(Vec3.atCenterOf(base), Direction.SOUTH, base, false);
            player.gameMode.useItemOn(player, level, key, InteractionHand.MAIN_HAND, baseHit);
            helper.assertTrue(!level.getBlockState(base).getValue(TallDoorBlock.OPEN), Component.literal("Wrong key must leave door closed"));
            key.set(ModComponents.KEYABLE_INFO_DATA_COMPONENT_TYPE, new KeyableInfo("locked-code", "Matching Key"));
            player.setItemInHand(InteractionHand.MAIN_HAND, key);
            player.gameMode.useItemOn(player, level, key, InteractionHand.MAIN_HAND, baseHit);
            helper.assertTrue(level.getBlockState(base).getValue(TallDoorBlock.OPEN), Component.literal("Matching key must still open door"));

            blockEntity.setKeyableInfo(new KeyableInfo("", "Unlocked Door"));
            var block = new ItemStack(Items.STONE, 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, block);
            player.gameMode.useItemOn(player, level, block, InteractionHand.MAIN_HAND, baseHit);
            helper.assertTrue(!level.getBlockState(base).getValue(TallDoorBlock.OPEN), Component.literal("Held blocks must still toggle unlocked doors"));
            level.destroyBlock(base, false);
        }
        helper.succeed();
    }
}
