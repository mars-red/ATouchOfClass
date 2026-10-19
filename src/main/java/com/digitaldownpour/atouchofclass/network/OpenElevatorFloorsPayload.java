package com.digitaldownpour.atouchofclass.network;

import com.digitaldownpour.atouchofclass.ATouchofClass;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Sent server -> client when a destination control is validly used: carries
 * the derived list of other floor indices on this shaft so the client can
 * present a selection screen. The client never invents this list itself.
 */
public record OpenElevatorFloorsPayload(BlockPos controlPos, List<Integer> floorIndices, int currentFloorIndex) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenElevatorFloorsPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(ATouchofClass.MODID, "open_elevator_floors"));

    public static final StreamCodec<ByteBuf, OpenElevatorFloorsPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpenElevatorFloorsPayload::controlPos,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.VAR_INT),
            OpenElevatorFloorsPayload::floorIndices,
            ByteBufCodecs.VAR_INT,
            OpenElevatorFloorsPayload::currentFloorIndex,
            OpenElevatorFloorsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
