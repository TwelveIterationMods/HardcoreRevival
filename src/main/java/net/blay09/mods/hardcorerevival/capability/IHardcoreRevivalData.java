package net.blay09.mods.hardcorerevival.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface IHardcoreRevivalData {
	void setKnockedOut(boolean knockedOut);
	boolean isKnockedOut();
	void setKnockoutTicksPassed(int knockoutTime);
	int getKnockoutTicksPassed();
	void setKnockoutPos(BlockPos pos);
	BlockPos getKnockoutPos();
	void setRescueTime(int rescueTime);
	int getRescueTime();
	void setRescueTarget(@Nullable PlayerEntity rescueTarget);
	@Nullable
	PlayerEntity getRescueTarget();
}
