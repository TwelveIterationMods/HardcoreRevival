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
        abortRescue(event.getPlayer());
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
                        if (dist > HardcoreRevivalConfig.SERVER.maxRescueDist.get()) {
                            abortRescue(event.player);
                        } else {
                            int rescueTime = it.getRescueTime() + 1;
                            it.setRescueTime(rescueTime);
                            int step = HardcoreRevivalConfig.SERVER.rescueTime.get() / 4;
                            if (rescueTime >= HardcoreRevivalConfig.SERVER.rescueTime.get()) {
                                finishRescue(event.player);
                            } else if (rescueTime % step == 0) {
                                NetworkHandler.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.player), new MessageRevivalProgress(it.getRescueTarget().getEntityId(), (float) rescueTime / (float) HardcoreRevivalConfig.SERVER.rescueTime.get()));
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
                event.getPlayer().setLocationAndAngles(original.getPosX(), original.getPosY(), original.getPosZ(), 0f, 0f);
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
                    if (HardcoreRevivalConfig.SERVER.glowOnDeath.get()) {
                        target.setGlowing(false);
                    }

                    target.setHealth(HardcoreRevivalConfig.SERVER.rescueRespawnHealth.get());
                    target.getFoodStats().setFoodLevel(HardcoreRevivalConfig.SERVER.rescueRespawnFoodLevel.get());
                    target.addPotionEffect(new EffectInstance(Effects.HUNGER, 20 * 30)); // Hunger
                    target.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 20 * 60)); // Weakness
                    target.extinguish();
                    target.deathTime = -1;
                    target.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null).ifPresent(ot -> {
                        ot.setDeathTime(0);
                    });

                    NetworkHandler.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new MessageRevivalProgress(it.getRescueTarget().getEntityId(), -1f));
                    NetworkHandler.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), new MessageRevivalSuccess(target.getEntityId()));

                    it.setRescueTarget(null);
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
