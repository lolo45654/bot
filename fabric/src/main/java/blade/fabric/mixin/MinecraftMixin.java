package blade.fabric.mixin;

import blade.Bot;
import blade.fabric.BotMod;
import blade.fabric.adapter.MinecraftAdapter;
import blade.fabric.client.BotClientMod;
import blade.fabric.client.screen.BotScreen;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
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
public abstract class MinecraftMixin implements MinecraftAdapter {
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

    @Inject(method = "pickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getPickResult()Lnet/minecraft/world/item/ItemStack;", ordinal = 0), cancellable = true)
    public void onPick(CallbackInfo ci, @Local Entity entity) {
        if (!Minecraft.getInstance().isLocalServer()) return;
        for (Bot bot : BotMod.PLATFORM.getBots()) {
            if (bot.getVanillaPlayer().getUUID().equals(entity.getUUID())) {
                ci.cancel();
                Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new BotScreen(bot)));
                return;
            }
        }
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
