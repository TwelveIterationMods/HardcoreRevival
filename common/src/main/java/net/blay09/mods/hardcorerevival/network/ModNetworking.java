package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.balm.api.network.BalmNetworking;

public class ModNetworking {
    public static void initialize(BalmNetworking networking) {
        networking.registerClientboundPacket(HardcoreRevivalDataMessage.TYPE,
                HardcoreRevivalDataMessage.class,
                HardcoreRevivalDataMessage::encode,
                HardcoreRevivalDataMessage::decode,
                HardcoreRevivalDataMessage::handle);
        networking.registerClientboundPacket(RevivalSuccessMessage.TYPE,
                RevivalSuccessMessage.class,
                RevivalSuccessMessage::encode,
                RevivalSuccessMessage::decode,
                RevivalSuccessMessage::handle);
        networking.registerClientboundPacket(RevivalProgressMessage.TYPE,
                RevivalProgressMessage.class,
                RevivalProgressMessage::encode,
                RevivalProgressMessage::decode,
                RevivalProgressMessage::handle);
        networking.registerServerboundPacket(RescueMessage.TYPE, RescueMessage.class, RescueMessage::encode, RescueMessage::decode, RescueMessage::handle);
        networking.registerServerboundPacket(AcceptFateMessage.TYPE, AcceptFateMessage.class, (message, buf) -> {
        }, it -> new AcceptFateMessage(), AcceptFateMessage::handle);
    }
}
