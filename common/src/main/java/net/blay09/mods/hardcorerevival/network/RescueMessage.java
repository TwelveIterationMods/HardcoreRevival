package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class RescueMessage {
    private final int targetEntityId;

    public RescueMessage(int targetEntityId) {
        this.targetEntityId = targetEntityId;
    }

    public static void encode(RescueMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.targetEntityId);
    }

    public static RescueMessage decode(FriendlyByteBuf buf) {
        int targetEntityId = buf.readInt();
        return new RescueMessage(targetEntityId);
    }

    public static void handle(ServerPlayer player, RescueMessage message) {
        if (player == null || !player.isAlive() || player.isSpectator() || HardcoreRevival.getRevivalData(player).isKnockedOut()) {
            return;
        }

        if (message.targetEntityId < 0) {
            HardcoreRevival.getManager().abortRescue(player);
            return;
        }

        Entity targetEntity = player.level().getEntity(message.targetEntityId);
        if (!(targetEntity instanceof Player target) || !HardcoreRevival.getRevivalData(target).isKnockedOut()) {
            HardcoreRevival.getManager().abortRescue(player);
            return;
        }

        if (target.distanceTo(player) > HardcoreRevivalConfig.getActive().rescueDistance + 0.5f) {
            HardcoreRevival.getManager().abortRescue(player);
            return;
        }

        HardcoreRevival.getManager().startRescue(player, target);
    }
}
