package com.github.littleemptydoll.exoequipment.client;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonAccess;
import com.github.littleemptydoll.exoequipment.module.JetpackOperations;
import com.github.littleemptydoll.exoequipment.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

final class JetpackSoundInstance extends AbstractTickableSoundInstance {
    private final LocalPlayer player;

    JetpackSoundInstance(LocalPlayer player) {
        super(ModSounds.JETPACK_LOOP.get(), SoundSource.PLAYERS,
                SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.relative = true;
        this.volume = 0.55F;
    }

    static boolean isActive(LocalPlayer player) {
        if (player == null || player != Minecraft.getInstance().player) {
            return false;
        }
        return ExoskeletonAccess.findContext(player)
                .map(context -> JetpackOperations.isThrusting(
                        player, context.data(), context.poweredModules()))
                .orElse(false);
    }

    void stopSound() {
        stop();
    }

    @Override
    public void tick() {
        if (!isActive(player)) {
            stop();
        }
    }
}
