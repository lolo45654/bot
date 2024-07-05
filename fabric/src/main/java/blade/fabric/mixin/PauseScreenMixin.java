package blade.fabric.mixin;

import blade.fabric.client.BotClientMod;
import blade.fabric.client.screen.BotScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    @Shadow @Final private boolean showPauseMenu;

    protected PauseScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;I)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
    public void addGuiButton(CallbackInfo ci) {
        if (!showPauseMenu) return;
        if (BotClientMod.PLATFORM.getBot() == null) return;

        addRenderableWidget(Button.builder(Component.literal("Bot"), btn -> {
            Minecraft.getInstance().setScreen(new BotScreen(BotClientMod.PLATFORM.getBot()));
        }).pos(12, height - 32).size(40, 20).build());
    }
}
