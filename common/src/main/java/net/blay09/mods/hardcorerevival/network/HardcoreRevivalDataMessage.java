package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.PlayerHardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.client.HardcoreRevivalClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import static net.blay09.mods.hardcorerevival.HardcoreRevival.id;

public record HardcoreRevivalDataMessage(int entityId, boolean knockedOut, int knockoutTicksPassed, boolean beingRescued) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<HardcoreRevivalDataMessage> TYPE = new CustomPacketPayload.Type<>(id("hardcore_revival_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HardcoreRevivalDataMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            HardcoreRevivalDataMessage::entityId,
            ByteBufCodecs.BOOL,
            HardcoreRevivalDataMessage::knockedOut,
            ByteBufCodecs.INT,
            HardcoreRevivalDataMessage::knockoutTicksPassed,
            ByteBufCodecs.BOOL,
            HardcoreRevivalDataMessage::beingRescued,
            HardcoreRevivalDataMessage::new
    );

    public static void handle(Player player, HardcoreRevivalDataMessage message) {
        if (player != null) {
            Entity entity = player.level().getEntity(message.entityId);
            if (entity instanceof Player targetEntity) {
                PlayerHardcoreRevivalManager.setKnockedOut(targetEntity, message.knockedOut);
                PlayerHardcoreRevivalManager.setKnockoutTicksPassed(targetEntity, message.knockoutTicksPassed);
                HardcoreRevivalClient.setBeingRescued(message.beingRescued);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
