package net.blay09.mods.hardcorerevival.handler;


import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.HardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.mixin.LivingEntityAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
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
        if (event.getEntityLiving() instanceof Player && HardcoreRevival.getRevivalData(event.getEntityLiving()).isKnockedOut()) {
            Entity attacker = event.getSource().getEntity();
            if (attacker instanceof Mob mob) {
                mob.setTarget(null);
            }
            if (!event.getSource().isBypassInvul() && event.getSource() != HardcoreRevivalManager.notRescuedInTime) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDamage(LivingDamageEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer player) {
            boolean canDamageSourceKnockout = event.getSource() != DamageSource.OUT_OF_WORLD && event.getSource() != HardcoreRevivalManager.notRescuedInTime;
            if (canDamageSourceKnockout && player.getHealth() - event.getAmount() <= 0f) {
                // Reduce damage to prevent the player from dying
                event.setAmount(Math.min(event.getAmount(), Math.max(0f, player.getHealth() - 1f)));

                // Trigger knockout for this player, if totem does not protect player
                if (((LivingEntityAccessor) player).callCheckTotemDeathProtection(event.getSource())) {
                    event.setCanceled(true);
                } else {
                    HardcoreRevival.getManager().knockout(player, event.getSource());
                }
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

                int maxTicksUntilDeath = HardcoreRevivalConfig.getActive().ticksUntilDeath;
                if (maxTicksUntilDeath > 0 && revivalData.getKnockoutTicksPassed() >= maxTicksUntilDeath) {
                    HardcoreRevival.getManager().notRescuedInTime(event.player);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof Player player) {
            HardcoreRevival.getManager().reset(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        HardcoreRevival.getManager().reset(event.getPlayer());
    }


}
