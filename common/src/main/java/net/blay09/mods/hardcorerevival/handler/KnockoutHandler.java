package net.blay09.mods.hardcorerevival.handler;


import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.platform.event.EventPhases;
import net.blay09.mods.balm.platform.event.callback.LivingEntityCallback;
import net.blay09.mods.balm.platform.event.callback.ServerPlayerCallback;
import net.blay09.mods.balm.platform.event.callback.ServerTickCallback;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.HardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.PlayerHardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.api.PlayerAboutToKnockOutEvent;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.mixin.LivingEntityAccessor;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.level.gameevent.GameEvent;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;


public class KnockoutHandler {
    private static boolean didLogTrinketsDeathProtectionError;
    private static boolean didLogTrinketsDeathProtectionSyncError;

    public static void initialize() {
        LivingEntityCallback.Death.Before.EVENT.register(EventPhases.HIGH, KnockoutHandler::allowPlayerDeath);
        ServerPlayerCallback.Respawn.EVENT.register(KnockoutHandler::onPlayerRespawn);

        ServerTickCallback.ServerPlayerTick.BEFORE.register(KnockoutHandler::onPlayerTick);
    }

    public static boolean allowPlayerDeath(LivingEntity entity, DamageSource damageSource) {
        if (entity instanceof ServerPlayer player) {
            if (PlayerHardcoreRevivalManager.isKnockedOut(player)) {
                Entity attacker = damageSource.getEntity();
                if (attacker instanceof Mob mob) {
                    mob.setTarget(null);
                }
                player.setHealth(0.5f);
                return damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || damageSource.is(HardcoreRevivalManager.NOT_RESCUED_IN_TIME);
            }

            if (isKnockoutEnabledFor(player, damageSource)) {
                if (tryUseDeathProtection(player, damageSource)) {
                    return false;
                }

                final var aboutToKnockOutEvent = new PlayerAboutToKnockOutEvent(player, damageSource);
                PlayerAboutToKnockOutEvent.EVENT.invoker().accept(aboutToKnockOutEvent);

                if (!aboutToKnockOutEvent.isCanceled()) {
                    HardcoreRevivalManager.knockout(player, damageSource);
                    player.setHealth(0.5f);
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean tryUseDeathProtection(ServerPlayer player, DamageSource damageSource) {
        if (((LivingEntityAccessor) player).callCheckTotemDeathProtection(damageSource)) {
            return true;
        }

        if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }

        return tryUseTrinketsDeathProtection(player);
    }

    private static boolean tryUseTrinketsDeathProtection(ServerPlayer player) {
        try {
            final var trinketsApiClass = Class.forName("dev.emi.trinkets.api.TrinketsApi", false, KnockoutHandler.class.getClassLoader());
            final var trinketComponentClass = Class.forName("dev.emi.trinkets.api.TrinketComponent", false, KnockoutHandler.class.getClassLoader());
            final var getTrinketComponent = findMethod(trinketsApiClass, "getTrinketComponent", 1);
            if (getTrinketComponent == null) {
                return false;
            }

            final var componentResult = getTrinketComponent.invoke(null, player);
            if (!(componentResult instanceof Optional<?> optionalComponent) || optionalComponent.isEmpty()) {
                return false;
            }

            final var component = optionalComponent.get();
            final var foundItem = new AtomicReference<ItemStack>();
            final var forEach = trinketComponentClass.getMethod("forEach", BiConsumer.class);
            forEach.invoke(component, (BiConsumer<Object, ItemStack>) (slotReference, itemStack) -> {
                if (foundItem.get() == null && itemStack.get(DataComponents.DEATH_PROTECTION) != null) {
                    foundItem.set(itemStack);
                }
            });

            final var itemStack = foundItem.get();
            if (itemStack == null) {
                return false;
            }

            final var deathProtection = itemStack.get(DataComponents.DEATH_PROTECTION);
            if (deathProtection == null) {
                return false;
            }

            useDeathProtectionItem(player, itemStack, deathProtection);
            trySyncTrinketsComponent(trinketsApiClass, player);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        } catch (LinkageError | ReflectiveOperationException e) {
            if (!didLogTrinketsDeathProtectionError) {
                didLogTrinketsDeathProtectionError = true;
                HardcoreRevival.logger.warn("Failed to check Trinkets death protection items", e);
            }
            return false;
        }
    }

    private static Method findMethod(Class<?> type, String name, int parameterCount) {
        for (final var method : type.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                return method;
            }
        }

        return null;
    }

    private static void trySyncTrinketsComponent(Class<?> trinketsApiClass, ServerPlayer player) {
        try {
            final var trinketComponentKey = trinketsApiClass.getField("TRINKET_COMPONENT").get(null);
            final var sync = findMethod(trinketComponentKey.getClass(), "sync", 1);
            if (sync != null) {
                sync.invoke(trinketComponentKey, player);
            }
        } catch (LinkageError | ReflectiveOperationException e) {
            if (!didLogTrinketsDeathProtectionSyncError) {
                didLogTrinketsDeathProtectionSyncError = true;
                HardcoreRevival.logger.warn("Failed to sync Trinkets death protection item consumption", e);
            }
        }
    }

    private static void useDeathProtectionItem(ServerPlayer player, ItemStack itemStack, DeathProtection deathProtection) {
        final var itemStackCopy = itemStack.copy();
        itemStack.shrink(1);

        player.awardStat(Stats.ITEM_USED.get(itemStackCopy.getItem()));
        CriteriaTriggers.USED_TOTEM.trigger(player, itemStackCopy);
        itemStackCopy.causeUseVibration(player, GameEvent.ITEM_INTERACT_FINISH);
        player.setHealth(1f);
        deathProtection.applyEffects(itemStackCopy, player);
        player.level().broadcastEntityEvent(player, (byte) 35);
    }

    private static boolean isKnockoutEnabledFor(ServerPlayer player, DamageSource damageSource) {
        final var server = player.level().getServer();

        boolean canDamageSourceKnockout = !damageSource.is(DamageTypes.FELL_OUT_OF_WORLD) && !damageSource.is(HardcoreRevivalManager.NOT_RESCUED_IN_TIME);
        final var damageSourceId = player.level().getServer().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getKey(damageSource.type());
        if (!canDamageSourceKnockout || HardcoreRevivalConfig.getActive().instantDeathSources.contains(damageSourceId)) {
            return false;
        }

        final var attacker = damageSource.getEntity();
        if (attacker != null) {
            final var entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(attacker.getType());
            if (HardcoreRevivalConfig.getActive().instantDeathEntityTypes.contains(entityTypeId)) {
                return false;
            }
        }

        if (HardcoreRevivalConfig.getActive().disableInSingleplayer && server.isSingleplayer() && server.getPlayerCount() == 1) {
            return false;
        } else if (HardcoreRevivalConfig.getActive().disableInLonelyMultiplayer && !server.isSingleplayer() && server.getPlayerCount() == 1) {
            return false;
        }

        return true;
    }

    public static void onPlayerTick(ServerPlayer player) {
        //if (event.phase == TickEvent.Phase.START && event.side == LogicalSide.SERVER) {
        if (PlayerHardcoreRevivalManager.isKnockedOut(player) && player.isAlive()) {
            // Make sure health stays locked at half a heart
            player.setHealth(1f);

            PlayerHardcoreRevivalManager.setKnockoutTicksPassed(player, PlayerHardcoreRevivalManager.getKnockoutTicksPassed(player) + 1);

            if (player.tickCount % 20 == 0) {
                Balm.hooks().setForcedPose(player, PlayerHardcoreRevivalManager.isKnockedOut(player) ? Pose.FALL_FLYING : null);
            }

            int maxTicksUntilDeath = HardcoreRevivalConfig.getActive().secondsUntilDeath * 20;
            if (maxTicksUntilDeath > 0 && PlayerHardcoreRevivalManager.getKnockoutTicksPassed(player) >= maxTicksUntilDeath) {
                HardcoreRevivalManager.notRescuedInTime(player);
            }
        }
    }

    public static void onPlayerRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        HardcoreRevivalManager.reset(newPlayer);
    }


}
