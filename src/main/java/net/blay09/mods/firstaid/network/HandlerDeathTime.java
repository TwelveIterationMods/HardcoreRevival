package net.blay09.mods.firstaid.network;

import net.blay09.mods.firstaid.FirstAid;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class HandlerDeathTime implements IMessageHandler<MessageDeathTime, IMessage> {
	@Override
	@Nullable
	public IMessage onMessage(MessageDeathTime message, MessageContext ctx) {
		NetworkHandler.getThreadListener(ctx).addScheduledTask(() -> FirstAid.proxy.receiveDeathTime(message.getDeathTime()));
		return null;
	}
}
