package net.blay09.mods.hardcorerevival.config;

import com.mojang.datafixers.util.Either;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.shogi.Shogi;
import net.blay09.mods.shogi.ShogiValue;
import net.blay09.mods.shogi.context.MutableShogiContext;
import net.blay09.mods.shogi.context.ShogiContext;
import net.blay09.mods.shogi.context.executor.EffectExecutor;
import net.blay09.mods.shogi.network.ShogiStreamCodecs;
import net.blay09.mods.shogi.scope.ShogiScope;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static net.blay09.mods.hardcorerevival.HardcoreRevival.id;

public class HardcoreRevivalRules {

    private static final ShogiScope scope = Shogi.scope(id("rules"), it -> it.setDefaultNamespaces(List.of("hardcorerevival", "shogi")));

    public static final ShogiValue<ShogiContext, ?> revived = scope.maybe(id("revived"), HardcoreRevivalRules::applyDefaultRevivedEffects);
    public static final ShogiValue<MutableShogiContext, ?> canRevive = scope.maybe(id("can_revive"), HardcoreRevivalRules::allowByDefault);
    public static final ShogiValue<MutableShogiContext, ?> canBeRevived = scope.maybe(id("can_be_revived"), HardcoreRevivalRules::allowByDefault);

    public static void initialize() {
    }

    public static void applyRevivedEffects(Player player) {
        final var context = MutableShogiContext.of(player);
        revived.getOrDefault(context);
    }

    public static Either<?, ?> simulateRescueRules(Player player, Player target) {
        return evaluateRescueRules(player, target, EffectExecutor.simulated());
    }

    public static Either<?, ?> executeRescueRules(Player player, Player target) {
        return evaluateRescueRules(player, target, EffectExecutor.immediate());
    }

    private static Either<?, ?> evaluateRescueRules(Player player, Player target, EffectExecutor executor) {
        final var canReviveContext = createRuleContext(player, target, player, executor);
        final var canReviveResult = evaluateRule(canRevive, canReviveContext);
        if (canReviveResult.right().isPresent()) {
            return canReviveResult;
        }

        final var canBeRevivedContext = createRuleContext(target, player, target, executor);
        final var canBeRevivedResult = evaluateRule(canBeRevived, canBeRevivedContext);
        if (canBeRevivedResult.right().isPresent()) {
            return canBeRevivedResult;
        }

        return Either.left(combinePayloads(canReviveResult, canBeRevivedResult));
    }

    private static MutableShogiContext createRuleContext(Player entity, Player reviver, Player target, EffectExecutor executor) {
        return MutableShogiContext.create(executor)
                .withEntity(entity)
                .withLevel(entity.level())
                .withBlockPos(entity.blockPosition())
                .withItemStack(entity.getMainHandItem())
                .withVariable("reviver", reviver)
                .withVariable("target", target);
    }

    private static Either<?, ?> evaluateRule(ShogiValue<MutableShogiContext, ?> rule, MutableShogiContext context) {
        final var result = rule.get(context);
        final var payload = Either.unwrap(result);
        if (result.right().isPresent() && payload instanceof Throwable throwable) {
            HardcoreRevival.logger.error("Unhandled exception while evaluating revival rules", throwable);
        }
        return result;
    }

    private static List<?> combinePayloads(Either<?, ?> first, Either<?, ?> second) {
        return List.of(Either.unwrap(first), Either.unwrap(second));
    }

    private static Either<Boolean, ?> applyDefaultRevivedEffects(ShogiContext context) {
        final var player = context.requirePlayer();
        final var config = HardcoreRevivalConfig.getActive();
        player.setHealth(Math.max(1, config.rescueRespawnHealth));
        final var foodData = player.getFoodData();
        foodData.setFoodLevel(Math.min(foodData.getFoodLevel() - config.rescueRespawnFoodLevelDecrease, config.rescueRespawnFoodLevel));
        // client only, won't bother: player.getFoodStats().setFoodSaturationLevel((float) config.getRescueRespawnFoodSaturation());

        for (String effectString : config.rescueRespawnEffects) {
            String[] parts = effectString.split("\\|");
            Identifier registryName = Identifier.tryParse(parts[0]);
            if (registryName != null) {
                final var holder = BuiltInRegistries.MOB_EFFECT.get(registryName);
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

    private static Either<Boolean, ?> allowByDefault(MutableShogiContext context) {
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
