package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class RevivalSuccessMessage implements CustomPacketPayload {

    public static CustomPacketPayload.Type<RevivalSuccessMessage> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath(HardcoreRevival.MOD_ID, "revival_success"));

    private final int entityId;

    public RevivalSuccessMessage(int entityId) {
        this.entityId = entityId;
    }

    public static RevivalSuccessMessage decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        return new RevivalSuccessMessage(entityId);
    }

    public static void encode(FriendlyByteBuf buf, RevivalSuccessMessage message) {
        buf.writeInt(message.entityId);
    }

    public static void handle(Player player, RevivalSuccessMessage message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        if (message.entityId == mc.player.getId()) {
            mc.setScreen(null);
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
