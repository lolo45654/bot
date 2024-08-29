package blade.fabric.mixin;

import blade.fabric.adapter.MinecraftAdapter;
import blade.fabric.client.BotClientMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin implements MinecraftAdapter {
    @Shadow public Screen screen;

    @Shadow @Nullable
    public LocalPlayer player;
    @Shadow
    public int missTime;
    @Shadow @Final
    public Options options;
    @Unique
    private int previousMissTime = 0;
    @Unique
    private int lastAttackClicks = 0;
    @Unique
    private int lastInteractClicks = 0;

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", ordinal = 6))
    public Screen onScreenAccess(Minecraft instance) {
        if (BotClientMod.PLATFORM.getBot() == null || player == null) return screen;
        if (missTime == 10000) {
            missTime = previousMissTime;
        }
        return null;
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;missTime:I"))
    public void onMissTimeAccess(CallbackInfo ci) {
        previousMissTime = missTime;
    }

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    public void preHandleKeybinds(CallbackInfo ci) {
        lastAttackClicks = ((KeyMappingAccessor) options.keyAttack).getClickCount();
        lastInteractClicks = ((KeyMappingAccessor) options.keyUse).getClickCount();
    }

    @Override
    public int bot$getLastAttackClicks() {
        return lastAttackClicks;
    }

    @Override
    public int bot$getLastInteractClicks() {
        return lastInteractClicks;
    }
}
