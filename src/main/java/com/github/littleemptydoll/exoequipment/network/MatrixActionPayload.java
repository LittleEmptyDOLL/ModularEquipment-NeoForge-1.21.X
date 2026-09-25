package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MatrixActionPayload(
        int action,
        int x,
        int y,
        int targetX,
        int targetY,
        int rotation
) implements CustomPacketPayload {

    public MatrixActionPayload(
            Action action,
            int x,
            int y,
            int targetX,
            int targetY,
            int rotation
    ) {
        this(
                action.id(),
                x,
                y,
                targetX,
                targetY,
                rotation
        );
    }

    public Action actionType() {
        return Action.fromId(action);
    }

    public static final Type<MatrixActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "matrix_action"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, MatrixActionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    MatrixActionPayload::action,
                    ByteBufCodecs.VAR_INT,
                    MatrixActionPayload::x,
                    ByteBufCodecs.VAR_INT,
                    MatrixActionPayload::y,
                    ByteBufCodecs.VAR_INT,
                    MatrixActionPayload::targetX,
                    ByteBufCodecs.VAR_INT,
                    MatrixActionPayload::targetY,
                    ByteBufCodecs.VAR_INT,
                    MatrixActionPayload::rotation,
                    MatrixActionPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Action {
        PLACE(0),
        REMOVE(1),
        ROTATE(2),
        MOVE(3);

        private final int id;

        Action(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }

        public static Action fromId(int id) {
            return switch (id) {
                case 0 -> PLACE;
                case 1 -> REMOVE;
                case 2 -> ROTATE;
                case 3 -> MOVE;
                default -> null;
            };
        }
    }
}
