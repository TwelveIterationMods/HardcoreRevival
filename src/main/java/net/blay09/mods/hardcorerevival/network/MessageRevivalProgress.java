package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageRevivalProgress {
    private final int entityId;
    private final float progress;

    public MessageRevivalProgress(int entityId, float progress) {
        this.entityId = entityId;
        this.progress = progress;
    }

    public static void encode(MessageRevivalProgress message, PacketBuffer buf) {
        buf.writeInt(message.entityId);
        buf.writeFloat(message.progress);
    }

    public static MessageRevivalProgress decode(PacketBuffer buf) {
        int entityId = buf.readInt();
        float progress = buf.readFloat();
        return new MessageRevivalProgress(entityId, progress);
    }

    public static void handle(MessageRevivalProgress message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        NetworkHandler.ensureClientSide(context);

        context.enqueueWork(() -> HardcoreRevival.client.ifPresent(it -> it.setRevivalProgress(message.entityId, message.progress)));
        context.setPacketHandled(true);
    }
}
