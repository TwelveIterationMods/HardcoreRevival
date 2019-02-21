package net.blay09.mods.hardcorerevival;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.eventbus.api.Event;

public class PlayerKnockedOutEvent extends Event {
	private final EntityPlayer player;
	private final DamageSource source;

	public PlayerKnockedOutEvent(EntityPlayer player, DamageSource source) {
		this.player = player;
		this.source = source;
	}

	public EntityPlayer getPlayer() {
		return player;
	}

	public DamageSource getSource() {
		return source;
	}
}
