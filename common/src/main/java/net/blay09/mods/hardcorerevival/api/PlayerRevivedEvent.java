package net.blay09.mods.hardcorerevival.api;

import net.blay09.mods.balm.platform.event.BidirectionalEventMapper;
import net.blay09.mods.balm.platform.event.EventMapper;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public record PlayerRevivedEvent(Player player) {
    public static final BidirectionalEventMapper<Consumer<PlayerRevivedEvent>> EVENT = EventMapper.createBound(PlayerRevivedEvent.class);
}