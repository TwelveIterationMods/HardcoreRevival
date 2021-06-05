package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.network.RevivalProgressMessage;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HardcoreRevival.MOD_ID)
public class RescueHandler {

    @SubscribeEvent
    public static void onItemUse(LivingEntityUseItemEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            // Stop rescuing if the player does something other than rescuing
            HardcoreRevival.getManager().abortRescue((PlayerEntity) event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        // Stop rescuing if the player does something other than rescuing
        HardcoreRevival.getManager().abortRescue(event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END) {
            HardcoreRevivalData revivalData = HardcoreRevival.getRevivalData(event.player);
            PlayerEntity rescueTarget = revivalData.getRescueTarget();
            if (rescueTarget != null) {
                // Stop rescuing if the target logged out
                final int knockoutTicksPassed = HardcoreRevival.getRevivalData(rescueTarget).getKnockoutTicksPassed();
                if (!rescueTarget.isAlive() || knockoutTicksPassed >= HardcoreRevivalConfig.COMMON.ticksUntilDeath.get()) {
                    HardcoreRevival.getManager().abortRescue(event.player);
                } else {
                    // Stop rescuing if the player is out of range
                    float dist = event.player.getDistance(rescueTarget);
                    if (dist > HardcoreRevivalConfig.COMMON.rescueDistance.get()) {
                        HardcoreRevival.getManager().abortRescue(event.player);
                    } else {
                        int rescueTime = revivalData.getRescueTime() + 1;
                        revivalData.setRescueTime(rescueTime);
                        int step = HardcoreRevivalConfig.COMMON.rescueActionTicks.get() / 4;
                        if (rescueTime >= HardcoreRevivalConfig.COMMON.rescueActionTicks.get()) {
                            HardcoreRevival.getManager().finishRescue(event.player);
                        } else if (rescueTime % step == 0) {
                            NetworkHandler.sendToPlayer(event.player, new RevivalProgressMessage(rescueTarget.getEntityId(), (float) rescueTime / (float) HardcoreRevivalConfig.COMMON.rescueActionTicks.get()));
                        }
                    }
                }
            }
        }
    }


}
