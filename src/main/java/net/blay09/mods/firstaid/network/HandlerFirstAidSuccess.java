package net.blay09.mods.firstaid.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class HandlerFirstAidSuccess implements IMessageHandler<MessageFirstAidSuccess, IMessage> {
	@Override
	@Nullable
	public IMessage onMessage(MessageFirstAidSuccess message, MessageContext ctx) {
		NetworkHandler.getThreadListener(ctx).addScheduledTask(() -> {
			Minecraft mc = Minecraft.getMinecraft();
			if(message.getEntityId() == mc.player.getEntityId()) {
				mc.displayGuiScreen(null);
			}
			Entity entity = mc.world.getEntityByID(message.getEntityId());
			if(entity != null) {
				mc.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, entity.posX, entity.posY, entity.posZ, 0, 0, 0);
			}
		});
		return null;
	}
}
