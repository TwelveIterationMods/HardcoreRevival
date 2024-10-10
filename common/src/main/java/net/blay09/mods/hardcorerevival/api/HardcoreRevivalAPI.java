package net.blay09.mods.hardcorerevival.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

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

    public static void knockout(ServerPlayer player, DamageSource damageSource) {
        internalMethods.knockout(player, damageSource);
    }

    public static void wakeup(ServerPlayer player, boolean applyEffects) {
        internalMethods.wakeup(player, applyEffects);
    }

    public static boolean isKnockedOut(ServerPlayer player) {
        return internalMethods.isKnockedOut(player);
    }

    public static int getKnockoutTicksPassed(ServerPlayer player) {
        return internalMethods.getKnockoutTicksPassed(player);
    }

    public static int getKnockoutTicksLeft(ServerPlayer player) {
        return internalMethods.getKnockoutTicksLeft(player);
    }
}
