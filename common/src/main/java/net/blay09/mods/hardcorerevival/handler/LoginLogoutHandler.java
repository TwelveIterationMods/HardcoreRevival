package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.balm.platform.event.callback.ServerPlayerCallback;
import net.blay09.mods.hardcorerevival.HardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.PlayerHardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.server.level.ServerPlayer;

public class LoginLogoutHandler {

    public static void initialize() {
        ServerPlayerCallback.Login.EVENT.register(LoginLogoutHandler::onPlayerLogin);
        ServerPlayerCallback.Logout.EVENT.register(LoginLogoutHandler::onPlayerLogout);
    }

    public static void onPlayerLogin(ServerPlayer player) {
        if (HardcoreRevivalConfig.getActive().continueTimerWhileOffline && PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            final var now = System.currentTimeMillis();
            final var then = PlayerHardcoreRevivalManager.getLastLogoutAt(player);
            final var millisPassed = (int) Math.max(0, now - then);
            final var secondsPassed = millisPassed / 1000;
            final var ticksPassed = secondsPassed * 20;
            PlayerHardcoreRevivalManager.setKnockoutTicksPassed(player, PlayerHardcoreRevivalManager.getKnockoutTicksPassed(player) + ticksPassed);
        }

        HardcoreRevivalManager.updateKnockoutEffects(player);
    }

    public static void onPlayerLogout(ServerPlayer player) {
        PlayerHardcoreRevivalManager.setLastLogoutAt(player, System.currentTimeMillis());
    }

}
