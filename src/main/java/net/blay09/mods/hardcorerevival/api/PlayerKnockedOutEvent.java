package net.blay09.mods.hardcorerevival.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.eventbus.api.Event;

public class PlayerKnockedOutEvent extends Event {
	private final PlayerEntity player;
	private final DamageSource source;

	public PlayerKnockedOutEvent(PlayerEntity player, DamageSource source) {
		this.player = player;
		this.source = source;
	}

	public PlayerEntity getPlayer() {
		return player;
	}

	public DamageSource getSource() {
		return source;
	}
}
