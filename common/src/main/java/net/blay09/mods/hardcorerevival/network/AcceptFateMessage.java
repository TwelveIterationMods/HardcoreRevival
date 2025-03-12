package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.HardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class AcceptFateMessage implements CustomPacketPayload {

    public static final AcceptFateMessage INSTANCE = new AcceptFateMessage();
    public static final CustomPacketPayload.Type<AcceptFateMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HardcoreRevival.MOD_ID,
            "accept_fate"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AcceptFateMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private AcceptFateMessage() {
    }

    public static void handle(ServerPlayer player, AcceptFateMessage message) {
        if (!HardcoreRevivalConfig.getActive().allowAcceptingFate) {
            return;
        }

        HardcoreRevivalManager.notRescuedInTime(player);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
