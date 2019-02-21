package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageDeathTime {
    private final int deathTime;

    public MessageDeathTime(int deathTime) {
        this.deathTime = deathTime;
    }

    public static void encode(MessageDeathTime message, PacketBuffer buf) {
        buf.writeInt(message.deathTime);
    }

    public static MessageDeathTime decode(PacketBuffer buf) {
        int deathTime = buf.readInt();
        return new MessageDeathTime(deathTime);
    }

    public static void handle(MessageDeathTime message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            HardcoreRevival.client.ifPresent(it -> it.setDeathTime(message.deathTime)); // TODO maybe unsafe
        });
    }
}
