package blade.impl.action.attack.totem;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.inventory.BotInventory;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.util.blade.BladeAction;
import net.minecraft.world.item.Items;

public class EquipHotBarTotem extends BladeAction implements Totem {
    @Override
    public void onTick() {
        BotInventory inv = bot.getInventory();
        Slot totemSlot = getInventoryTotemSlot(bot);
        if (totemSlot == null) return;
        if (tick == 0) {
            inv.openInventory();
        }
        if (tick >= ConfigKeys.getDifficultyReversed(bot) * 3) {
            inv.move(totemSlot, Slot.fromHotBar(1), true);
        }
    }

    @Override
    public boolean isSatisfied() {
        return true;
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.DOUBLE_HAND_TOTEM, 1);
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        BotInventory inv = bot.getInventory();
        Slot hotBarSlot = inv.findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING), SlotFlag.HOT_BAR);
        return getTotemScore(bot) +
                (hotBarSlot == null ? 0.8 : -10);
    }

    @Override
    public void onRelease(BladeAction next) {
        super.onRelease(next);
        bot.getInventory().closeInventory();
    }
}
