package net.blay09.mods.firstaid.capability;

import net.minecraft.entity.player.EntityPlayer;

public class FirstAidImpl implements IFirstAid {
	private int deathTime;
	private int rescueTime;
	private EntityPlayer rescueTarget;

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
	public void setRescueTarget(EntityPlayer rescueTarget) {
		this.rescueTarget = rescueTarget;
	}

	@Override
	public EntityPlayer getRescueTarget() {
		return rescueTarget;
	}
}
