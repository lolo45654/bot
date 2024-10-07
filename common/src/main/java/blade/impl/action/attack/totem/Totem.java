package blade.impl.action.attack.totem;

import blade.BladeMachine;
import blade.Bot;
import blade.impl.action.attack.Attack;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import net.minecraft.world.item.Items;

public interface Totem extends Attack {
    static void register(BladeMachine blade) {
        blade.registerAction(new SelectTotem());
        blade.registerAction(new EquipOffhandTotem());
        blade.registerAction(new EquipHotBarTotem());
    }

    static boolean isTotemSatisfied(Bot bot) {
        return bot.getInventory().findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING)) != null;
    }

    static Slot getInventoryTotemSlot(Bot bot) {
        return bot.getInventory().findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING), SlotFlag.MAIN, SlotFlag.ARMOR);
    }

    static Slot getHotBarTotemSlot(Bot bot) {
        return bot.getInventory().findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING), SlotFlag.HOT_BAR);
    }
}
