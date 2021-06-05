package net.blay09.mods.hardcorerevival.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface HardcoreRevivalData {
	void setKnockedOut(boolean knockedOut);
	boolean isKnockedOut();
	void setKnockoutTicksPassed(int knockoutTicksPassed);
	int getKnockoutTicksPassed();
	void setLogoutWorldTime(long logoutWorldTime);
	long getLogoutWorldTime();
	void setRescueTime(int rescueTime);
	int getRescueTime();
	void setRescueTarget(@Nullable PlayerEntity rescueTarget);
	@Nullable
	PlayerEntity getRescueTarget();
}
