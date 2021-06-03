package net.blay09.mods.hardcorerevival.capability;

import net.minecraft.entity.player.PlayerEntity;

public class HardcoreRevivalDataImpl implements HardcoreRevivalData {
	private boolean knockedOut;
	private int knockoutTicksPassed;
	private int rescueTime;
	private PlayerEntity rescueTarget;

	@Override
	public void setKnockedOut(boolean knockedOut) {
		this.knockedOut = knockedOut;
	}

	@Override
	public boolean isKnockedOut() {
		return knockedOut;
	}

	@Override
	public void setKnockoutTicksPassed(int knockoutTicksPassed) {
		this.knockoutTicksPassed = knockoutTicksPassed;
	}

	@Override
	public int getKnockoutTicksPassed() {
		return knockoutTicksPassed;
	}

	@Override
	public void setRescueTime(int rescueTime) {
		this.rescueTime = rescueTime;
	}

	@Override
	public int getRescueTime() {
		return rescueTime;
	}

	@Override
	public void setRescueTarget(PlayerEntity rescueTarget) {
		this.rescueTarget = rescueTarget;
	}

	@Override
	public PlayerEntity getRescueTarget() {
		return rescueTarget;
	}
}
