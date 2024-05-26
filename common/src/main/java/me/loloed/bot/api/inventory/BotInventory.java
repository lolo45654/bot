package me.loloed.bot.api.inventory;

import me.loloed.bot.api.Bot;
import me.loloed.bot.api.event.InventoryEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public class BotInventory {
    protected final Bot bot;
    protected final Inventory inventory;

    public BotInventory(Bot bot) {
        this.bot = bot;
        this.inventory = bot.getVanillaPlayer().getInventory();
        if (bot.isClient) throw new UnsupportedOperationException("use BotClientInventory");
    }


    public Bot getBot() {
        return bot;
    }

    public void move(Slot from, Slot to) {
        move(from, to, false);
    }

    public void move(Slot from, Slot to, boolean force) {
        if (from.equals(to)) return;
        InventoryEvents.MOVE_ITEM.call(bot).onMoveItem(bot, from, to);
        moveInternally(from, to);
    }

    public ItemStack getItem(Slot slot) {
        if (slot.isOffHand()) return getOffHand();
        if (slot.isArmor()) return getArmor().get(slot.getArmorIndex());
        if (slot.isHotBar()) return getHotBar().get(slot.getHotBarIndex());
        return getMain().get(slot.getMainIndex());
    }

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

    public void moveInternally(Slot from, Slot to) {
        int fromVanillaIndex = from.getVanillaIndex();
        int toVanillaIndex = to.getVanillaIndex();
        ItemStack tmp = inventory.getItem(fromVanillaIndex);
        inventory.setItem(fromVanillaIndex, inventory.getItem(toVanillaIndex));
        inventory.setItem(toVanillaIndex, tmp);
        if (getBot().getVanillaPlayer() instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(serverPlayer.containerMenu.containerId, serverPlayer.containerMenu.incrementStateId(), from.getIndex(), inventory.getItem(from.getIndex())));
            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(serverPlayer.containerMenu.containerId, serverPlayer.containerMenu.incrementStateId(), to.getIndex(), inventory.getItem(to.getIndex())));
        }
    }

    public void drop(boolean entireStack) {
        ((ServerPlayer) bot.getVanillaPlayer()).drop(entireStack);
    }

    public void startAction() {
    }

    public void endAction() {
    }

    public Inventory getVanilla() {
        return inventory;
    }
}
