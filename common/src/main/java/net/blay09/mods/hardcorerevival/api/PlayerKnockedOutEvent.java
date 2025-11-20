package net.blay09.mods.hardcorerevival.api;

import net.blay09.mods.balm.platform.event.BidirectionalEventMapper;
import net.blay09.mods.balm.platform.event.EventMapper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public record PlayerKnockedOutEvent(Player player, DamageSource source) {

    public static final BidirectionalEventMapper<Consumer<PlayerKnockedOutEvent>> EVENT = EventMapper.createBound(PlayerKnockedOutEvent.class);

}
