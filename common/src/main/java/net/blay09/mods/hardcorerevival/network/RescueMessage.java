package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class RescueMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RescueMessage> TYPE = new CustomPacketPayload.Type<>(new ResourceLocation(HardcoreRevival.MOD_ID, "rescue"));

    private final boolean active;

    public RescueMessage(boolean active) {
        this.active = active;
    }

    public static void encode(FriendlyByteBuf buf, RescueMessage message) {
        buf.writeBoolean(message.active);
    }

    public static RescueMessage decode(FriendlyByteBuf buf) {
        boolean active = buf.readBoolean();
        return new RescueMessage(active);
    }

    public static void handle(ServerPlayer player, RescueMessage message) {
        if (player == null || !player.isAlive() || player.isSpectator() || HardcoreRevival.getRevivalData(player).isKnockedOut()) {
            return;
        }

        if (message.active) {
            final double range = HardcoreRevivalConfig.getActive().rescueDistance;
            List<Player> candidates = player.level().getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(range), p -> p != null && HardcoreRevival.getRevivalData(p).isKnockedOut());
            float minDist = Float.MAX_VALUE;
            Player target = null;
            for (Player candidate : candidates) {
                float dist = candidate.distanceTo(player);
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
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
