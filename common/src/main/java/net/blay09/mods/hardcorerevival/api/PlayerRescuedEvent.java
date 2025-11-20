package net.blay09.mods.hardcorerevival.api;

import net.blay09.mods.balm.platform.event.BidirectionalEventMapper;
import net.blay09.mods.balm.platform.event.EventMapper;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public record PlayerRescuedEvent(Player player, Player rescuer) {

    public static final BidirectionalEventMapper<Consumer<PlayerRescuedEvent>> EVENT = EventMapper.createBound(PlayerRescuedEvent.class);

}
