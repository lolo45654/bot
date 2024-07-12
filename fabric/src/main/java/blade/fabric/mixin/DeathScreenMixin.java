package blade.fabric.mixin;

import blade.fabric.client.BotClientMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public class DeathScreenMixin {
    @Inject(method = "setButtonsActive", at = @At("RETURN"))
    public void onDeathScreenActive(boolean active, CallbackInfo ci) {
        if (active && BotClientMod.PLATFORM.getBot() != null) {
            Minecraft.getInstance().player.respawn();
        }
    }
}
