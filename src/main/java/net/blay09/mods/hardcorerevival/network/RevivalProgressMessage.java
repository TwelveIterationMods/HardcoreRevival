package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.client.HardcoreRevivalClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class RevivalProgressMessage {
    private final int entityId;
    private final float progress;

    public RevivalProgressMessage(int entityId, float progress) {
        this.entityId = entityId;
        this.progress = progress;
    }

    public static void encode(RevivalProgressMessage message, PacketBuffer buf) {
        buf.writeInt(message.entityId);
        buf.writeFloat(message.progress);
    }

    public static RevivalProgressMessage decode(PacketBuffer buf) {
        int entityId = buf.readInt();
        float progress = buf.readFloat();
        return new RevivalProgressMessage(entityId, progress);
    }

    public static void handle(RevivalProgressMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        NetworkHandler.ensureClientSide(context);

        context.enqueueWork(() -> {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
                HardcoreRevivalClient.setRevivalProgress(message.entityId, message.progress);
            });
        });

        context.setPacketHandled(true);
    }
}
