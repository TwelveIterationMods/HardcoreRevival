package net.blay09.mods.hardcorerevival.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import static net.blay09.mods.hardcorerevival.HardcoreRevival.id;

public record RevivalSuccessMessage(int entityId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RevivalSuccessMessage> TYPE = new CustomPacketPayload.Type<>(id("revival_success"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RevivalSuccessMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            RevivalSuccessMessage::entityId,
            RevivalSuccessMessage::new
    );

    public static void handle(Player player, RevivalSuccessMessage message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        if (message.entityId == mc.player.getId()) {
            mc.gui.setScreen(null);
        }

        Entity entity = mc.level.getEntity(message.entityId);
        if (entity instanceof LivingEntity) {
            mc.level.addParticle(ParticleTypes.EXPLOSION, entity.getX(), entity.getY(), entity.getZ(), 0, 0, 0);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
