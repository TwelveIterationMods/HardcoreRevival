package net.blay09.mods.firstaid.network;

import net.blay09.mods.firstaid.ModConfig;
import net.blay09.mods.firstaid.capability.CapabilityFirstAid;
import net.blay09.mods.firstaid.capability.IFirstAid;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class HandlerDie implements IMessageHandler<MessageDie, IMessage> {
	@Override
	@Nullable
	public IMessage onMessage(MessageDie message, MessageContext ctx) {
		NetworkHandler.getThreadListener(ctx).addScheduledTask(() -> {
			IFirstAid firstAid = ctx.getServerHandler().player.getCapability(CapabilityFirstAid.FIRST_AID_CAPABILITY, null);
			if(firstAid != null) {
				firstAid.setDeathTime(ModConfig.maxDeathTicks);
			}
		});
		return null;
	}
}
