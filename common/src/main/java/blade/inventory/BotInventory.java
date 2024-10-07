package blade.inventory;

import blade.Bot;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class BotInventory {
    protected final Bot bot;
    protected final Inventory inventory;
    protected final ImmutableList<NonNullList<ItemStack>> compartments;
    protected boolean inventoryOpen = false;

    public BotInventory(Bot bot) {
        this.bot = bot;
        this.inventory = bot.getVanillaPlayer().getInventory();
        this.compartments = ImmutableList.of(inventory.items, inventory.armor, inventory.offhand);
    }

    public Bot getBot() {
        return bot;
    }

    public ItemStack getItem(Slot slot) {
        return inventory.getItem(slot.vanillaIndex());
    }

    public List<ItemStack> getHotBar() {
        return inventory.items.subList(0, 9);
    }

    public List<ItemStack> getMain() {
        return inventory.items.subList(9, 36);
    }

    public ItemStack getOffHand() {
        return inventory.offhand.get(0);
    }

    public List<ItemStack> getArmor() {
        return inventory.armor;
    }

    public int getSelectedSlot() {
        return inventory.selected;
    }

    public void setSelectedSlot(int slot) {
        inventory.selected = slot;
    }

    public Inventory getVanilla() {
        return inventory;
    }

    public void openInventory() {
        inventoryOpen = true;
        if (!bot.isClient) return;
        Minecraft client = Minecraft.getInstance();
        if (client.screen != null) return;
        client.setScreen(new InventoryScreen(bot.getVanillaPlayer()));
    }

    public void closeInventory() {
        inventoryOpen = false;
        if (!bot.isClient) return;
        Minecraft client = Minecraft.getInstance();
        if (!(client.screen instanceof InventoryScreen)) return;
        client.setScreen(null);
    }

    public boolean hasInventoryOpen() {
        return inventoryOpen || (bot.isClient && Minecraft.getInstance().screen instanceof InventoryScreen);
    }

    /**
     * Swap item from one slot to another.
     */
    public void move(Slot from, Slot to) {
        if (from.equals(to)) return;
        moveInternally(from, to);
    }

    public void moveInternally(Slot from, Slot to) {
        if (bot.isClient) {
            LocalPlayer player = (LocalPlayer) bot.getVanillaPlayer();
            Minecraft.getInstance().gameMode.handleInventoryMouseClick(player.containerMenu.containerId, from.vanillaIndex(), to.vanillaIndex(), ClickType.SWAP, player);
            return;
        }

        int fromVanillaIndex = from.vanillaIndex();
        int toVanillaIndex = to.vanillaIndex();
        ItemStack tmp = inventory.getItem(fromVanillaIndex);
        inventory.setItem(fromVanillaIndex, inventory.getItem(toVanillaIndex));
        inventory.setItem(toVanillaIndex, tmp);
        if (getBot().getVanillaPlayer() instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(serverPlayer.containerMenu.containerId, serverPlayer.containerMenu.incrementStateId(), from.index(), inventory.getItem(from.index())));
            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(serverPlayer.containerMenu.containerId, serverPlayer.containerMenu.incrementStateId(), to.index(), inventory.getItem(to.index())));
        }
    }

    /**
     * Drops the main hand item.
     */
    public void drop(boolean entireStack) {
        if (bot.isClient) {
            ((LocalPlayer) bot.getVanillaPlayer()).drop(entireStack);
            return;
        }

        ((ServerPlayer) bot.getVanillaPlayer()).drop(entireStack);
    }

    /**
     * Retrieve slot with a matching item. Order can also be used to exclude slot regions.
     */
    public Slot findFirst(Predicate<ItemStack> tester, SlotFlag... order) {
        if (order.length == 0) order = new SlotFlag[] { SlotFlag.MAIN, SlotFlag.HOT_BAR, SlotFlag.ARMOR, SlotFlag.OFF_HAND };
        for (SlotFlag flag : order) {
            for (int i = 0; i < Slot.MAX_INDEX; i++) {
                Slot slot = new Slot(i);
                if (!flag.matchesSlot(slot)) continue;
                if (tester.test(getItem(slot))) return slot;
            }
        }
        return null;
    }

    public Slot find(Comparator<ItemStack> comparator) {
        return findBest(stack -> true, comparator);
    }

    /**
     * Retrieve best slot using comparator.
     */
    public Slot findBest(Predicate<ItemStack> tester, Comparator<ItemStack> sorter) {
        ItemStack bestStack = null;
        Integer bestVanillaIndex = null;
        int index = -1;
        for (NonNullList<ItemStack> itemList : compartments) {
            for (ItemStack stack : itemList) {
                index++;
                if (!tester.test(stack)) continue;
                if (bestStack != null && sorter.compare(stack, bestStack) <= 0) continue;
                bestStack = stack;
                bestVanillaIndex = index;
            }
        }
        return bestVanillaIndex == null ? null : Slot.ofVanilla(bestVanillaIndex);
    }

    public Slot findBestFood(Predicate<FoodProperties> tester, SlotFlag... slots) {
        Slot bestSlot = null;
        float bestNum = Float.MIN_NORMAL;
        for (int i = 0; i < Slot.MAX_INDEX; i++) {
            Slot slot = new Slot(i);
            inner: {
                for (SlotFlag flag : slots) {
                    if (flag.matchesSlot(slot)) break inner;
                }
                continue;
            }
            ItemStack stack = getItem(slot);
            FoodProperties foodProperties = stack.get(DataComponents.FOOD);
            if (foodProperties == null) continue;
            float num = foodProperties.nutrition() + foodProperties.saturation();
            if (num > bestNum && tester.test(foodProperties)) {
                bestNum = num;
                bestSlot = slot;
            }
        }
        return bestSlot;
    }
}
