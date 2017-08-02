package net.blay09.mods.firstaid.handler;

import net.blay09.mods.firstaid.capability.CapabilityFirstAid;
import net.blay09.mods.firstaid.capability.IFirstAid;
import net.blay09.mods.firstaid.network.MessageDeathTime;
import net.blay09.mods.firstaid.network.NetworkHandler;
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
			IFirstAid firstAid = event.player.getCapability(CapabilityFirstAid.FIRST_AID_CAPABILITY, null);
			if(firstAid != null) {
				CapabilityFirstAid.FIRST_AID_CAPABILITY.readNBT(firstAid, null, data.getCompoundTag("FirstAid"));
			}

			NetworkHandler.instance.sendTo(new MessageDeathTime(firstAid != null ? firstAid.getDeathTime() : 0), (EntityPlayerMP) event.player);
		}
	}

	@SubscribeEvent
	public void onCapabilityInject(AttachCapabilitiesEvent<Entity> event) {
		if(event.getObject() instanceof EntityPlayerMP) {
			event.addCapability(CapabilityFirstAid.REGISTRY_NAME, new ICapabilityProvider() {
				private IFirstAid firstAid;

				@Override
				public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
					return true;
				}

				@Nullable
				@Override
				@SuppressWarnings("unchecked")
				public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
					if (capability == CapabilityFirstAid.FIRST_AID_CAPABILITY) {
						if (firstAid == null) {
							firstAid = CapabilityFirstAid.FIRST_AID_CAPABILITY.getDefaultInstance();
						}
						return (T) firstAid;
					}
					return null;
				}
			});
		}
	}

	@SubscribeEvent
	public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		NBTTagCompound data = event.player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
		IFirstAid firstAid = event.player.getCapability(CapabilityFirstAid.FIRST_AID_CAPABILITY, null);
		if(firstAid != null) {
			NBTBase tag = CapabilityFirstAid.FIRST_AID_CAPABILITY.writeNBT(firstAid, null);
			if (tag != null) {
				data.setTag("FirstAid", tag);
				event.player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
			}
		}
	}
}
