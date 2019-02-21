package net.blay09.mods.hardcorerevival.network;

import io.netty.buffer.ByteBuf;
import net.blay09.mods.hardcorerevival.HardcoreRevivalConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Particles;
import net.minecraft.network.PacketBuffer;
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
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (message.entityId == mc.player.getEntityId()) {
                mc.player.extinguish();
                mc.player.setFlag(0, false); // burning flag
                if (HardcoreRevivalConfig.COMMON.glowOnDeath.get()) {
                    mc.player.setGlowing(false);
                    mc.player.setFlag(6, false); // glowing flag
                }
                mc.displayGuiScreen(null);
            }
            Entity entity = mc.world.getEntityByID(message.entityId);
            if (entity != null) {
                mc.world.spawnParticle(Particles.EXPLOSION, entity.posX, entity.posY, entity.posZ, 0, 0, 0);
            }
        });
    }
}
