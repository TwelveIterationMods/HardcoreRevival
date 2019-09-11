package net.blay09.mods.hardcorerevival.handler;

import net.blay09.mods.hardcorerevival.HardcoreRevivalConfig;
import net.blay09.mods.hardcorerevival.capability.CapabilityHardcoreRevival;
import net.blay09.mods.hardcorerevival.capability.IHardcoreRevival;
import net.blay09.mods.hardcorerevival.network.MessageDeathTime;
import net.blay09.mods.hardcorerevival.network.MessageDie;
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
            CompoundNBT data = player.getPersistantData().getCompound(PlayerEntity.PERSISTED_NBT_TAG);
            LazyOptional<IHardcoreRevival> revival = player.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
            revival.ifPresent(it -> CapabilityHardcoreRevival.REVIVAL_CAPABILITY.readNBT(it, null, data.getCompound("HardcoreRevival")));
            NetworkHandler.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new MessageDeathTime(revival.map(IHardcoreRevival::getDeathTime).orElse(0)));
            revival.ifPresent(it -> {
                if (it.getDeathTime() >= HardcoreRevivalConfig.COMMON.maxDeathTicks.get()) {
                    NetworkHandler.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new MessageDie());
                }
            });
        }
    }

    @SubscribeEvent
    public void onCapabilityInject(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof ServerPlayerEntity) {
            event.addCapability(CapabilityHardcoreRevival.REGISTRY_NAME, new ICapabilityProvider() {
                private LazyOptional<IHardcoreRevival> revival;

                private LazyOptional<IHardcoreRevival> getRevivalCapabilityInstance() {
                    if (revival == null) {
                        IHardcoreRevival instance = CapabilityHardcoreRevival.REVIVAL_CAPABILITY.getDefaultInstance();
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
        CompoundNBT data = player.getPersistantData().getCompound(PlayerEntity.PERSISTED_NBT_TAG);
        LazyOptional<IHardcoreRevival> revival = player.getCapability(CapabilityHardcoreRevival.REVIVAL_CAPABILITY, null);
        revival.ifPresent(it -> {
            INBT tag = CapabilityHardcoreRevival.REVIVAL_CAPABILITY.writeNBT(it, null);
            if (tag != null) {
                data.put("HardcoreRevival", tag);
                player.getPersistantData().put(PlayerEntity.PERSISTED_NBT_TAG, data);
            }
        });
    }
}
