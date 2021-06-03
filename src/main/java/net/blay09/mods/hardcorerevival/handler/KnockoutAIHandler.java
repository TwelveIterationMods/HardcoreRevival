package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HardcoreRevival.MOD_ID)
public class KnockoutAIHandler {
    @SubscribeEvent
    public static void onAITarget(LivingSetAttackTargetEvent event) {
        if (event.getTarget() instanceof PlayerEntity && HardcoreRevival.getRevivalData(event.getTarget()).isKnockedOut()) {
            event.setCanceled(true);
        }
    }
}
