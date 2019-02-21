package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkHandler {
    public static final SimpleChannel channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(HardcoreRevival.MOD_ID, "network"), () -> "1.0", it -> true, it -> true);

    public static void init() {
        channel.registerMessage(0, MessageDeathTime.class, MessageDeathTime::encode, MessageDeathTime::decode, MessageDeathTime::handle);
        channel.registerMessage(1, MessageRevivalSuccess.class, MessageRevivalSuccess::encode, MessageRevivalSuccess::decode, MessageRevivalSuccess::handle);
        channel.registerMessage(2, MessageRevival.class, MessageRevival::encode, MessageRevival::decode, MessageRevival::handle);
        channel.registerMessage(3, MessageRevivalProgress.class, MessageRevivalProgress::encode, MessageRevivalProgress::decode, MessageRevivalProgress::handle);
        channel.registerMessage(4, MessageDie.class, (message, buf) -> {}, it -> new MessageDie(), MessageDie::handle);
    }
}
