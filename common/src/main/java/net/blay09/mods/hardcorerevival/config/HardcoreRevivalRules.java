package net.blay09.mods.hardcorerevival.config;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.shogi.Shogi;
import net.blay09.mods.shogi.ShogiValue;
import net.blay09.mods.shogi.context.ShogiContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.Nullable;

public class HardcoreRevivalRules {

    public static final ShogiValue<ShogiContext, ?> revived = Shogi.maybe(ResourceLocation.fromNamespaceAndPath(HardcoreRevival.MOD_ID,
            "revived"), HardcoreRevivalRules::applyDefaultRevivedEffects);

    public static void initialize() {
    }

    private static Either<Boolean, ?> applyDefaultRevivedEffects(ShogiContext context) {
        final var player = context.requirePlayer();
        final var config = HardcoreRevivalConfig.getActive();
        player.setHealth(config.rescueRespawnHealth);
        final var foodData = player.getFoodData();
        foodData.setFoodLevel(Math.min(foodData.getFoodLevel(), config.rescueRespawnFoodLevel));
        // client only, won't bother: player.getFoodStats().setFoodSaturationLevel((float) config.getRescueRespawnFoodSaturation());

        for (String effectString : config.rescueRespawnEffects) {
            String[] parts = effectString.split("\\|");
            ResourceLocation registryName = ResourceLocation.tryParse(parts[0]);
            if (registryName != null) {
                final var holder = BuiltInRegistries.MOB_EFFECT.getHolder(registryName);
                if (holder.isPresent()) {
                    int duration = tryParseInt(parts.length >= 2 ? parts[1] : null, 600);
                    int amplifier = tryParseInt(parts.length >= 3 ? parts[2] : null, 0);
                    player.addEffect(new MobEffectInstance(holder.get(), duration, amplifier));
                } else {
                    HardcoreRevival.logger.info("Invalid rescue potion effect '{}'", parts[0]);
                }
            } else {
                HardcoreRevival.logger.info("Invalid rescue potion effect '{}'", parts[0]);
            }
        }

        return Either.left(true);
    }

    private static int tryParseInt(@Nullable String text, int defaultVal) {
        if (text != null) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                return defaultVal;
            }
        }
        return defaultVal;
    }
}
