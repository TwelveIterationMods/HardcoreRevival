package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.PlayerLoginEvent;
import net.blay09.mods.balm.api.event.PlayerLogoutEvent;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;


public class LoginLogoutHandler {

    public static void initialize() {
        Balm.getEvents().onEvent(PlayerLoginEvent.class, LoginLogoutHandler::onPlayerLogin);
        Balm.getEvents().onEvent(PlayerLogoutEvent.class, LoginLogoutHandler::onPlayerLogout);
    }

    public static void onPlayerLogin(PlayerLoginEvent event) {
        ServerPlayer player = event.getPlayer();
        CompoundTag data = Balm.getHooks().getPersistentData(player);
        HardcoreRevivalData revivalData = HardcoreRevival.getRevivalData(player);
        revivalData.deserialize(data.getCompound("HardcoreRevival"));

        if (HardcoreRevivalConfig.getActive().continueTimerWhileOffline && revivalData.isKnockedOut()) {
            final var now = System.currentTimeMillis();
            final var then = revivalData.getLastLogoutAt();
            final var millisPassed = (int) Math.max(0, now - then);
            final var secondsPassed = millisPassed / 1000;
            final var ticksPassed = secondsPassed * 20;
            revivalData.setKnockoutTicksPassed(revivalData.getKnockoutTicksPassed() + ticksPassed);
        }

        HardcoreRevival.getManager().updateKnockoutEffects(player);
    }

    public static void onPlayerLogout(PlayerLogoutEvent event) {
        Player player = event.getPlayer();
        CompoundTag data = Balm.getHooks().getPersistentData(player);
        HardcoreRevivalData revivalData = HardcoreRevival.getRevivalData(player);
        revivalData.setLastLogoutAt(player.level().getGameTime());
        Tag tag = revivalData.serialize();
        if (tag != null) {
            data.put("HardcoreRevival", tag);
        }
    }

}
