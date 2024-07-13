package blade.impl.action.attack.totem;

import blade.impl.StateKeys;
import blade.impl.action.attack.Attack;
import blade.inventory.Slot;
import blade.planner.score.ScoreState;
import blade.util.blade.BladeAction;

import static blade.impl.action.attack.Attack.lookAtEnemy;

public class SelectTotem extends BladeAction implements Totem {
    @Override
    public void onTick() {
        Slot totemSlot = Totem.getHotBarTotemSlot(bot);
        if (totemSlot == null) return;
        lookAtEnemy(bot, tick);
        bot.getInventory().setSelectedSlot(totemSlot.getHotbarIndex());
    }

    @Override
    public boolean isSatisfied() {
        return Attack.isPvPSatisfied(bot);
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.DOUBLE_HAND_TOTEM, 1);
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        return Totem.getTotemScore(bot) +
                Math.min(bot.getVanillaPlayer().getDeltaMovement().y * 2, 0.4) +
                (Totem.getHotBarTotemSlot(bot) == null ? -5 : 0);
    }
}
