package blade.impl.action.pvp.totem;

import blade.impl.StateKeys;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreAction;
import blade.state.BladeState;
import net.minecraft.world.item.Items;

public class SelectTotem extends ScoreAction implements Totem {
    public Slot getTotemSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING), SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot totemSlot = getTotemSlot();
        if (totemSlot == null) return;
        bot.getInventory().setSelectedSlot(totemSlot.getHotBarIndex());
    }

    @Override
    public void getResult(BladeState result) {
        result.setValue(StateKeys.DOUBLE_HAND_TOTEM, 1);
    }

    @Override
    public double getScore() {
        return getTotemScore(bot) +
                Math.min(bot.getVanillaPlayer().getDeltaMovement().y * 4, 2) + (getTotemSlot() == null ? -5 : 0);
    }
}
