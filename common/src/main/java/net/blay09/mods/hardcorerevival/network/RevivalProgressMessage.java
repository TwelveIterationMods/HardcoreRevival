package net.blay09.mods.hardcorerevival.network;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.client.HardcoreRevivalClient;
import net.blay09.mods.hardcorerevival.client.hint.HintOverlay;
import net.blay09.mods.shogi.network.ShogiStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import static net.blay09.mods.hardcorerevival.HardcoreRevival.id;

public record RevivalProgressMessage(int entityId, float progress, @Nullable Either<?, ?> ruleHint) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RevivalProgressMessage> TYPE = new CustomPacketPayload.Type<>(id("revival_progress"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RevivalProgressMessage> STREAM_CODEC = StreamCodec.of(
            RevivalProgressMessage::encode,
            RevivalProgressMessage::decode
    );

    public RevivalProgressMessage(int entityId, float progress) {
        this(entityId, progress, null);
    }

    @SuppressWarnings("unchecked")
    public static void encode(RegistryFriendlyByteBuf buf, RevivalProgressMessage message) {
        ByteBufCodecs.INT.encode(buf, message.entityId);
        ByteBufCodecs.FLOAT.encode(buf, message.progress);
        final var canEncodeHint = message.ruleHint == null || ShogiStreamCodecs.canEncodeEither(message.ruleHint);
        if (message.ruleHint != null && !canEncodeHint) {
            HardcoreRevival.logger.warn("Dropping unsyncable revival rule hint: {}", Either.unwrap(message.ruleHint));
        }
        buf.writeBoolean(message.ruleHint != null && canEncodeHint);
        if (message.ruleHint != null && canEncodeHint) {
            ByteBufCodecs.either(ShogiStreamCodecs.dynamicObjectCodec(), ShogiStreamCodecs.dynamicObjectCodec())
                    .encode(buf, (Either<Object, Object>) message.ruleHint);
        }
    }

    public static RevivalProgressMessage decode(RegistryFriendlyByteBuf buf) {
        final int entityId = ByteBufCodecs.INT.decode(buf);
        final float progress = ByteBufCodecs.FLOAT.decode(buf);
        if (!buf.readBoolean()) {
            return new RevivalProgressMessage(entityId, progress);
        }

        final var hint = ByteBufCodecs.either(ShogiStreamCodecs.dynamicObjectCodec(), ShogiStreamCodecs.dynamicObjectCodec()).decode(buf);
        return new RevivalProgressMessage(entityId, progress, hint);
    }

    public static void handle(Player player, RevivalProgressMessage message) {
        HardcoreRevivalClient.setRevivalProgress(message.entityId, message.progress);
        if (message.ruleHint != null) {
            HintOverlay.setActiveHint(message.ruleHint);
            if (message.ruleHint.right().isPresent()) {
                HardcoreRevivalClient.clearRescueProgress();
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
