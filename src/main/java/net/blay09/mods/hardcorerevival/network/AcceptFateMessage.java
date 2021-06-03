package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class AcceptFateMessage {

    public static void handle(AcceptFateMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        NetworkHandler.ensureServerSide(context);

        context.enqueueWork(() -> {
            PlayerEntity player = context.getSender();
            if (player != null) {
                HardcoreRevival.getManager().notRescuedInTime(player);
            }
        });

        context.setPacketHandled(true);
    }

}
