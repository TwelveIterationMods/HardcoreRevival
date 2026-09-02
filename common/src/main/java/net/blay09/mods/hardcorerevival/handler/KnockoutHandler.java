package net.blay09.mods.hardcorerevival.handler;


import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.*;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.api.PlayerAboutToKnockOutEvent;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.HardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.tag.ModDamageTypeTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;


public class KnockoutHandler {

    public static void initialize() {
        Balm.getEvents().onEvent(LivingDamageEvent.class, KnockoutHandler::onPlayerDamage, EventPriority.High);
        Balm.getEvents().onEvent(LivingDeathEvent.class, KnockoutHandler::onPlayerDeath, EventPriority.High);
        Balm.getEvents().onEvent(PlayerRespawnEvent.class, KnockoutHandler::onPlayerRespawn);

        Balm.getEvents().onTickEvent(TickType.ServerPlayer, TickPhase.Start, KnockoutHandler::onPlayerTick);
    }

    public static void onPlayerDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && HardcoreRevival.getRevivalData(player).isKnockedOut()) {
            final var damageSource = event.getDamageSource();
            if (!damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !bypassesKnockout(player, damageSource)) {
                event.setCanceled(true);

                final var attacker = damageSource.getEntity();
                if (attacker instanceof Mob mob) {
                    mob.setTarget(null);
                }
            }
        }
    }

    public static void onPlayerDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) {
            final var damageSource = event.getDamageSource();

            if (HardcoreRevival.getRevivalData(entity).isKnockedOut()) {
                Entity attacker = damageSource.getEntity();
                if (attacker instanceof Mob mob) {
                    mob.setTarget(null);
                }
                if (!damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !bypassesKnockout(player, damageSource)) {
                    event.setCanceled(true);
                    player.setHealth(0.5f);
                }
                return;
            }

            if (!bypassesKnockout(player, damageSource) && isKnockoutEnabledFor(player, damageSource)) {
                final var aboutToKnockOutEvent = new PlayerAboutToKnockOutEvent(player, damageSource);
                Balm.getEvents().fireEvent(aboutToKnockOutEvent);
                if (aboutToKnockOutEvent.isCanceled()) {
                    return;
                }

                HardcoreRevival.getManager().knockout(player, damageSource);
                player.setHealth(0.5f);
                event.setCanceled(true);
            }
        }
    }

    private static boolean isKnockoutEnabledFor(ServerPlayer player, DamageSource damageSource) {
        final var server = player.getServer();
        if (server == null) {
            return false;
        }

        final var attacker = damageSource.getEntity();
        if (attacker != null) {
            final var entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(attacker.getType());
            if (HardcoreRevivalConfig.getActive().instantDeathEntityTypes.contains(Objects.toString(entityTypeId))) {
                return false;
            }
        }

        if (HardcoreRevivalConfig.getActive().disableInNonHardcore && !server.isHardcore()) {
            return false;
        } else if (HardcoreRevivalConfig.getActive().disableInSingleplayer && server.isSingleplayer() && server.getPlayerCount() == 1) {
            return false;
        } else if (HardcoreRevivalConfig.getActive().disableInLonelyMultiplayer && !server.isSingleplayer() && server.getPlayerCount() == 1) {
            return false;
        }

        return true;
    }

    private static boolean bypassesKnockout(ServerPlayer player, DamageSource damageSource) {
        if (damageSource.is(HardcoreRevivalManager.NOT_RESCUED_IN_TIME) || damageSource.is(ModDamageTypeTags.BYPASSES_KNOCKOUT)) {
            return true;
        }

        final var server = player.getServer();
        if (server == null) {
            return false;
        }

        final var damageSourceId = server.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getKey(damageSource.type());
        return HardcoreRevivalConfig.getActive().instantDeathSources.contains(Objects.toString(damageSourceId));
    }

    public static void onPlayerTick(ServerPlayer player) {
        //if (event.phase == TickEvent.Phase.START && event.side == LogicalSide.SERVER) {
        HardcoreRevivalData revivalData = HardcoreRevival.getRevivalData(player);
        if (revivalData.isKnockedOut()) {
            if (player.isAlive()) {
                // Make sure health stays locked at half a heart
                player.setHealth(1f);
                player.setAirSupply(Math.max(90, player.getAirSupply()));

                player.travel(Vec3.ZERO);

                revivalData.setKnockoutTicksPassed(revivalData.getKnockoutTicksPassed() + 1);

                if (player.tickCount % 20 == 0) {
                    Balm.getHooks().setForcedPose(player, revivalData.isKnockedOut() ? Pose.FALL_FLYING : null);
                }

                int maxTicksUntilDeath = HardcoreRevivalConfig.getActive().ticksUntilDeath;
                if (maxTicksUntilDeath > 0 && revivalData.getKnockoutTicksPassed() >= maxTicksUntilDeath) {
                    HardcoreRevival.getManager().notRescuedInTime(player);
                }
            } else {
                HardcoreRevival.logger.error("Found non-alive player {} ({}) in knocked out state; resetting knockout state", player.getName().getString(), player.getUUID());
                HardcoreRevival.getManager().reset(player);
            }
        }
    }

    public static void onPlayerRespawn(PlayerRespawnEvent event) {
        HardcoreRevival.getManager().reset(event.getNewPlayer());
    }


}
