package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.client.HardcoreRevivalClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import static net.blay09.mods.hardcorerevival.HardcoreRevival.id;

public record RevivalProgressMessage(int entityId, float progress) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RevivalProgressMessage> TYPE = new CustomPacketPayload.Type<>(id("revival_progress"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RevivalProgressMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            RevivalProgressMessage::entityId,
            ByteBufCodecs.FLOAT,
            RevivalProgressMessage::progress,
            RevivalProgressMessage::new
    );

    public static void handle(Player player, RevivalProgressMessage message) {
        HardcoreRevivalClient.setRevivalProgress(message.entityId, message.progress);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
