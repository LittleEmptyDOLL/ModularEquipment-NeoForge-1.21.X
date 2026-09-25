package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ProfileActionPayload(
        int action,
        int profile,
        int matrix,
        String name
) implements CustomPacketPayload {

    public ProfileActionPayload(
            Action action,
            int profile,
            int matrix
    ) {
        this(action, profile, matrix, "");
    }

    public ProfileActionPayload(
            Action action,
            int profile,
            int matrix,
            String name
    ) {
        this(
                action.id(),
                profile,
                matrix,
                name
        );
    }

    public Action actionType() {
        return Action.fromId(action);
    }

    public static final Type<ProfileActionPayload> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            ExoEquipment.MODID,
                            "profile_action"
                    )
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, ProfileActionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ProfileActionPayload::action,
                    ByteBufCodecs.VAR_INT,
                    ProfileActionPayload::profile,
                    ByteBufCodecs.VAR_INT,
                    ProfileActionPayload::matrix,
                    ByteBufCodecs.stringUtf8(32),
                    ProfileActionPayload::name,
                    ProfileActionPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Action {
        SELECT(0),
        CREATE(1),
        REMOVE(2),
        TOGGLE_MATRIX(3),
        RENAME(4);

        private final int id;

        Action(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }

        public static Action fromId(int id) {
            return switch (id) {
                case 0 -> SELECT;
                case 1 -> CREATE;
                case 2 -> REMOVE;
                case 3 -> TOGGLE_MATRIX;
                case 4 -> RENAME;
                default -> null;
            };
        }
    }
}
