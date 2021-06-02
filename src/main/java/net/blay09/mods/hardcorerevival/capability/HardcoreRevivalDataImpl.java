package net.blay09.mods.hardcorerevival.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class HardcoreRevivalDataImpl implements IHardcoreRevivalData {
	private boolean knockedOut;
	private BlockPos knockoutPos;
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
	public void setKnockoutTicksPassed(int knockoutTime) {
		this.knockoutTicksPassed = knockoutTime;
	}

	@Override
	public int getKnockoutTicksPassed() {
		return knockoutTicksPassed;
	}

	@Override
	public void setKnockoutPos(BlockPos pos) {
		knockoutPos = pos;
	}

	@Override
	public BlockPos getKnockoutPos() {
		return knockoutPos;
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
