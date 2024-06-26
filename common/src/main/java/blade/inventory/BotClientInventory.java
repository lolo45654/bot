package blade.inventory;

import blade.Bot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.ClickType;

public class BotClientInventory extends BotInventory {

    public BotClientInventory(Bot bot) {
        super(bot);
    }

    @Override
    public void moveInternally(Slot from, Slot to) {
        var player = (net.minecraft.client.player.LocalPlayer) bot.getVanillaPlayer();
        Minecraft.getInstance().gameMode.handleInventoryMouseClick(player.containerMenu.containerId, from.getIndex(), to.getIndex(), ClickType.SWAP, player);
    }

    @Override
    public void drop(boolean entireStack) {
        ((net.minecraft.client.player.LocalPlayer) bot.getVanillaPlayer()).drop(entireStack);
    }

    @Override
    public void startAction() {
        Minecraft.getInstance().setScreen(new InventoryScreen(bot.getVanillaPlayer()));
    }

    @Override
    public void endAction() {
        Minecraft.getInstance().setScreen(null);
    }
}
