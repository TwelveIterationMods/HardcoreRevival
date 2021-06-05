package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class RescueMessage {
    private final boolean active;

    public RescueMessage(boolean active) {
        this.active = active;
    }

    public static void encode(RescueMessage message, PacketBuffer buf) {
        buf.writeBoolean(message.active);
    }

    public static RescueMessage decode(PacketBuffer buf) {
        boolean active = buf.readBoolean();
        return new RescueMessage(active);
    }

    public static void handle(RescueMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            PlayerEntity player = context.getSender();
            if (player == null || player.getHealth() <= 0 || player.isSpectator()) {
                return;
            }

            if (message.active) {
                final double range = HardcoreRevivalConfig.getActive().getRescueDistance();
                List<PlayerEntity> candidates = player.world.getEntitiesWithinAABB(PlayerEntity.class, player.getBoundingBox().grow(range), p -> p != null && HardcoreRevival.getRevivalData(p).isKnockedOut());
                float minDist = Float.MAX_VALUE;
                PlayerEntity target = null;
                for (PlayerEntity candidate : candidates) {
                    float dist = candidate.getDistance(player);
                    if (dist < minDist) {
                        target = candidate;
                        minDist = dist;
                    }
                }
                if (target != null) {
                    HardcoreRevival.getManager().startRescue(player, target);

                }
            } else {
                HardcoreRevival.getManager().abortRescue(player);
            }
        });
        context.setPacketHandled(true);
    }
}
