package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.hardcorerevival.HardcoreRevivalManager;
import net.blay09.mods.hardcorerevival.capability.CapabilityHardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.IHardcoreRevivalData;
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
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class PlayerHandler {
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player instanceof ServerPlayerEntity) {
            CompoundNBT data = player.getPersistentData().getCompound(PlayerEntity.PERSISTED_NBT_TAG);
            IHardcoreRevivalData revivalData = HardcoreRevivalManager.getRevivalData(player);
            CapabilityHardcoreRevival.REVIVAL_CAPABILITY.readNBT(revivalData, null, data.getCompound("HardcoreRevival"));
            NetworkHandler.sendToPlayer(player, new HardcoreRevivalDataMessage(revivalData.isKnockedOut(), revivalData.getKnockoutTicksPassed()));
        }
    }

    @SubscribeEvent
    public void onCapabilityInject(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof ServerPlayerEntity) {
            event.addCapability(CapabilityHardcoreRevival.REGISTRY_NAME, new ICapabilityProvider() {
                private LazyOptional<IHardcoreRevivalData> revival;

                private LazyOptional<IHardcoreRevivalData> getRevivalCapabilityInstance() {
                    if (revival == null) {
                        IHardcoreRevivalData instance = CapabilityHardcoreRevival.REVIVAL_CAPABILITY.getDefaultInstance();
                        revival = LazyOptional.of(() -> Objects.requireNonNull(instance));
                    }

                    return revival;
                }

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
                    return CapabilityHardcoreRevival.REVIVAL_CAPABILITY.orEmpty(cap, getRevivalCapabilityInstance());
                }
            });
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerEntity player = event.getPlayer();
        CompoundNBT data = player.getPersistentData().getCompound(PlayerEntity.PERSISTED_NBT_TAG);
        IHardcoreRevivalData revivalData = HardcoreRevivalManager.getRevivalData(player);
        INBT tag = CapabilityHardcoreRevival.REVIVAL_CAPABILITY.writeNBT(revivalData, null);
        if (tag != null) {
            data.put("HardcoreRevival", tag);
            player.getPersistentData().put(PlayerEntity.PERSISTED_NBT_TAG, data);
        }
    }
}
