package net.blay09.mods.hardcorerevival.capability;

import net.minecraft.world.entity.player.Player;

public class HardcoreRevivalDataImpl implements HardcoreRevivalData {
	private boolean knockedOut;
	private int knockoutTicksPassed;
	private long logoutWorldTime;
	private int rescueTime;
	private Player rescueTarget;

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
	public void setLogoutWorldTime(long logoutWorldTime) {
		this.logoutWorldTime = logoutWorldTime;
	}

	@Override
	public long getLogoutWorldTime() {
		return logoutWorldTime;
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
	public void setRescueTarget(Player rescueTarget) {
		this.rescueTarget = rescueTarget;
	}

	@Override
	public Player getRescueTarget() {
		return rescueTarget;
	}
}
