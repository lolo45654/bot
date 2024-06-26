package blade.impl.action.pvp.totem;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.inventory.BotInventory;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreAction;
import blade.state.BladeState;
import net.minecraft.world.item.Items;

public class EquipTotem extends ScoreAction implements Totem {
    @Override
    public void onTick() {
        BotInventory inv = bot.getInventory();
        Slot inventorySlot = inv.findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING), SlotFlag.MAIN, SlotFlag.ARMOR);
        if (inventorySlot == null) return;
        if (tick == 0) {
            inv.startAction();
        }
        if (tick >= ConfigKeys.getDifficultyReversedCubic(bot) * 0.8) {
            inv.move(inventorySlot, Slot.fromOffHand(), true);
        }
    }

    @Override
    public void getResult(BladeState result) {
        result.setValue(StateKeys.OFF_HAND_TOTEM, 1);
    }

    @Override
    public double getScore() {
        BotInventory inv = bot.getInventory();
        Slot hotBarSlot = inv.findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING), SlotFlag.HOT_BAR);
        return getTotemScore(bot) +
                (inv.getOffHand().is(Items.TOTEM_OF_UNDYING) ? -5 : 0) +
                (hotBarSlot != null && inv.getSelectedSlot() == hotBarSlot.getHotBarIndex() ? 1.4 : 0) +
                (Math.min(tick / 0.7, 5));
    }

    @Override
    public void onRelease(ScoreAction next) {
        super.onRelease(next);
        bot.getInventory().endAction();
    }
}
