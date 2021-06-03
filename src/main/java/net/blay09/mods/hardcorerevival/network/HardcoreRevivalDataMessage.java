package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class HardcoreRevivalDataMessage {
    private final boolean knockedOut;
    private final int knockoutTicksPassed;

    public HardcoreRevivalDataMessage(boolean knockedOut, int knockoutTicksPassed) {
        this.knockedOut = knockedOut;
        this.knockoutTicksPassed = knockoutTicksPassed;
    }

    public static void encode(HardcoreRevivalDataMessage message, PacketBuffer buf) {
        buf.writeBoolean(message.knockedOut);
        buf.writeInt(message.knockoutTicksPassed);
    }

    public static HardcoreRevivalDataMessage decode(PacketBuffer buf) {
        boolean knockedOut = buf.readBoolean();
        int knockoutTicksPassed = buf.readInt();
        return new HardcoreRevivalDataMessage(knockedOut, knockoutTicksPassed);
    }

    public static void handle(HardcoreRevivalDataMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        NetworkHandler.ensureClientSide(context);

        context.enqueueWork(() -> {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
                HardcoreRevivalData revivalData = HardcoreRevival.getClientRevivalData();
                revivalData.setKnockedOut(message.knockedOut);
                revivalData.setKnockoutTicksPassed(message.knockoutTicksPassed);
            });
        });

        context.setPacketHandled(true);
    }
}
