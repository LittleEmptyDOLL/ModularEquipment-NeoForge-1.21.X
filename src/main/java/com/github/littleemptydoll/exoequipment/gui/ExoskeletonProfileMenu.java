package com.github.littleemptydoll.exoequipment.gui;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonProfileOperations;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonValidation;
import com.github.littleemptydoll.exoequipment.item.ExoskeletonItem;
import com.github.littleemptydoll.exoequipment.network.ProfileSyncPayload;
import com.github.littleemptydoll.exoequipment.registry.ModDataComponents;
import com.github.littleemptydoll.exoequipment.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ExoskeletonProfileMenu extends AbstractContainerMenu {
    private static final int SELECT = 0;
    private static final int CREATE = 1;
    private static final int REMOVE = 2;
    private static final int TOGGLE_MATRIX = 3;
    private static final int RENAME = 4;

    private final Player player;
    private final ItemStack exoskeleton;

    private int activeProfile = -1;
    private int maxProfiles;
    private int maxActiveMatrices;
    private int installedMatrices;
    private List<Integer> profileMasks = List.of();
    private List<String> profileNames = List.of();

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
        profileNames = List.copyOf(payload.profileNames());
    }

    public ProfileSyncPayload getSyncPayload() {
        return ProfileSyncPayload.fromData(ExoskeletonItem.getData(exoskeleton));
    }

    public boolean handleAction(int action, int profile, int matrix, String name) {
        if (player.level().isClientSide) {
            return false;
        }

        ExoskeletonData data = ExoskeletonItem.getData(exoskeleton);

        try {
            switch (action) {
                case SELECT -> data = ExoskeletonProfileOperations.activateProfile(data, profile);
                case CREATE -> {
                    data = ExoskeletonProfileOperations.createProfile(data);
                    data = ExoskeletonProfileOperations.activateProfile(
                            data,
                            data.profiles().size() - 1
                    );
                }
                case REMOVE -> data = ExoskeletonProfileOperations.removeProfile(data, profile);
                case TOGGLE_MATRIX -> data = toggleMatrix(data, profile, matrix);
                case RENAME -> data = ExoskeletonProfileOperations.renameProfile(data, profile, name);
                default -> {
                    return false;
                }
            }
        } catch (IllegalArgumentException | IllegalStateException ignored) {
            return false;
        }

        exoskeleton.set(ModDataComponents.EXOSKELETON_DATA.get(), data);
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

    public String getProfileName(int profile) {
        if (profile < 0 || profile >= profileNames.size()) {
            return "";
        }
        return profileNames.get(profile);
    }

    public int getActiveMatrixCount() {
        if (activeProfile < 0 || activeProfile >= profileMasks.size()) {
            return 0;
        }
        return Integer.bitCount(profileMasks.get(activeProfile));
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
