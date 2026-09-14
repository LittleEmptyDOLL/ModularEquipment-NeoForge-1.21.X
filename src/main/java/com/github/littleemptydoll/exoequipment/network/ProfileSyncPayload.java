package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.controller.ControllerDefinition;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonProfile;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonValidation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record ProfileSyncPayload(
        int activeProfile,
        int maxProfiles,
        int maxActiveMatrices,
        int installedMatrices,
        List<Integer> profileMasks
) implements CustomPacketPayload {
    public static final Type<ProfileSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, "profile_sync")
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, List<Integer>> MASKS_CODEC =
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list());

    public static final StreamCodec<RegistryFriendlyByteBuf, ProfileSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ProfileSyncPayload::activeProfile,
                    ByteBufCodecs.VAR_INT, ProfileSyncPayload::maxProfiles,
                    ByteBufCodecs.VAR_INT, ProfileSyncPayload::maxActiveMatrices,
                    ByteBufCodecs.VAR_INT, ProfileSyncPayload::installedMatrices,
                    MASKS_CODEC, ProfileSyncPayload::profileMasks,
                    ProfileSyncPayload::new
            );

    public static ProfileSyncPayload fromData(ExoskeletonData data) {
        ControllerDefinition controller = ExoskeletonValidation.getControllerDefinition(data);

        int installed = 0;
        for (int slot = 0; slot < ExoskeletonData.MAX_MATRICES; slot++) {
            if (data.matrices().get(slot).matrix().isPresent()) {
                installed |= 1 << slot;
            }
        }

        List<Integer> masks = new ArrayList<>();
        for (ExoskeletonProfile profile : data.profiles()) {
            int mask = 0;
            for (int slot : profile.activeMatrices()) {
                if (slot >= 0 && slot < ExoskeletonData.MAX_MATRICES) {
                    mask |= 1 << slot;
                }
            }
            masks.add(mask);
        }

        return new ProfileSyncPayload(
                data.activeProfile(),
                controller.maxProfiles(),
                controller.maxActiveMatrices(),
                installed,
                List.copyOf(masks)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
