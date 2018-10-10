package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.hardcorerevival.ModConfig;
import net.blay09.mods.hardcorerevival.capability.CapabilityHardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.IHardcoreRevival;
import net.blay09.mods.hardcorerevival.network.MessageRevivalProgress;
import net.blay09.mods.hardcorerevival.network.MessageRevivalSuccess;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class RescueHandler {

    @SubscribeEvent
    public void onItemUse(LivingEntityUseItemEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            // Stop rescuing if the player does something other than rescuing
            abortRescue((EntityPlayer) event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        // Stop rescuing if the player does something other than rescuing
        abortRescue(event.getEntityPlayer());
    }

    public static void startRescue(EntityPlayer player, EntityPlayer target) {
        IHardcoreRevival revival = player.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
        if (revival != null) {
            revival.setRescueTarget(target);
            revival.setRescueTime(0);
            NetworkHandler.instance.sendTo(new MessageRevivalProgress(target.getEntityId(), 0f), (EntityPlayerMP) player);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == Side.SERVER && event.phase == TickEvent.Phase.END) {
            IHardcoreRevival revival = event.player.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
            if (revival != null && revival.getRescueTarget() != null) {
                // Stop rescuing if the target logged out
                if (revival.getRescueTarget().isDead) {
                    abortRescue(event.player);
                } else {
                    // Stop rescuing if the player is out of range
                    float dist = event.player.getDistanceToEntity(revival.getRescueTarget());
                    if (dist > ModConfig.maxRescueDist) {
                        abortRescue(event.player);
                    } else {
                        int rescueTime = revival.getRescueTime() + 1;
                        revival.setRescueTime(rescueTime);
                        int step = ModConfig.rescueTime / 4;
                        if (rescueTime >= ModConfig.rescueTime) {
                            finishRescue(event.player);
                        } else if (rescueTime % step == 0) {
                            NetworkHandler.instance.sendTo(new MessageRevivalProgress(revival.getRescueTarget().getEntityId(), (float) rescueTime / (float) ModConfig.rescueTime), (EntityPlayerMP) event.player);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        EntityPlayer original = event.getOriginal();
        IHardcoreRevival revival = original.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
        if (revival != null && revival.getDeathTime() > 0) {
            event.getEntityPlayer().setLocationAndAngles(original.posX, original.posY, original.posZ, 0f, 0f);
        }
    }

    public static void finishRescue(EntityPlayer player) {
        IHardcoreRevival revival = player.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
        if (revival != null) {
            EntityPlayer target = revival.getRescueTarget();
            if (target != null) {
                MinecraftServer server = target.getServer();
                if (server != null) {
                    // Remember the old spawn point and then disable it so we can manually position the player
                    BlockPos prevSpawnPos = target.getBedLocation(target.dimension);
                    boolean prevSpawnForced = target.isSpawnForced(target.dimension);
                    target.setSpawnChunk(null, false, target.dimension);

                    if (ModConfig.glowOnDeath) {
                        target.setGlowing(false);
                    }

                    EntityPlayerMP newPlayer = server.getPlayerList().recreatePlayerEntity((EntityPlayerMP) target, target.dimension, false);
                    ((EntityPlayerMP) target).connection.player = newPlayer;
                    newPlayer.setHealth(1f);
                    newPlayer.getFoodStats().setFoodLevel(5);
                    newPlayer.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 20 * 30));
                    newPlayer.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 20 * 60));
                    newPlayer.inventory.copyInventory(target.inventory);
                    newPlayer.experienceLevel = target.experienceLevel;
                    newPlayer.experienceTotal = target.experienceTotal;
                    newPlayer.experience = target.experience;

                    newPlayer.extinguish();
                    newPlayer.setFlag(0, false); // burning flag

                    newPlayer.setScore(target.getScore());

                    // Restore the old spawnpoint
                    newPlayer.setSpawnPoint(prevSpawnPos, prevSpawnForced);

                    NetworkHandler.instance.sendToAllAround(new MessageRevivalSuccess(newPlayer.getEntityId()), new NetworkRegistry.TargetPoint(newPlayer.dimension, newPlayer.posX, newPlayer.posY, newPlayer.posZ, 32));
                }
            }
        }
    }

    public static void abortRescue(EntityPlayer player) {
        IHardcoreRevival revival = player.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
        if (revival != null) {
            revival.setRescueTime(0);
            revival.setRescueTarget(null);
            NetworkHandler.instance.sendTo(new MessageRevivalProgress(-1, -1), (EntityPlayerMP) player);
        }
    }

}
