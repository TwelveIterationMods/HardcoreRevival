package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.hardcorerevival.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.capability.CapabilityHardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.IHardcoreRevival;
import net.blay09.mods.hardcorerevival.network.MessageRevivalProgress;
import net.blay09.mods.hardcorerevival.network.MessageRevivalSuccess;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor;

public class RescueHandler {

    @SubscribeEvent
    public void onItemUse(LivingEntityUseItemEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            // Stop rescuing if the player does something other than rescuing
            abortRescue((PlayerEntity) event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        // Stop rescuing if the player does something other than rescuing
        abortRescue(event.getEntityPlayer());
    }

    public static void startRescue(PlayerEntity player, PlayerEntity target) {
        LazyOptional<IHardcoreRevival> revival = player.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
        revival.ifPresent(it -> {
            it.setRescueTarget(target);
            it.setRescueTime(0);
            NetworkHandler.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new MessageRevivalProgress(target.getEntityId(), 0f));
        });
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END) {
            LazyOptional<IHardcoreRevival> revival = event.player.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
            revival.ifPresent(it -> {
                if (it.getRescueTarget() != null) {
                    // Stop rescuing if the target logged out
                    if (it.getRescueTarget().removed) { // we can't use isAlive like deprecation notes suggest because it also checks health
                        abortRescue(event.player);
                    } else {
                        // Stop rescuing if the player is out of range
                        float dist = event.player.getDistance(it.getRescueTarget());
                        if (dist > HardcoreRevivalConfig.COMMON.maxRescueDist.get()) {
                            abortRescue(event.player);
                        } else {
                            int rescueTime = it.getRescueTime() + 1;
                            it.setRescueTime(rescueTime);
                            int step = HardcoreRevivalConfig.COMMON.rescueTime.get() / 4;
                            if (rescueTime >= HardcoreRevivalConfig.COMMON.rescueTime.get()) {
                                finishRescue(event.player);
                            } else if (rescueTime % step == 0) {
                                NetworkHandler.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.player), new MessageRevivalProgress(it.getRescueTarget().getEntityId(), (float) rescueTime / (float) HardcoreRevivalConfig.COMMON.rescueTime.get()));
                            }
                        }
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        PlayerEntity original = event.getOriginal();
        LazyOptional<IHardcoreRevival> revival = original.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
        revival.ifPresent(it -> {
            if (it.getDeathTime() > 0) {
                event.getEntityPlayer().setLocationAndAngles(original.posX, original.posY, original.posZ, 0f, 0f);
            }
        });
    }

    public static void finishRescue(PlayerEntity player) {
        LazyOptional<IHardcoreRevival> revival = player.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
        revival.ifPresent(it -> {
            PlayerEntity target = it.getRescueTarget();
            if (target != null) {
                MinecraftServer server = target.getServer();
                if (server != null) {
                    // Remember the old spawn point and then disable it so we can manually position the player
                    BlockPos prevSpawnPos = target.getBedLocation(target.dimension);
                    boolean prevSpawnForced = target.isSpawnForced(target.dimension);
                    DimensionType prevSpawnDimension = target.getSpawnDimension();

                    target.setSpawnPoint(target.getPosition(), true, target.dimension);

                    if (HardcoreRevivalConfig.COMMON.glowOnDeath.get()) {
                        target.setGlowing(false);
                    }

                    ServerPlayerEntity newPlayer = server.getPlayerList().recreatePlayerEntity((ServerPlayerEntity) target, target.dimension, true);
                    ((ServerPlayerEntity) target).connection.player = newPlayer;
                    newPlayer.setHealth(HardcoreRevivalConfig.COMMON.rescueRespawnHealth.get());
                    newPlayer.getFoodStats().setFoodLevel(HardcoreRevivalConfig.COMMON.rescueRespawnFoodLevel.get());
                    newPlayer.addPotionEffect(new EffectInstance(Effects.HUNGER, 20 * 30)); // Hunger
                    newPlayer.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 20 * 60)); // Weakness
                    newPlayer.inventory.copyInventory(target.inventory);
                    newPlayer.experienceLevel = target.experienceLevel;
                    newPlayer.experienceTotal = target.experienceTotal;
                    newPlayer.experience = target.experience;

                    newPlayer.extinguish();
                    newPlayer.setFlag(0, false); // burning flag

                    newPlayer.setScore(target.getScore());

                    // Restore the old spawnpoint
                    newPlayer.setSpawnPoint(prevSpawnPos, prevSpawnForced, prevSpawnDimension);

                    NetworkHandler.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), new MessageRevivalSuccess(newPlayer.getEntityId()));
                }
            }
        });
    }

    public static void abortRescue(PlayerEntity player) {
        LazyOptional<IHardcoreRevival> revival = player.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
        revival.ifPresent(it -> {
            if (it.getRescueTarget() != null) {
                it.setRescueTime(0);
                it.setRescueTarget(null);
                NetworkHandler.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new MessageRevivalProgress(-1, -1));
            }
        });
    }

}
