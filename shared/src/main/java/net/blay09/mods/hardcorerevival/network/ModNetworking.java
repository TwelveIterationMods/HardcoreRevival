package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.balm.api.network.BalmNetworking;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.resources.ResourceLocation;

public class ModNetworking {
    public static void initialize(BalmNetworking networking) {
        networking.registerClientboundPacket(id("revival_data"), HardcoreRevivalDataMessage.class, HardcoreRevivalDataMessage::encode, HardcoreRevivalDataMessage::decode, HardcoreRevivalDataMessage::handle);
        networking.registerServerboundPacket(id("revival_success"), RevivalSuccessMessage.class, RevivalSuccessMessage::encode, RevivalSuccessMessage::decode, RevivalSuccessMessage::handle);
        networking.registerServerboundPacket(id("rescue"), RescueMessage.class, RescueMessage::encode, RescueMessage::decode, RescueMessage::handle);
        networking.registerClientboundPacket(id("revival_progress"), RevivalProgressMessage.class, RevivalProgressMessage::encode, RevivalProgressMessage::decode, RevivalProgressMessage::handle);
        networking.registerServerboundPacket(id("accept_fate"), AcceptFateMessage.class, (message, buf) -> {}, it -> new AcceptFateMessage(), AcceptFateMessage::handle);
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(HardcoreRevival.MOD_ID, path);
    }
}
