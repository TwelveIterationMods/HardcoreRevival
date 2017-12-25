package net.blay09.mods.hardcorerevival.network;

import net.blay09.mods.hardcorerevival.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class HandlerRevivalSuccess implements IMessageHandler<MessageRevivalSuccess, IMessage> {
	@Override
	@Nullable
	public IMessage onMessage(MessageRevivalSuccess message, MessageContext ctx) {
		NetworkHandler.getThreadListener(ctx).addScheduledTask(() -> {
			Minecraft mc = Minecraft.getMinecraft();
			if (message.getEntityId() == mc.player.getEntityId()) {
				mc.player.extinguish();
				mc.player.setFlag(0, false); // burning flag
				if (ModConfig.glowOnDeath) {
					mc.player.setGlowing(false);
					mc.player.setFlag(6, false); // glowing flag
				}
				mc.displayGuiScreen(null);
			}
			Entity entity = mc.world.getEntityByID(message.getEntityId());
			if (entity != null) {
				mc.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, entity.posX, entity.posY, entity.posZ, 0, 0, 0);
			}
		});
		return null;
	}
}
