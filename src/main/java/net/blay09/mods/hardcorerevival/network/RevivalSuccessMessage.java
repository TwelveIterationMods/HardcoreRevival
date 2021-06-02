package net.blay09.mods.hardcorerevival.network;

import io.netty.buffer.ByteBuf;
import net.blay09.mods.hardcorerevival.HardcoreRevivalConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class RevivalSuccessMessage {
    private final int entityId;

    public RevivalSuccessMessage(int entityId) {
        this.entityId = entityId;
    }

    public static RevivalSuccessMessage decode(PacketBuffer buf) {
        int entityId = buf.readInt();
        return new RevivalSuccessMessage(entityId);
    }

    public static void encode(RevivalSuccessMessage message, ByteBuf buf) {
        buf.writeInt(message.entityId);
    }

    public static void handle(RevivalSuccessMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        NetworkHandler.ensureClientSide(context);

        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.world == null) {
                return;
            }

            if (message.entityId == mc.player.getEntityId()) {
                mc.displayGuiScreen(null);
            }

            Entity entity = mc.world.getEntityByID(message.entityId);
            if (entity instanceof LivingEntity) {
                mc.world.addParticle(ParticleTypes.EXPLOSION, entity.getPosX(), entity.getPosY(), entity.getPosZ(), 0, 0, 0);
            }
        });
        context.setPacketHandled(true);
    }
}
