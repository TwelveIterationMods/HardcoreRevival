package net.blay09.mods.firstaid.capability;

import net.blay09.mods.firstaid.FirstAid;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityFirstAid {
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(FirstAid.MOD_ID, "first_aid");
	@CapabilityInject(IFirstAid.class)
	public static Capability<IFirstAid> FIRST_AID_CAPABILITY = null;

	public static void register() {
		CapabilityManager.INSTANCE.register(IFirstAid.class, new Capability.IStorage<IFirstAid>() {
			@Override
			public NBTBase writeNBT(Capability<IFirstAid> capability, IFirstAid instance, EnumFacing side) {
				NBTTagCompound tagCompound = new NBTTagCompound();
				tagCompound.setInteger("DeathTime", instance.getDeathTime());
				return tagCompound;
			}

			@Override
			public void readNBT(Capability<IFirstAid> capability, IFirstAid instance, EnumFacing side, NBTBase base) {
				NBTTagCompound tagCompound = (NBTTagCompound) base;
				instance.setDeathTime(tagCompound.getInteger("DeathTime"));
			}
		}, FirstAidImpl::new);
	}
}
