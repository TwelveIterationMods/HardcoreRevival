package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.handler.RescueHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class MessageRevival {
    private final boolean active;

    public MessageRevival(boolean active) {
        this.active = active;
    }

    public static void encode(MessageRevival message, PacketBuffer buf) {
        buf.writeBoolean(message.active);
    }

    public static MessageRevival decode(PacketBuffer buf) {
        boolean active = buf.readBoolean();
        return new MessageRevival(active);
    }

    public static void handle(MessageRevival message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            EntityPlayer player = context.getSender();
            if (player == null || player.getHealth() <= 0) {
                return;
            }

            if (message.active) {
                final double range = HardcoreRevivalConfig.COMMON.maxRescueDist.get();
                List<EntityPlayer> candidates = player.world.getEntitiesWithinAABB(EntityPlayer.class, player.getBoundingBox().grow(range), p -> p != null && p.getHealth() <= 0f);
                float minDist = Float.MAX_VALUE;
                EntityPlayer target = null;
                for (EntityPlayer candidate : candidates) {
                    float dist = candidate.getDistance(player);
                    if (dist < minDist) {
                        target = candidate;
                        minDist = dist;
                    }
                }
                if (target != null) {
                    RescueHandler.startRescue(player, target);
                }
            } else {
                RescueHandler.abortRescue(player);
            }
        });
    }
}
