package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonProfile;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonProfileOperations;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonValidation;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.network.ProfileSyncPayload;
import com.github.littleemptydoll.exoequipment.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ExoskeletonProfileMenu extends AbstractContainerMenu {
    private final Player player;
    private final ItemStack exoskeleton;

    private int activeProfile = -1;
    private int maxProfiles;
    private int maxActiveMatrices;
    private int installedMatrices;
    private List<Integer> profileMasks = List.of();

    // Client
    public ExoskeletonProfileMenu(
            int containerId,
            Inventory inventory,
            RegistryFriendlyByteBuf buffer
    ) {
        this(containerId, inventory, ItemStack.STREAM_CODEC.decode(buffer));
    }

    // Server
    public ExoskeletonProfileMenu(
            int containerId,
            Inventory inventory,
            ItemStack exoskeleton
    ) {
        super(ModMenus.EXOSKELETON_PROFILES.get(), containerId);
        this.player = inventory.player;
        if (!(exoskeleton.getItem() instanceof ExoskeletonItem)) {
            throw new IllegalArgumentException("Source stack does not contain an exoskeleton");
        }
        this.exoskeleton = exoskeleton;
        refreshFromData();
    }

    public void applySync(ProfileSyncPayload payload) {
        activeProfile = payload.activeProfile();
        maxProfiles = payload.maxProfiles();
        maxActiveMatrices = payload.maxActiveMatrices();
        installedMatrices = payload.installedMatrices();
        profileMasks = List.copyOf(payload.profileMasks());
    }

    public ProfileSyncPayload getSyncPayload() {
        return ProfileSyncPayload.fromData(ExoskeletonItem.getData(exoskeleton));
    }

    public boolean handleAction(int action, int profile, int matrix) {
        if (player.level().isClientSide) {
            return false;
        }

        ExoskeletonData data = ExoskeletonItem.getData(exoskeleton);

        try {
            switch (action) {
                case 0 -> data = ExoskeletonProfileOperations.activateProfile(data, profile);
                case 1 -> {
                    data = ExoskeletonProfileOperations.createProfile(data);
                    data = ExoskeletonProfileOperations.activateProfile(
                            data,
                            data.profiles().size() - 1
                    );
                }
                case 2 -> data = ExoskeletonProfileOperations.removeProfile(data, profile);
                case 3 -> data = toggleMatrix(data, profile, matrix);
                default -> {
                    return false;
                }
            }
        } catch (IllegalArgumentException | IllegalStateException ignored) {
            return false;
        }

        exoskeleton.set(
                com.github.littleemptydoll.exoequipment.registry.ModDataComponents.EXOSKELETON_DATA.get(),
                data
        );
        refreshFromData();
        return true;
    }

    private ExoskeletonData toggleMatrix(
            ExoskeletonData data,
            int profile,
            int matrix
    ) {
        ExoskeletonValidation.validateProfileIndex(data, profile);
        ExoskeletonValidation.validateMatrixSlot(matrix);

        if (data.matrices().get(matrix).matrix().isEmpty()) {
            return data;
        }

        List<Integer> matrices = new ArrayList<>(
                data.profiles().get(profile).activeMatrices()
        );

        if (matrices.contains(matrix)) {
            matrices.remove(Integer.valueOf(matrix));
        } else {
            matrices.add(matrix);
        }

        return ExoskeletonProfileOperations.setProfileMatrices(
                data,
                profile,
                matrices
        );
    }

    private void refreshFromData() {
        if (player.level().isClientSide) {
            return;
        }
        applySync(getSyncPayload());
    }

    public int getActiveProfile() {
        return activeProfile;
    }

    public int getMaxProfiles() {
        return maxProfiles;
    }

    public int getMaxActiveMatrices() {
        return maxActiveMatrices;
    }

    public int getProfileCount() {
        return profileMasks.size();
    }

    public boolean isProfileActive(int profile) {
        return profile == activeProfile;
    }

    public boolean isMatrixInstalled(int matrix) {
        return matrix >= 0
                && matrix < ExoskeletonData.MAX_MATRICES
                && (installedMatrices & (1 << matrix)) != 0;
    }

    public boolean isMatrixActive(int profile, int matrix) {
        return profile >= 0
                && profile < profileMasks.size()
                && matrix >= 0
                && matrix < ExoskeletonData.MAX_MATRICES
                && (profileMasks.get(profile) & (1 << matrix)) != 0;
    }

    public ExoskeletonData getServerData() {
        return ExoskeletonItem.getData(exoskeleton);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.level().isClientSide) {
            return true;
        }

        if (!player.isAlive()) {
            return false;
        }

        return ExoskeletonMenuProvider.findBodyExoskeleton(player)
                .map(current -> current == exoskeleton)
                .orElse(false);
    }
}
