package net.blay09.mods.hardcorerevival;

import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.minecraft.world.entity.player.Player;

public interface IHardcoreRevivalManager {

    HardcoreRevivalData getRevivalData(Player player);

    default boolean isKnockedOut(Player player) {
        return getRevivalData(player).isKnockedOut();
    }
}
