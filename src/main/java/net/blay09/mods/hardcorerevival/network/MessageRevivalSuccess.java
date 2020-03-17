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

public class MessageRevivalSuccess {
    private final int entityId;

    public MessageRevivalSuccess(int entityId) {
        this.entityId = entityId;
    }

    public static MessageRevivalSuccess decode(PacketBuffer buf) {
        int entityId = buf.readInt();
        return new MessageRevivalSuccess(entityId);
    }

    public static void encode(MessageRevivalSuccess message, ByteBuf buf) {
        buf.writeInt(message.entityId);
    }

    public static void handle(MessageRevivalSuccess message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        NetworkHandler.ensureClientSide(context);

        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.world == null) {
                return;
            }

            if (message.entityId == mc.player.getEntityId()) {
                mc.player.extinguish();
                mc.player.setFlag(0, false); // burning flag
                if (HardcoreRevivalConfig.SERVER.glowOnDeath.get()) {
                    mc.player.setGlowing(false);
                    mc.player.setFlag(6, false); // glowing flag
                }
                mc.displayGuiScreen(null);
            }

            Entity entity = mc.world.getEntityByID(message.entityId);
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).deathTime = -1;
                mc.world.addParticle(ParticleTypes.EXPLOSION, entity.func_226277_ct_(), entity.func_226278_cu_(), entity.func_226281_cx_(), 0, 0, 0);
            }
        });
        context.setPacketHandled(true);
    }
}
