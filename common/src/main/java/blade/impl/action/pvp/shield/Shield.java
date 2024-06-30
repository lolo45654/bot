package blade.impl.action.pvp.shield;

import blade.BladeMachine;
import blade.Bot;
import blade.impl.action.pvp.PvP;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import net.minecraft.world.item.Items;

public interface Shield extends PvP {
    static void register(BladeMachine blade) {
        blade.addAction(new UseShield());
    }

    default Slot getShieldSlot(Bot bot) {
        return bot.getInventory().findFirst(stack -> stack.is(Items.SHIELD), SlotFlag.OFF_HAND, SlotFlag.HOT_BAR, SlotFlag.MAIN);
    }
}
