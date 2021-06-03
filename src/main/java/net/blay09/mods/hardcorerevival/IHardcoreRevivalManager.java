package net.blay09.mods.hardcorerevival;

import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.minecraft.entity.player.PlayerEntity;

public interface IHardcoreRevivalManager {

    HardcoreRevivalData getRevivalData(PlayerEntity player);

    default boolean isKnockedOut(PlayerEntity player) {
        return getRevivalData(player).isKnockedOut();
    }
}
