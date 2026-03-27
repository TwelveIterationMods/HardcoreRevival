package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.PlayerHardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import static net.blay09.mods.hardcorerevival.HardcoreRevival.id;

public record RescueMessage(int targetEntityId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RescueMessage> TYPE = new CustomPacketPayload.Type<>(id("rescue"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RescueMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            RescueMessage::targetEntityId,
            RescueMessage::new
    );

    public static void handle(ServerPlayer player, RescueMessage message) {
        if (!player.isAlive() || player.isSpectator() || PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            return;
        }

        if (message.targetEntityId < 0) {
            HardcoreRevivalManager.abortRescue(player);
            return;
        }

        Entity targetEntity = player.level().getEntity(message.targetEntityId);
        if (!(targetEntity instanceof Player target) || !PlayerHardcoreRevivalManager.isKnockedOut(target)) {
            HardcoreRevivalManager.abortRescue(player);
            return;
        }

        if (target.distanceTo(player) > HardcoreRevivalConfig.getActive().rescueDistance + 0.5f) {
            HardcoreRevivalManager.abortRescue(player);
            return;
        }

        HardcoreRevivalManager.startRescue(player, target);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
