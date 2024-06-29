package blade.impl.action.pvp.totem;

import blade.BladeMachine;
import blade.Bot;
import blade.impl.action.pvp.PvP;
import blade.inventory.BotInventory;
import net.minecraft.world.item.Items;

public interface Totem extends PvP {
    static void register(BladeMachine blade) {
        blade.addAction(new SelectTotem());
        blade.addAction(new EquipTotem());
        blade.addAction(new EquipHotBarTotem());
    }

    default double getTotemScore(Bot bot) {
        BotInventory inv = bot.getInventory();
        return inv.findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING)) == null ? -24 : 0;
    }
}
