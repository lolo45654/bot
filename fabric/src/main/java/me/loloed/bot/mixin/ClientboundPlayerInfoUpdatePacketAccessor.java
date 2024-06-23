package me.loloed.bot.mixin;

import net.minecraft.advancements.critereon.LightningStrikeTrigger;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ClientboundPlayerInfoUpdatePacket.class)
public interface ClientboundPlayerInfoUpdatePacketAccessor {
    @Accessor
    void setEntries(List<ClientboundPlayerInfoUpdatePacket.Entry> entries);
}
