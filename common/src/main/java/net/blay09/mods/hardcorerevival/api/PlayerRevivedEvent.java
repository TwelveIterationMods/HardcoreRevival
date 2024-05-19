package net.blay09.mods.hardcorerevival.api;

import net.blay09.mods.balm.api.event.BalmEvent;
import net.minecraft.world.entity.player.Player;

public class PlayerRevivedEvent extends BalmEvent {
    private final Player player;

    public PlayerRevivedEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

}