package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.hardcorerevival.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.HardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.capability.IHardcoreRevivalData;
import net.blay09.mods.hardcorerevival.network.RevivalProgressMessage;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor;

public class RescueHandler {

    @SubscribeEvent
    public void onItemUse(LivingEntityUseItemEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            // Stop rescuing if the player does something other than rescuing
            HardcoreRevivalManager.abortRescue((PlayerEntity) event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        // Stop rescuing if the player does something other than rescuing
        HardcoreRevivalManager.abortRescue(event.getPlayer());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END) {
            IHardcoreRevivalData revivalData = HardcoreRevivalManager.getRevivalData(event.player);
            PlayerEntity rescueTarget = revivalData.getRescueTarget();
            if (rescueTarget != null) {
                // Stop rescuing if the target logged out
                final int knockoutTicksPassed = HardcoreRevivalManager.getRevivalData(rescueTarget).getKnockoutTicksPassed();
                if (!rescueTarget.isAlive() || knockoutTicksPassed >= HardcoreRevivalConfig.COMMON.maxDeathTicks.get()) {
                    HardcoreRevivalManager.abortRescue(event.player);
                } else {
                    // Stop rescuing if the player is out of range
                    float dist = event.player.getDistance(rescueTarget);
                    if (dist > HardcoreRevivalConfig.COMMON.maxRescueDist.get()) {
                        HardcoreRevivalManager.abortRescue(event.player);
                    } else {
                        int rescueTime = revivalData.getRescueTime() + 1;
                        revivalData.setRescueTime(rescueTime);
                        int step = HardcoreRevivalConfig.COMMON.rescueTime.get() / 4;
                        if (rescueTime >= HardcoreRevivalConfig.COMMON.rescueTime.get()) {
                            HardcoreRevivalManager.finishRescue(event.player);
                        } else if (rescueTime % step == 0) {
                            NetworkHandler.sendToPlayer(event.player, new RevivalProgressMessage(rescueTarget.getEntityId(), (float) rescueTime / (float) HardcoreRevivalConfig.COMMON.rescueTime.get()));
                        }
                    }
                }
            }
        }
    }


}
