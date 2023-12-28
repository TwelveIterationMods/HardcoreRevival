package net.blay09.mods.hardcorerevival.api;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.InvocationTargetException;

public class HardcoreRevivalAPI {

    private static final InternalMethods internalMethods = loadInternalMethods();

    private static InternalMethods loadInternalMethods() {
        try {
            return (InternalMethods) Class.forName("net.blay09.mods.hardcorerevival.InternalMethodsImpl").getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            throw new RuntimeException("Failed to load Hardcore Revival API", e);
        }
    }

    public static void knockout(Player player, DamageSource damageSource) {
        internalMethods.knockout(player, damageSource);
    }

    public static void wakeup(Player player, boolean applyEffects) {
        internalMethods.wakeup(player, applyEffects);
    }

    public static boolean isKnockedOut(Player player) {
        return internalMethods.isKnockedOut(player);
    }

    public static int getKnockoutTicksPassed(Player player) {
        return internalMethods.getKnockoutTicksPassed(player);
    }

    public static int getKnockoutTicksLeft(Player player) {
        return internalMethods.getKnockoutTicksLeft(player);
    }
}
