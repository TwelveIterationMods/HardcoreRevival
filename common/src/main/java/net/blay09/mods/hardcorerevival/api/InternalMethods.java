package net.blay09.mods.hardcorerevival.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public interface InternalMethods {
    void knockout(ServerPlayer player, DamageSource damageSource);

    void wakeup(ServerPlayer player, boolean applyEffects);

    boolean isKnockedOut(ServerPlayer player);

    int getKnockoutTicksPassed(ServerPlayer player);

    int getKnockoutTicksLeft(ServerPlayer player);
}
