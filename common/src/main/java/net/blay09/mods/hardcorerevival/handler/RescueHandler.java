package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.platform.event.callback.*;
import net.blay09.mods.hardcorerevival.HardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.PlayerHardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.network.RevivalProgressMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class RescueHandler {

    public static void initialize() {
        ItemCallback.Use.EVENT.register(RescueHandler::onUseItem);
        BlockCallback.Use.EVENT.register(RescueHandler::onUseBlock);
        PlayerCallback.Attack.Before.EVENT.register(RescueHandler::onAttack);
        ServerTickCallback.ServerPlayerTick.AFTER.register(RescueHandler::onPlayerTick);
    }

    public static InteractionEventResult onUseItem(Player player, Level level, InteractionHand hand) {
        // Prevent player from using items while they're rescuing
        return HardcoreRevivalManager.isRescuing(player) ? InteractionEventResult.FAIL : InteractionEventResult.DEFAULT;
    }

    public static InteractionEventResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        // Prevent player from placing blocks while they're rescuing
        return HardcoreRevivalManager.isRescuing(player) ? InteractionEventResult.FAIL : InteractionEventResult.DEFAULT;
    }

    public static boolean onAttack(Player player, Entity target) {
        // Stop rescuing if the player does something other than rescuing
        HardcoreRevivalManager.abortRescue(player);
        return true;
    }

    public static void onPlayerTick(ServerPlayer player) {
        // if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END) {
        Player rescueTarget = PlayerHardcoreRevivalManager.getRescueTarget(player);
        if (rescueTarget != null) {
            // Stop rescuing if the target logged out
            final int knockoutTicksPassed = PlayerHardcoreRevivalManager.getKnockoutTicksPassed(rescueTarget);
            final int maxTicksUntilDeath = HardcoreRevivalConfig.getActive().secondsUntilDeath * 20;
            if (!rescueTarget.isAlive() || (maxTicksUntilDeath > 0 && knockoutTicksPassed >= maxTicksUntilDeath)) {
                HardcoreRevivalManager.abortRescue(player);
            } else {
                // Stop rescuing if the player is out of range.
                // We allow a drift of 0.5f on the server because of floating point precision issues.
                float dist = player.distanceTo(rescueTarget);
                if (dist > HardcoreRevivalConfig.getActive().rescueDistance + 0.5f) {
                    HardcoreRevivalManager.abortRescue(player);
                } else {
                    int rescueTime = PlayerHardcoreRevivalManager.getRescueTime(player) + 1;
                    PlayerHardcoreRevivalManager.setRescueTime(player, rescueTime);

                    // Delay death while rescuing
                    PlayerHardcoreRevivalManager.setKnockoutTicksPassed(rescueTarget, knockoutTicksPassed - 1);

                    int maxRescueActionTicks = HardcoreRevivalConfig.getActive().rescueActionTicks;
                    int step = maxRescueActionTicks / 4;
                    if (rescueTime >= maxRescueActionTicks) {
                        HardcoreRevivalManager.finishRescue(player);
                    } else if (rescueTime % step == 0) {
                        Balm.networking()
                                .sendTo(player, new RevivalProgressMessage(rescueTarget.getId(), (float) rescueTime / (float) maxRescueActionTicks));
                        KnockoutSyncHandler.sendHardcoreRevivalData(rescueTarget, rescueTarget, true);
                    }
                }
            }
        }
    }


}
