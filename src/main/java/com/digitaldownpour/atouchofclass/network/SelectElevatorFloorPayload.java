package com.digitaldownpour.atouchofclass.network;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent client -> server when a player picks a floor from the destination
 * control screen. The server re-validates lock ownership/phase from scratch
 * on receipt and never trusts the selection blindly.
 */
public record SelectElevatorFloorPayload(BlockPos controlPos, int targetFloorIndex) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SelectElevatorFloorPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ATouchofClass.MODID, "select_elevator_floor"));

    public static final StreamCodec<ByteBuf, SelectElevatorFloorPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            SelectElevatorFloorPayload::controlPos,
            ByteBufCodecs.VAR_INT,
            SelectElevatorFloorPayload::targetFloorIndex,
            SelectElevatorFloorPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
