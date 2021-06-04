package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class HardcoreRevivalDataMessage {
    private final int entityId;
    private final boolean knockedOut;
    private final int knockoutTicksPassed;

    public HardcoreRevivalDataMessage(int entityId, boolean knockedOut, int knockoutTicksPassed) {
        this.entityId = entityId;
        this.knockedOut = knockedOut;
        this.knockoutTicksPassed = knockoutTicksPassed;
    }

    public static void encode(HardcoreRevivalDataMessage message, PacketBuffer buf) {
        buf.writeInt(message.entityId);
        buf.writeBoolean(message.knockedOut);
        buf.writeInt(message.knockoutTicksPassed);
    }

    public static HardcoreRevivalDataMessage decode(PacketBuffer buf) {
        int entityId = buf.readInt();
        boolean knockedOut = buf.readBoolean();
        int knockoutTicksPassed = buf.readInt();
        return new HardcoreRevivalDataMessage(entityId, knockedOut, knockoutTicksPassed);
    }

    public static void handle(HardcoreRevivalDataMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        NetworkHandler.ensureClientSide(context);

        context.enqueueWork(() -> {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.world != null && mc.player != null) {
                    Entity entity = mc.world.getEntityByID(message.entityId);
                    if (entity != null) {
                        HardcoreRevivalData revivalData = entity.getEntityId() == mc.player.getEntityId() ? HardcoreRevival.getClientRevivalData() : HardcoreRevival.getRevivalData(entity);
                        revivalData.setKnockedOut(message.knockedOut);
                        revivalData.setKnockoutTicksPassed(message.knockoutTicksPassed);
                    }
                }
            });
        });

        context.setPacketHandled(true);
    }
}
