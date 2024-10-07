package blade.impl.action.attack.shield;

import blade.BladeMachine;
import blade.Bot;
import blade.impl.action.attack.Attack;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import net.minecraft.world.item.Items;

public interface Shield extends Attack {
    static void register(BladeMachine blade) {
        blade.registerAction(new UseShield());
        blade.registerAction(new BreakShield());
    }

    static Slot getShieldSlot(Bot bot) {
        return bot.getInventory().findFirst(stack -> stack.is(Items.SHIELD), SlotFlag.OFF_HAND, SlotFlag.HOT_BAR, SlotFlag.MAIN);
    }
}
