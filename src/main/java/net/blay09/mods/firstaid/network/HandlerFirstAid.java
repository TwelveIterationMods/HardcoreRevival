package net.blay09.mods.firstaid.network;

import net.blay09.mods.firstaid.ModConfig;
import net.blay09.mods.firstaid.handler.RescueHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.List;

public class HandlerFirstAid implements IMessageHandler<MessageFirstAid, IMessage> {
	@Override
	@Nullable
	public IMessage onMessage(MessageFirstAid message, MessageContext ctx) {
		NetworkHandler.getThreadListener(ctx).addScheduledTask(() -> {
			EntityPlayer player = ctx.getServerHandler().player;
			if(player.getHealth() <= 0) {
				return;
			}
			if(message.isActive()) {
				final float range = ModConfig.maxRescueDist;
				List<EntityPlayer> candidates = player.world.getEntitiesWithinAABB(EntityPlayer.class, player.getEntityBoundingBox().grow(range), p -> p != null && p.getHealth() <= 0f);
				float minDist = Float.MAX_VALUE;
				EntityPlayer target = null;
				for (EntityPlayer candidate : candidates) {
					float dist = candidate.getDistanceToEntity(player);
					if (dist < minDist) {
						target = candidate;
						minDist = dist;
					}
				}
				if (target != null) {
					RescueHandler.startRescue(player, target);
				}
			} else {
				RescueHandler.abortRescue(player);
			}
		});
		return null;
	}
}
