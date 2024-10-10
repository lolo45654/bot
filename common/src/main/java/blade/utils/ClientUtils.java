package blade.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

public class ClientUtils {
    public static void openInventory() {
        Minecraft client = Minecraft.getInstance();
        if (client.screen != null) return;
        client.setScreen(new InventoryScreen(client.player));
    }

    public static void closeInventory() {
        Minecraft client = Minecraft.getInstance();
        if (!(client.screen instanceof InventoryScreen)) return;
        client.setScreen(null);
    }

    public static boolean hasInventoryOpen() {
        return Minecraft.getInstance().screen instanceof InventoryScreen;
    }
}
