package net.blay09.mods.hardcorerevival.handler;


import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.HardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = HardcoreRevival.MOD_ID)
public class KnockoutHandler {

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity && HardcoreRevival.getRevivalData(event.getEntityLiving()).isKnockedOut()) {
            Entity attacker = event.getSource().getTrueSource();
            if (attacker instanceof MobEntity) {
                ((MobEntity) attacker).setAttackTarget(null);
            }
            if (event.getSource().canHarmInCreative() && event.getSource() != HardcoreRevivalManager.notRescuedInTime) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDamage(LivingDamageEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();

            boolean canDamageSourceKnockout = event.getSource() != DamageSource.OUT_OF_WORLD && event.getSource() != HardcoreRevivalManager.notRescuedInTime;
            if (canDamageSourceKnockout && player.getHealth() - event.getAmount() <= 0f) {
                // Reduce damage to prevent the player from dying
                event.setAmount(Math.min(event.getAmount(), Math.max(0f, player.getHealth() - 1f)));

                // Trigger knockout for this player
                HardcoreRevival.getManager().knockout(player, event.getSource());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.side == LogicalSide.SERVER) {
            HardcoreRevivalData revivalData = HardcoreRevival.getRevivalData(event.player);
            if (revivalData.isKnockedOut() && event.player.isAlive()) {
                // Make sure health stays locked at half a heart
                event.player.setHealth(1f);

                revivalData.setKnockoutTicksPassed(revivalData.getKnockoutTicksPassed() + 1);

                if (HardcoreRevivalConfig.COMMON.ticksUntilDeath.get() > 0 && revivalData.getKnockoutTicksPassed() >= HardcoreRevivalConfig.COMMON.ticksUntilDeath.get()) {
                    HardcoreRevival.getManager().notRescuedInTime(event.player);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            HardcoreRevival.getManager().reset(((PlayerEntity) event.getEntityLiving()));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        HardcoreRevival.getManager().reset(event.getPlayer());
    }


}
