package net.blay09.mods.hardcorerevival.capability;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityHardcoreRevival {
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(HardcoreRevival.MOD_ID, "revival_data");

    @CapabilityInject(IHardcoreRevivalData.class)
    public static Capability<IHardcoreRevivalData> REVIVAL_CAPABILITY = null;

    private static final String KNOCKED_OUT = "KnockedOut";
    private static final String KNOCKOUT_POS = "KnockoutPos";
    private static final String KNOCKOUT_TICKS_PASSED = "KnockoutTicksPassed";

    public static void register() {
        CapabilityManager.INSTANCE.register(IHardcoreRevivalData.class, new Capability.IStorage<IHardcoreRevivalData>() {
            @Override
            public INBT writeNBT(Capability<IHardcoreRevivalData> capability, IHardcoreRevivalData instance, Direction side) {
                CompoundNBT tagCompound = new CompoundNBT();
                tagCompound.putBoolean(KNOCKED_OUT, instance.isKnockedOut());
                tagCompound.putLong(KNOCKOUT_POS, instance.getKnockoutPos().toLong());
                tagCompound.putInt(KNOCKOUT_TICKS_PASSED, instance.getKnockoutTicksPassed());
                return tagCompound;
            }

            @Override
            public void readNBT(Capability<IHardcoreRevivalData> capability, IHardcoreRevivalData instance, Direction side, INBT base) {
                CompoundNBT tagCompound = (CompoundNBT) base;
                instance.setKnockedOut(tagCompound.getBoolean(KNOCKED_OUT));
                instance.setKnockoutTicksPassed(tagCompound.getInt(KNOCKOUT_TICKS_PASSED));
                instance.setKnockoutPos(BlockPos.fromLong(tagCompound.getLong(KNOCKOUT_POS)));
            }
        }, HardcoreRevivalDataImpl::new);
    }
}
