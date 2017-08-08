package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.hardcorerevival.ModConfig;
import net.blay09.mods.hardcorerevival.capability.CapabilityHardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.IHardcoreRevival;
import net.blay09.mods.hardcorerevival.network.MessageDeathTime;
import net.blay09.mods.hardcorerevival.network.MessageDie;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerHandler {
	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if(event.player instanceof EntityPlayerMP) {
			NBTTagCompound data = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
			IHardcoreRevival revival = event.player.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
			if(revival != null) {
				CapabilityHardcoreRevival.REVIVAL_CAPABILITY.readNBT(revival, null, data.getCompoundTag("HardcoreRevival"));
			}

			NetworkHandler.instance.sendTo(new MessageDeathTime(revival != null ? revival.getDeathTime() : 0), (EntityPlayerMP) event.player);
			if(revival != null && revival.getDeathTime() >= ModConfig.maxDeathTicks) {
				NetworkHandler.instance.sendTo(new MessageDie(), (EntityPlayerMP) event.player);
			}
		}
	}

	@SubscribeEvent
	public void onCapabilityInject(AttachCapabilitiesEvent<Entity> event) {
		if(event.getObject() instanceof EntityPlayerMP) {
			event.addCapability(CapabilityHardcoreRevival.REGISTRY_NAME, new ICapabilityProvider() {
				private IHardcoreRevival revival;

				@Override
				public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
					return true;
				}

				@Nullable
				@Override
				@SuppressWarnings("unchecked")
				public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
					if (capability == CapabilityHardcoreRevival.REVIVAL_CAPABILITY) {
						if (revival == null) {
							revival = CapabilityHardcoreRevival.REVIVAL_CAPABILITY.getDefaultInstance();
						}
						return (T) revival;
					}
					return null;
				}
			});
		}
	}

	@SubscribeEvent
	public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		NBTTagCompound data = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
		IHardcoreRevival revival = event.player.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
		if(revival != null) {
			NBTBase tag = CapabilityHardcoreRevival.REVIVAL_CAPABILITY.writeNBT(revival, null);
			if (tag != null) {
				data.setTag("HardcoreRevival", tag);
				event.player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
			}
		}
	}
}
