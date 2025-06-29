package net.blay09.mods.hardcorerevival.api;

import net.blay09.mods.balm.api.event.BalmEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

public class PlayerKnockedOutEvent extends BalmEvent {
	private final Player player;
	private final DamageSource source;

	public PlayerKnockedOutEvent(Player player, DamageSource source) {
		this.player = player;
		this.source = source;
	}

	public Player getPlayer() {
		return player;
	}

	public DamageSource getSource() {
		return source;
	}
}
