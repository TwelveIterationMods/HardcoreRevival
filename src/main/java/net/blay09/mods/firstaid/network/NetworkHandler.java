package net.blay09.mods.firstaid.network;

import net.blay09.mods.firstaid.FirstAid;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NetworkHandler {
	public static final SimpleNetworkWrapper instance = NetworkRegistry.INSTANCE.newSimpleChannel(FirstAid.MOD_ID);

	public static void init() {
		instance.registerMessage(HandlerDeathTime.class, MessageDeathTime.class, 0, Side.CLIENT);
		instance.registerMessage(HandlerFirstAidSuccess.class, MessageFirstAidSuccess.class, 1, Side.CLIENT);
		instance.registerMessage(HandlerFirstAid.class, MessageFirstAid.class, 2, Side.SERVER);
		instance.registerMessage(HandlerFirstAidProgress.class, MessageFirstAidProgress.class, 3, Side.CLIENT);
		instance.registerMessage(HandlerDie.class, MessageDie.class, 4, Side.SERVER);
	}

	public static IThreadListener getThreadListener(MessageContext ctx) {
		return ctx.side == Side.SERVER ? (WorldServer) ctx.getServerHandler().player.world : getClientThreadListener();
	}

	@SideOnly(Side.CLIENT)
	public static IThreadListener getClientThreadListener() {
		return Minecraft.getMinecraft();
	}

}
