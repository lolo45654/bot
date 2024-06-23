package me.loloed.bot.mixin;

import me.loloed.bot.client.BotDebugOverlay;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class GuiMixin {
    @ModifyArg(method = "drawGameInformation",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;renderLines(Lnet/minecraft/client/gui/GuiGraphics;Ljava/util/List;Z)V"), index = 1)
    public List<String> onRenderLeft(List<String> list) {
        list.addAll(BotDebugOverlay.getLeft());
        return list;
    }

    @ModifyArg(method = "drawSystemInformation",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;renderLines(Lnet/minecraft/client/gui/GuiGraphics;Ljava/util/List;Z)V"), index = 1)
    public List<String> onRenderRight(List<String> list) {
        list.addAll(BotDebugOverlay.getRight());
        return list;
    }
}
