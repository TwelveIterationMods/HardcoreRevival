package net.blay09.mods.hardcorerevival.handler;


import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.*;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.HardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.mixin.LivingEntityAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;


public class KnockoutHandler {

    public static void initialize() {
        Balm.getEvents().onEvent(LivingDamageEvent.class, KnockoutHandler::onPlayerDamage);
        Balm.getEvents().onEvent(PlayerDeathEvent.class, KnockoutHandler::onPlayerDeath);
        Balm.getEvents().onEvent(PlayerRespawnEvent.class, KnockoutHandler::onPlayerRespawn);

        Balm.getEvents().onTickEvent(TickType.ServerPlayer, TickPhase.Start, KnockoutHandler::onPlayerTick);
    }

    public static void onPlayerDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DamageSource damageSource = event.getDamageSource();

            if (HardcoreRevival.getRevivalData(event.getEntity()).isKnockedOut()) {
                Entity attacker = damageSource.getEntity();
                if (attacker instanceof Mob mob) {
                    mob.setTarget(null);
                }
                if (!damageSource.isBypassInvul() && damageSource != HardcoreRevivalManager.notRescuedInTime) {
                    event.setCanceled(true);
                }
                return;
            }

            boolean canDamageSourceKnockout = damageSource != DamageSource.OUT_OF_WORLD && damageSource != HardcoreRevivalManager.notRescuedInTime;
            if (canDamageSourceKnockout && player.getHealth() - event.getDamageAmount() <= 0f) {
                // Reduce damage to prevent the player from dying
                event.setDamageAmount(Math.min(event.getDamageAmount(), Math.max(0f, player.getHealth() - 1f)));

                // Trigger knockout for this player, if totem does not protect player
                if (((LivingEntityAccessor) player).callCheckTotemDeathProtection(damageSource)) {
                    event.setCanceled(true);
                } else {
                    HardcoreRevival.getManager().knockout(player, damageSource);
                }
            }
        }
    }

    public static void onPlayerTick(ServerPlayer player) {
        //if (event.phase == TickEvent.Phase.START && event.side == LogicalSide.SERVER) {
        HardcoreRevivalData revivalData = HardcoreRevival.getRevivalData(player);
        if (revivalData.isKnockedOut() && player.isAlive()) {
            // Make sure health stays locked at half a heart
            player.setHealth(1f);

            revivalData.setKnockoutTicksPassed(revivalData.getKnockoutTicksPassed() + 1);

            int maxTicksUntilDeath = HardcoreRevivalConfig.getActive().ticksUntilDeath;
            if (maxTicksUntilDeath > 0 && revivalData.getKnockoutTicksPassed() >= maxTicksUntilDeath) {
                HardcoreRevival.getManager().notRescuedInTime(player);
            }
        }
    }

    public static void onPlayerDeath(PlayerDeathEvent event) {
        HardcoreRevival.getManager().reset(event.getPlayer());
    }

    public static void onPlayerRespawn(PlayerRespawnEvent event) {
        HardcoreRevival.getManager().reset(event.getNewPlayer());
    }


}
