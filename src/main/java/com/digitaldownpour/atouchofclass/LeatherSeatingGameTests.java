package com.digitaldownpour.atouchofclass;

import com.digitaldownpour.atouchofclass.blocks.LoveSeatBlock;
import com.digitaldownpour.atouchofclass.blocks.SofaBlock;
import com.digitaldownpour.atouchofclass.entities.SeatEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ATouchofClass.MODID)
@PrefixGameTestTemplate(false)
public class LeatherSeatingGameTests {
    private static final BlockPos BASE = new BlockPos(3, 1, 3);

    @GameTest(template = "chandelier_test")
    public static void everyLeatherSeatFacesFrontAndDismountsOntoFloor(GameTestHelper helper) {
        checkFurniture(helper, 0);
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void leatherSeatsFindAnotherExitWhenFrontIsBlocked(GameTestHelper helper) {
        checkFurniture(helper, 1);
        helper.succeed();
    }

    @GameTest(template = "chandelier_test")
    public static void surroundedLeatherSeatsDismountAboveFurniture(GameTestHelper helper) {
        checkFurniture(helper, 2);
        helper.succeed();
    }

    private static void checkFurniture(GameTestHelper helper, int obstacles) {
        var level = helper.getLevel();
        for (Block furniture : List.of(ModBlocks.LEATHER_CHAIR.get(), ModBlocks.LEATHER_LOVE_SEAT.get(), ModBlocks.LEATHER_SOFA.get())) {
            for (Direction front : Direction.Plane.HORIZONTAL) {
                for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
                    helper.setBlock(BASE.offset(x, -1, z), Blocks.STONE);
                    helper.setBlock(BASE.offset(x, 0, z), Blocks.AIR);
                    helper.setBlock(BASE.offset(x, 1, z), Blocks.AIR);
                    helper.setBlock(BASE.offset(x, 2, z), Blocks.AIR);
                }
                var state = furniture.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, front);
                helper.setBlock(BASE, state);
                furniture.setPlacedBy(level, helper.absolutePos(BASE), state, null, ItemStack.EMPTY);
                List<BlockPos> cushions = new ArrayList<>();
                if (furniture instanceof LoveSeatBlock) {
                    for (var side : LoveSeatBlock.Side.values()) cushions.add(LoveSeatBlock.partPos(BASE, front, side));
                } else if (furniture instanceof SofaBlock) {
                    for (var section : SofaBlock.Section.values()) cushions.add(SofaBlock.partPos(BASE, front, section));
                } else cushions.add(BASE);

                if (obstacles != 0) {
                    for (BlockPos cushion : cushions) {
                        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
                            BlockPos wall = cushion.offset(x, 0, z);
                            if (cushions.contains(wall)) continue;
                            if (obstacles == 1 && !wall.equals(cushion.relative(front))) continue;
                            helper.setBlock(wall, Blocks.STONE);
                            helper.setBlock(wall.above(), Blocks.STONE);
                        }
                    }
                }

                List<Player> riders = new ArrayList<>();
                List<SeatEntity> seats = new ArrayList<>();
                for (BlockPos cushion : cushions) {
                    // FakePlayer forbids riding. Use a real server player with
                    // a no-network test connection to exercise camera packets.
                    var player = new net.minecraft.server.level.ServerPlayer(level.getServer(), level,
                            new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "leather-seat-test"),
                            net.minecraft.server.level.ClientInformation.createDefault());
                    player.connection = new net.neoforged.neoforge.common.util.FakePlayer(level,
                            new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "seat-test-connection")).connection;
                    player.setYRot(front.getOpposite().toYRot());
                    player.setYHeadRot(front.getOpposite().toYRot());
                    player.setYBodyRot(front.getOpposite().toYRot());
                    helper.useBlock(cushion, player);
                    helper.assertTrue(player.getVehicle() instanceof SeatEntity, "every leather cushion must be usable");
                    var seat = (SeatEntity) player.getVehicle();
                    helper.assertTrue(!seats.contains(seat), "each cushion must have its own independent mount");
                    seat.tick();
                    helper.assertTrue(!seat.isRemoved() && player.getYRot() == front.toYRot()
                            && player.getYHeadRot() == front.toYRot() && player.yBodyRot == front.toYRot(),
                            "mounting must turn the rider's body and head toward the furniture front in " + front);
                    var other = helper.makeMockPlayer(GameType.CREATIVE);
                    helper.useBlock(cushion, other);
                    helper.assertTrue(!other.isPassenger() && seat.hasPassenger(player), "occupied cushions must reject another rider");
                    riders.add(player);
                    seats.add(seat);
                }

                for (int i = 0; i < riders.size(); i++) {
                    Player player = riders.get(i);
                    BlockPos absolute = helper.absolutePos(cushions.get(i));
                    player.stopRiding();
                    helper.assertTrue(!player.isPassenger() && level.noCollision(player),
                            "dismount must leave the rider clear of the cushion, arms, and back without jumping");
                    if (obstacles == 0) {
                        helper.assertTrue(player.blockPosition().equals(absolute.relative(front)) && player.getY() == absolute.getY(),
                                "open-floor dismount must prefer the floor in front of the occupied cushion");
                    } else if (obstacles == 1) {
                        helper.assertTrue(player.getY() == absolute.getY() && !player.blockPosition().equals(absolute.relative(front)),
                                "a blocked front must use another clear floor exit");
                    } else {
                        helper.assertTrue(player.getY() > absolute.getY() + 1
                                        && Math.abs(player.getX() - absolute.getX() - 0.5) < 0.0001
                                        && Math.abs(player.getZ() - absolute.getZ() - 0.5) < 0.0001,
                                "when every exit is blocked, place the rider just above their furniture section");
                    }
                    seats.get(i).tick();
                    helper.assertTrue(seats.get(i).isRemoved(), "empty mounts must be cleaned up");
                    if (i + 1 < riders.size()) helper.assertTrue(riders.get(i + 1).isPassenger(),
                            "dismounting one cushion must preserve other riders");
                }

                // Ensure breaking a reused complete furnishing still cleans up every rider.
                for (BlockPos cushion : cushions) helper.useBlock(cushion, helper.makeMockPlayer(GameType.CREATIVE));
                level.destroyBlock(helper.absolutePos(BASE), false);
                for (BlockPos cushion : cushions) {
                    BlockPos absolute = helper.absolutePos(cushion);
                    helper.assertTrue(level.getBlockState(absolute).isAir()
                            && level.getEntitiesOfClass(SeatEntity.class, new AABB(absolute).inflate(0.25)).isEmpty(),
                            "breaking the furniture must remove every section's mount");
                }
            }
        }
    }
}
