package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.network.HardcoreRevivalDataMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HardcoreRevival.MOD_ID)
public class KnockoutSyncHandler {
    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Watch event) {
        MinecraftServer server = event.getWorld().getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            HardcoreRevivalData revivalData = HardcoreRevival.getRevivalData(player);
            if (revivalData.isKnockedOut()) {
                sendHardcoreRevivalData(event.getPlayer(), player, revivalData);
            }
        }
    }

    public static void sendHardcoreRevivalDataToWatching(Player player, HardcoreRevivalData revivalData) {
        HardcoreRevivalDataMessage message = new HardcoreRevivalDataMessage(player.getId(), revivalData.isKnockedOut(), revivalData.getKnockoutTicksPassed(), false);
        Balm.getNetworking().sendToTracking(player, message);
        sendHardcoreRevivalData(player, player, revivalData);
    }

    public static void sendHardcoreRevivalData(Player player, Entity entity, HardcoreRevivalData revivalData) {
        sendHardcoreRevivalData(player, entity, revivalData, false);
    }

    public static void sendHardcoreRevivalData(Player player, Entity entity, HardcoreRevivalData revivalData, boolean beingRescued) {
        HardcoreRevivalDataMessage message = new HardcoreRevivalDataMessage(entity.getId(), revivalData.isKnockedOut(), revivalData.getKnockoutTicksPassed(), beingRescued);
        Balm.getNetworking().sendTo(player, message);
    }
}
