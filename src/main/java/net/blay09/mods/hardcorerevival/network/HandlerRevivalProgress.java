package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class HandlerRevivalProgress implements IMessageHandler<MessageRevivalProgress, IMessage> {
	@Override
	@Nullable
	public IMessage onMessage(MessageRevivalProgress message, MessageContext ctx) {
		NetworkHandler.getThreadListener(ctx).addScheduledTask(() -> HardcoreRevival.proxy.receiveRevivalProgress(message.getEntityId(), message.getProgress()));
		return null;
	}
}
