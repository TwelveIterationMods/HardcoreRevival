package net.blay09.mods.hardcorerevival.capability;

import net.minecraft.entity.player.PlayerEntity;

public class HardcoreRevivalImpl implements IHardcoreRevival {
	private int deathTime;
	private int rescueTime;
	private PlayerEntity rescueTarget;

	@Override
	public void setDeathTime(int deathTime) {
		this.deathTime = deathTime;
	}

	@Override
	public int getDeathTime() {
		return deathTime;
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
