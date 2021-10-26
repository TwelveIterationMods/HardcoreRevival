package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.PlayerLoginEvent;
import net.blay09.mods.balm.api.event.PlayerLogoutEvent;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalDataImpl;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

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
            long worldTimeNow = player.level.getGameTime();
            long worldTimeThen = revivalData.getLogoutWorldTime();
            int worldTimePassed = (int) Math.max(0, worldTimeNow - worldTimeThen);
            revivalData.setKnockoutTicksPassed(revivalData.getKnockoutTicksPassed() + worldTimePassed);
        }

        HardcoreRevival.getManager().updateKnockoutEffects(player);
    }

    public static void onPlayerLogout(PlayerLogoutEvent event) {
        Player player = event.getPlayer();
        CompoundTag data = Balm.getHooks().getPersistentData(player);
        HardcoreRevivalData revivalData = HardcoreRevival.getRevivalData(player);
        revivalData.setLogoutWorldTime(player.level.getGameTime());
        Tag tag = revivalData.serialize();
        if (tag != null) {
            data.put("HardcoreRevival", tag);
        }
    }

}
