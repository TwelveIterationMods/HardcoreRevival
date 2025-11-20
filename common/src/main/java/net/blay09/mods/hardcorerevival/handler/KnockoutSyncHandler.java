package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.platform.event.callback.ServerPlayerCallback;
import net.blay09.mods.hardcorerevival.PlayerHardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.network.HardcoreRevivalDataMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

public class KnockoutSyncHandler {
    public static void initialize() {
        ServerPlayerCallback.ChunkTracking.START.register(KnockoutSyncHandler::onStartChunkTracking);
    }

    public static void onStartChunkTracking(ServerLevel level, ServerPlayer player, ChunkPos chunkPos) {
        final var  server = level.getServer();
        for (final var otherPlayer : server.getPlayerList().getPlayers()) {
            if (PlayerHardcoreRevivalManager.isKnockedOut(otherPlayer)) {
                sendHardcoreRevivalData(player, otherPlayer);
            }
        }
    }

    public static void sendHardcoreRevivalDataToWatching(Player player) {
        HardcoreRevivalDataMessage message = new HardcoreRevivalDataMessage(player.getId(),
                PlayerHardcoreRevivalManager.isKnockedOut(player),
                PlayerHardcoreRevivalManager.getKnockoutTicksPassed(player),
                false);
        Balm.networking().sendToTracking(player, message);
        sendHardcoreRevivalData(player, player);
    }

    public static void sendHardcoreRevivalData(Player toPlayer, Player forPlayer) {
        sendHardcoreRevivalData(toPlayer, forPlayer, false);
    }

    public static void sendHardcoreRevivalData(Player toPlayer, Player forPlayer, boolean beingRescued) {
        HardcoreRevivalDataMessage message = new HardcoreRevivalDataMessage(forPlayer.getId(),
                PlayerHardcoreRevivalManager.isKnockedOut(forPlayer),
                PlayerHardcoreRevivalManager.getKnockoutTicksPassed(forPlayer),
                beingRescued);
        Balm.networking().sendTo(toPlayer, message);
    }
}
