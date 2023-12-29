package net.blay09.mods.hardcorerevival.api;

import net.blay09.mods.balm.api.event.BalmEvent;
import net.minecraft.world.entity.player.Player;

public class PlayerRescuedEvent extends BalmEvent {
	private final Player player;
	private final Player rescuer;

	public PlayerRescuedEvent(Player player, Player rescuer) {
		this.player = player;
		this.rescuer = rescuer;
	}

	public Player getPlayer() {
		return player;
	}

	public Player getRescuer() {
		return rescuer;
	}
}
