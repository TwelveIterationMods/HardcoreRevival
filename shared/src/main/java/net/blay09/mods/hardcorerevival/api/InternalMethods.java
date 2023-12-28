package net.blay09.mods.hardcorerevival.api;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

public interface InternalMethods {
    void knockout(Player player, DamageSource damageSource);

    void wakeup(Player player, boolean applyEffects);

    boolean isKnockedOut(Player player);

    int getKnockoutTicksPassed(Player player);

    int getKnockoutTicksLeft(Player player);
}
