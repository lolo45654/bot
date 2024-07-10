package blade.impl.action.attack.totem;

import blade.BladeMachine;
import blade.Bot;
import blade.impl.action.attack.Attack;
import blade.inventory.BotInventory;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import net.minecraft.world.item.Items;

public interface Totem extends Attack {
    static void register(BladeMachine blade) {
        blade.addAction(new SelectTotem());
        blade.addAction(new EquipTotem());
        blade.addAction(new EquipHotBarTotem());
    }

    default double getTotemScore(Bot bot) {
        BotInventory inv = bot.getInventory();
        return inv.findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING)) == null ? -24 : 0;
    }

    default Slot getInventoryTotemSlot(Bot bot) {
        return bot.getInventory().findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING), SlotFlag.MAIN, SlotFlag.ARMOR);
    }

    default Slot getHotBarTotemSlot(Bot bot) {
        return bot.getInventory().findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING), SlotFlag.HOT_BAR);
    }
}
