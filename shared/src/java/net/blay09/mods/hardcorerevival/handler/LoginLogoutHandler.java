package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalDataCapability;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalDataImpl;
import net.blay09.mods.hardcorerevival.config.HardcoreRevivalConfig;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = HardcoreRevival.MOD_ID)
public class LoginLogoutHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            CompoundTag data = Balm.getHooks().getPersistentData(player);
            HardcoreRevivalData revivalData = HardcoreRevival.getRevivalData(player);
            HardcoreRevivalDataCapability.REVIVAL_CAPABILITY.readNBT(revivalData, null, data.getCompound("HardcoreRevival"));

            if (HardcoreRevivalConfig.getActive().continueTimerWhileOffline && revivalData.isKnockedOut()) {
                long worldTimeNow = player.level.getGameTime();
                long worldTimeThen = revivalData.getLogoutWorldTime();
                int worldTimePassed = (int) Math.max(0, worldTimeNow - worldTimeThen);
                revivalData.setKnockoutTicksPassed(revivalData.getKnockoutTicksPassed() + worldTimePassed);
            }

            HardcoreRevival.getManager().updateKnockoutEffects(player);
        }
    }

    @SubscribeEvent
    public static void onCapabilityInject(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(HardcoreRevivalDataCapability.REGISTRY_NAME, new ICapabilityProvider() {
                private LazyOptional<HardcoreRevivalData> revival;

                private LazyOptional<HardcoreRevivalData> getRevivalCapabilityInstance() {
                    if (revival == null) {
                        HardcoreRevivalData instance = new HardcoreRevivalDataImpl();
                        revival = LazyOptional.of(() -> Objects.requireNonNull(instance));
                    }

                    return revival;
                }

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
                    return HardcoreRevivalDataCapability.REVIVAL_CAPABILITY.orEmpty(cap, getRevivalCapabilityInstance());
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getPlayer();
        CompoundTag data = Balm.getHooks().getPersistentData(player);
        HardcoreRevivalData revivalData = HardcoreRevival.getRevivalData(player);
        revivalData.setLogoutWorldTime(player.level.getGameTime());
        Tag tag = HardcoreRevivalDataCapability.REVIVAL_CAPABILITY.writeNBT(revivalData, null);
        if (tag != null) {
            data.put("HardcoreRevival", tag);
        }
    }
}
