package blade.inventory;

import blade.Bot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.ClickType;

public class BotClientInventory extends BotInventory {

    public BotClientInventory(Bot bot) {
        super(bot);
    }

    @Override
    public void moveInternally(Slot from, Slot to) {
        LocalPlayer player = (LocalPlayer) bot.getVanillaPlayer();
        Minecraft.getInstance().gameMode.handleInventoryMouseClick(player.containerMenu.containerId, from.getIndex(), to.getIndex(), ClickType.SWAP, player);
    }

    @Override
    public void drop(boolean entireStack) {
        ((LocalPlayer) bot.getVanillaPlayer()).drop(entireStack);
    }

    @Override
    public void openInventory() {
        Minecraft client = Minecraft.getInstance();
        if (client.screen != null) return;
        client.setScreen(new InventoryScreen(bot.getVanillaPlayer()));
    }

    @Override
    public void closeInventory() {
        Minecraft client = Minecraft.getInstance();
        if (!(client.screen instanceof InventoryScreen)) return;
        client.setScreen(null);
    }

    @Override
    public boolean hasInventoryOpen() {
        return Minecraft.getInstance().screen instanceof InventoryScreen;
    }
}
