package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkHandler {
    public static final SimpleChannel channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(HardcoreRevival.MOD_ID, "network"), () -> "1.0", it -> true, it -> true);

    public static void init() {
        channel.registerMessage(0, HardcoreRevivalDataMessage.class, HardcoreRevivalDataMessage::encode, HardcoreRevivalDataMessage::decode, HardcoreRevivalDataMessage::handle);
        channel.registerMessage(1, RevivalSuccessMessage.class, RevivalSuccessMessage::encode, RevivalSuccessMessage::decode, RevivalSuccessMessage::handle);
        channel.registerMessage(2, RescueMessage.class, RescueMessage::encode, RescueMessage::decode, RescueMessage::handle);
        channel.registerMessage(3, RevivalProgressMessage.class, RevivalProgressMessage::encode, RevivalProgressMessage::decode, RevivalProgressMessage::handle);
        channel.registerMessage(4, AcceptFateMessage.class, (message, buf) -> {}, it -> new AcceptFateMessage(), AcceptFateMessage::handle);
        channel.registerMessage(5, HardcoreRevivalConfigMessage.class, HardcoreRevivalConfigMessage::encode, HardcoreRevivalConfigMessage::decode, HardcoreRevivalConfigMessage::handle);
    }

    public static void sendToPlayer(PlayerEntity player, Object message) {
        NetworkHandler.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), message);
    }

    public static void ensureServerSide(NetworkEvent.Context context) {
        if (context.getDirection() != NetworkDirection.PLAY_TO_SERVER) {
            throw new UnsupportedOperationException("Expected packet side does not match; expected client");
        }
    }

    public static void ensureClientSide(NetworkEvent.Context context) {
        if (context.getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
            throw new UnsupportedOperationException("Expected packet side does not match; expected client");
        }
    }
}
