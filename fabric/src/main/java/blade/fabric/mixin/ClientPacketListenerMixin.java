package blade.fabric.mixin;

import blade.fabric.client.BotClientMod;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleRespawn", at = @At("RETURN"))
    public void onAccess(ClientboundRespawnPacket clientboundRespawnPacket, CallbackInfo ci) {
        BotClientMod.PLATFORM.onRespawn();
    }
}
