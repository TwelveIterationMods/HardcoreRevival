package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.balm.network.BalmNetworking;

public class ModNetworking {
    public static void initialize(BalmNetworking networking) {
        networking.registerClientboundPacket(HardcoreRevivalDataMessage.TYPE,
                HardcoreRevivalDataMessage.class,
                HardcoreRevivalDataMessage.STREAM_CODEC,
                HardcoreRevivalDataMessage::handle);
        networking.registerClientboundPacket(RevivalSuccessMessage.TYPE,
                RevivalSuccessMessage.class,
                RevivalSuccessMessage.STREAM_CODEC,
                RevivalSuccessMessage::handle);
        networking.registerClientboundPacket(RevivalProgressMessage.TYPE,
                RevivalProgressMessage.class,
                RevivalProgressMessage.STREAM_CODEC,
                RevivalProgressMessage::handle);
        networking.registerServerboundPacket(RescueMessage.TYPE, RescueMessage.class, RescueMessage.STREAM_CODEC, RescueMessage::handle);
        networking.registerServerboundPacket(AcceptFateMessage.TYPE, AcceptFateMessage.class, AcceptFateMessage.STREAM_CODEC, AcceptFateMessage::handle);
    }
}
