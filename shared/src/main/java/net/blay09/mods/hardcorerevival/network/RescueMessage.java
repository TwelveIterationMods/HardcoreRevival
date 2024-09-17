package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class RescueMessage {
    private final boolean active;

    public RescueMessage(boolean active) {
        this.active = active;
    }

    public static void encode(RescueMessage message, FriendlyByteBuf buf) {
        buf.writeBoolean(message.active);
    }

    public static RescueMessage decode(FriendlyByteBuf buf) {
        boolean active = buf.readBoolean();
        return new RescueMessage(active);
    }

    private static boolean isLookingTowards(Player player, Entity candidate) {
        double dx = candidate.getX() - player.getX();
        double dy = candidate.getY() - 1 - player.getY();
        double dz = candidate.getZ() - player.getZ();

        // Calculate the dot product of the view vector and the vector to the candidate
        double dotProduct = player.getLookAngle().x * dx + player.getLookAngle().y * dy + player.getLookAngle().z * dz;

        // Check if the candidate is within a 60-degree cone in front of the player
        return dotProduct > 0 && Math.abs(Math.acos(dotProduct / Math.sqrt(dx * dx + dy * dy + dz * dz))) < Math.PI / 3;
    }

    public static void handle(ServerPlayer player, RescueMessage message) {
        if (player == null || !player.isAlive() || player.isSpectator() || HardcoreRevival.getRevivalData(player).isKnockedOut()) {
            return;
        }

        if (message.active) {
            final double range = HardcoreRevivalConfig.getActive().rescueDistance;
            List<Player> candidates = player.level().getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(range), p -> {
                if (p == null || !HardcoreRevival.getRevivalData(p).isKnockedOut()) {
                    return false;
                }

                if (!player.hasLineOfSight(p)) {
                    return false;
                }

                return isLookingTowards(player, p);
            });

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
}