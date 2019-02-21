package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.capability.CapabilityHardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.IHardcoreRevival;
import net.blay09.mods.hardcorerevival.client.HardcoreRevivalClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageDie {

    public static void handle(MessageDie message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                EntityPlayer player = context.getSender();
                if (player == null) {
                    return;
                }

                LazyOptional<IHardcoreRevival> revival = player.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY);
                revival.ifPresent(it -> it.setDeathTime(HardcoreRevivalConfig.COMMON.maxDeathTicks.get()));
            } else {
                HardcoreRevival.client.ifPresent(HardcoreRevivalClient::onFinalDeath); // TODO maybe unsafe
            }
        });
    }

}
