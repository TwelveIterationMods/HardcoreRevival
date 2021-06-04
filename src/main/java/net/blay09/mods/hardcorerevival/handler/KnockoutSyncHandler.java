package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.network.HardcoreRevivalDataMessage;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = HardcoreRevival.MOD_ID)
public class KnockoutSyncHandler {
    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Watch event) {
        MinecraftServer server = event.getWorld().getServer();
        for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
            HardcoreRevivalData revivalData = HardcoreRevival.getRevivalData(player);
            if (revivalData.isKnockedOut()) {
                sendHardcoreRevivalData(event.getPlayer(), player, revivalData);
            }
        }
    }

    public static void sendHardcoreRevivalDataToWatching(PlayerEntity player, HardcoreRevivalData revivalData) {
        HardcoreRevivalDataMessage message = new HardcoreRevivalDataMessage(player.getEntityId(), revivalData.isKnockedOut(), revivalData.getKnockoutTicksPassed());
        NetworkHandler.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), message);
        sendHardcoreRevivalData(player, player, revivalData);
    }

    public static void sendHardcoreRevivalData(PlayerEntity player, Entity entity, HardcoreRevivalData revivalData) {
        HardcoreRevivalDataMessage message = new HardcoreRevivalDataMessage(entity.getEntityId(), revivalData.isKnockedOut(), revivalData.getKnockoutTicksPassed());
        NetworkHandler.sendToPlayer(player, message);
    }
}
