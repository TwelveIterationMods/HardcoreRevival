package net.blay09.mods.hardcorerevival.api;

import net.blay09.mods.balm.platform.event.BidirectionalEventMapper;
import net.blay09.mods.balm.platform.event.EventMapper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public final class PlayerAboutToKnockOutEvent {

    public static final BidirectionalEventMapper<Consumer<PlayerAboutToKnockOutEvent>> EVENT = EventMapper.createBound(PlayerAboutToKnockOutEvent.class);
    private final Player player;
    private final DamageSource source;

    public PlayerAboutToKnockOutEvent(Player player, DamageSource source) {
        this.player = player;
        this.source = source;
    }

    private boolean canceled;

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public Player player() {
        return player;
    }

    public DamageSource source() {
        return source;
    }

}
