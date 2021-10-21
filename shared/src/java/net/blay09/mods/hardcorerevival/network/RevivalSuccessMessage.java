package net.blay09.mods.hardcorerevival.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class RevivalSuccessMessage {
    private final int entityId;

    public RevivalSuccessMessage(int entityId) {
        this.entityId = entityId;
    }

    public static RevivalSuccessMessage decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        return new RevivalSuccessMessage(entityId);
    }

    public static void encode(RevivalSuccessMessage message, FriendlyByteBuf buf) {
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
}
