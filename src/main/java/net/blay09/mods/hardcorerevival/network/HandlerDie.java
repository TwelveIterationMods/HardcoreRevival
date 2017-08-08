package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.ModConfig;
import net.blay09.mods.hardcorerevival.capability.CapabilityHardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.IHardcoreRevival;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;

public class HandlerDie implements IMessageHandler<MessageDie, IMessage> {
	@Override
	@Nullable
	public IMessage onMessage(MessageDie message, MessageContext ctx) {
		NetworkHandler.getThreadListener(ctx).addScheduledTask(() -> {
			if(ctx.side == Side.SERVER) {
				IHardcoreRevival revival = ctx.getServerHandler().player.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
				if (revival != null) {
					revival.setDeathTime(ModConfig.maxDeathTicks);
				}
			} else {
				HardcoreRevival.proxy.receiveDeath();
			}
		});
		return null;
	}
}
