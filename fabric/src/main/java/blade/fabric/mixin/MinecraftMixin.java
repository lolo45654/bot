package blade.fabric.mixin;

import blade.fabric.client.BotClientMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow public Screen screen;
    @Shadow private Overlay overlay;

    @Unique
    private boolean overlayAccessed = false;

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;overlay:Lnet/minecraft/client/gui/screens/Overlay;"))
    public Overlay onOverlayAccess(Minecraft instance) {
        if (BotClientMod.PLATFORM.getBot() == null) return overlay;
        overlayAccessed = overlay == null;
        return null;
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;"))
    public Screen onScreenAccess(Minecraft instance) {
        if (!overlayAccessed) return screen;
        if (BotClientMod.PLATFORM.getBot() == null) return screen;
        overlayAccessed = false;
        return null;
    }
}
