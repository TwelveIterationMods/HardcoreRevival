package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.HardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.PlayerHardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class RescueMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RescueMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HardcoreRevival.MOD_ID,
            "rescue"));

    private final int targetEntityId;

    public RescueMessage(int targetEntityId) {
        this.targetEntityId = targetEntityId;
    }

    public static void encode(FriendlyByteBuf buf, RescueMessage message) {
        buf.writeInt(message.targetEntityId);
    }

    public static RescueMessage decode(FriendlyByteBuf buf) {
        int targetEntityId = buf.readInt();
        return new RescueMessage(targetEntityId);
    }

    public static void handle(ServerPlayer player, RescueMessage message) {
        if (player == null || !player.isAlive() || player.isSpectator() || PlayerHardcoreRevivalManager.isKnockedOut(player)) {
            return;
        }

        if (message.targetEntityId < 0) {
            HardcoreRevivalManager.abortRescue(player);
            return;
        }

        Entity targetEntity = player.level().getEntity(message.targetEntityId);
        if (!(targetEntity instanceof Player target) || !PlayerHardcoreRevivalManager.isKnockedOut(target)) {
            HardcoreRevivalManager.abortRescue(player);
            return;
        }

        if (target.distanceTo(player) > HardcoreRevivalConfig.getActive().rescueDistance + 0.5f) {
            HardcoreRevivalManager.abortRescue(player);
            return;
        }

        HardcoreRevivalManager.startRescue(player, target);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
