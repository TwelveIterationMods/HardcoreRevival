package net.blay09.mods.hardcorerevival.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class HardcoreRevivalDataImpl implements HardcoreRevivalData {
	private static final String KNOCKED_OUT = "KnockedOut";
	private static final String KNOCKOUT_TICKS_PASSED = "KnockoutTicksPassed";
	private static final String LAST_KNOCKOUT_AT = "LastKnockoutAt";
	private static final String LAST_LOGOUT_AT = "LastLogoutAt";

	private boolean knockedOut;
	private int knockoutTicksPassed;
	private long lastKnockoutAt;
	private long lastLogoutAt;
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
	public void setLastKnockoutAt(long lastKnockoutAt) {
		this.lastKnockoutAt = lastKnockoutAt;
	}

	@Override
	public long getLastKnockoutAt() {
		return lastKnockoutAt;
	}

	@Override
	public void setLastLogoutAt(long lastLogoutAt) {
		this.lastLogoutAt = lastLogoutAt;
	}

	@Override
	public long getLastLogoutAt() {
		return lastLogoutAt;
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

	@Override
	public CompoundTag serialize() {
		CompoundTag tagCompound = new CompoundTag();
		tagCompound.putBoolean(KNOCKED_OUT, isKnockedOut());
		tagCompound.putInt(KNOCKOUT_TICKS_PASSED, getKnockoutTicksPassed());
		tagCompound.putLong(LAST_LOGOUT_AT, getLastLogoutAt());
		tagCompound.putLong(LAST_KNOCKOUT_AT, getLastKnockoutAt());
		return tagCompound;
	}

	@Override
	public void deserialize(CompoundTag tag) {
		setKnockedOut(tag.getBoolean(KNOCKED_OUT));
		setKnockoutTicksPassed(tag.getInt(KNOCKOUT_TICKS_PASSED));
		setLastLogoutAt(tag.getLong(LAST_LOGOUT_AT));
		setLastKnockoutAt(tag.getLong(LAST_KNOCKOUT_AT));
	}
}
