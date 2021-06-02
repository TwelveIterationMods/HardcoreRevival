package net.blay09.mods.hardcorerevival.handler;


import net.blay09.mods.hardcorerevival.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.HardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.capability.IHardcoreRevivalData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class DeathHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerDamage(LivingDamageEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();

            boolean canKnockout = event.getSource() != DamageSource.OUT_OF_WORLD;
            if (canKnockout && player.getHealth() - event.getAmount() <= 0f) {
                // Reduce damage to prevent the player from dying
                event.setAmount(Math.min(event.getAmount(), Math.max(0f, player.getHealth() - 0.5f)));

                // Trigger knockout for this player
                HardcoreRevivalManager.knockout(player, event.getSource());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            IHardcoreRevivalData revivalData = HardcoreRevivalManager.getRevivalData(event.player);
            if (revivalData.isKnockedOut()) {
                event.player.setHealth(0.5f);

                BlockPos knockoutPos = revivalData.getKnockoutPos();
                if (event.player.getDistanceSq(knockoutPos.getX(), knockoutPos.getY(), knockoutPos.getZ()) > 1f) {
                    event.player.setPositionAndUpdate(knockoutPos.getX(), knockoutPos.getY(), knockoutPos.getZ());
                }

                revivalData.setKnockoutTicksPassed(revivalData.getKnockoutTicksPassed() + 1);

                if (!HardcoreRevivalConfig.COMMON.disableDeathTimer.get() && revivalData.getKnockoutTicksPassed() >= HardcoreRevivalConfig.COMMON.maxDeathTicks.get()) {
                    HardcoreRevivalManager.notRescuedInTime(event.player);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        HardcoreRevivalManager.reset(event.getPlayer());
    }
}
