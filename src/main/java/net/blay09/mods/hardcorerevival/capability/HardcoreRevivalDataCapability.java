package net.blay09.mods.hardcorerevival.capability;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class HardcoreRevivalDataCapability {
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(HardcoreRevival.MOD_ID, "revival_data");

    @CapabilityInject(HardcoreRevivalData.class)
    public static Capability<HardcoreRevivalData> REVIVAL_CAPABILITY = null;

    private static final String KNOCKED_OUT = "KnockedOut";
    private static final String KNOCKOUT_TICKS_PASSED = "KnockoutTicksPassed";
    private static final String LOGOUT_WORLD_TIME = "LogoutWorldTime";

    public static void register() {
        CapabilityManager.INSTANCE.register(HardcoreRevivalData.class, new Capability.IStorage<HardcoreRevivalData>() {
            @Override
            public INBT writeNBT(Capability<HardcoreRevivalData> capability, HardcoreRevivalData instance, Direction side) {
                CompoundNBT tagCompound = new CompoundNBT();
                tagCompound.putBoolean(KNOCKED_OUT, instance.isKnockedOut());
                tagCompound.putInt(KNOCKOUT_TICKS_PASSED, instance.getKnockoutTicksPassed());
                tagCompound.putLong(LOGOUT_WORLD_TIME, instance.getLogoutWorldTime());
                return tagCompound;
            }

            @Override
            public void readNBT(Capability<HardcoreRevivalData> capability, HardcoreRevivalData instance, Direction side, INBT base) {
                CompoundNBT tagCompound = (CompoundNBT) base;
                instance.setKnockedOut(tagCompound.getBoolean(KNOCKED_OUT));
                instance.setKnockoutTicksPassed(tagCompound.getInt(KNOCKOUT_TICKS_PASSED));
                instance.setLogoutWorldTime(tagCompound.getLong(LOGOUT_WORLD_TIME));
            }
        }, HardcoreRevivalDataImpl::new);
    }
}
