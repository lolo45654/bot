package blade.fabric.mixin;

import blade.fabric.client.BotClientMod;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientSuggestionProvider.class)
public class ClientSuggestionProviderMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(ClientPacketListener clientPacketListener, Minecraft minecraft, CallbackInfo ci) {
        BotClientMod.CLIENT_SOURCE = (FabricClientCommandSource) this;
    }
}
