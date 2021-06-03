package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.hardcorerevival.HardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalDataCapability;
import net.blay09.mods.hardcorerevival.capability.HardcoreRevivalData;
import net.blay09.mods.hardcorerevival.network.HardcoreRevivalDataMessage;
import net.blay09.mods.hardcorerevival.network.NetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
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
public class PersistenceHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player instanceof ServerPlayerEntity) {
            CompoundNBT data = player.getPersistentData().getCompound(PlayerEntity.PERSISTED_NBT_TAG);
            HardcoreRevivalData revivalData = HardcoreRevival.getRevivalData(player);
            HardcoreRevivalDataCapability.REVIVAL_CAPABILITY.readNBT(revivalData, null, data.getCompound("HardcoreRevival"));
            NetworkHandler.sendToPlayer(player, new HardcoreRevivalDataMessage(revivalData.isKnockedOut(), revivalData.getKnockoutTicksPassed()));
            HardcoreRevival.getManager().updateKnockoutEffects(player, revivalData.isKnockedOut());
        }
    }

    @SubscribeEvent
    public static void onCapabilityInject(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof ServerPlayerEntity) {
            event.addCapability(HardcoreRevivalDataCapability.REGISTRY_NAME, new ICapabilityProvider() {
                private LazyOptional<HardcoreRevivalData> revival;

                private LazyOptional<HardcoreRevivalData> getRevivalCapabilityInstance() {
                    if (revival == null) {
                        HardcoreRevivalData instance = HardcoreRevivalDataCapability.REVIVAL_CAPABILITY.getDefaultInstance();
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
        PlayerEntity player = event.getPlayer();
        CompoundNBT data = player.getPersistentData().getCompound(PlayerEntity.PERSISTED_NBT_TAG);
        HardcoreRevivalData revivalData = HardcoreRevival.getRevivalData(player);
        INBT tag = HardcoreRevivalDataCapability.REVIVAL_CAPABILITY.writeNBT(revivalData, null);
        if (tag != null) {
            data.put("HardcoreRevival", tag);
            player.getPersistentData().put(PlayerEntity.PERSISTED_NBT_TAG, data);
        }
    }
}
