package blade.impl.action.attack.sword;

import blade.impl.StateKeys;
import blade.planner.score.ScoreState;
import blade.utils.blade.BladeAction;

import static blade.impl.action.attack.Attack.isAttackSatisfied;
import static blade.impl.action.attack.Attack.lookAtEnemy;

public class Jump extends BladeAction implements Sword {
    @Override
    public void onTick() {
        lookAtEnemy(bot, tick);

        bot.setMoveForward(false);
        bot.jump();
    }

    @Override
    public boolean isSatisfied() {
        return isAttackSatisfied(bot);
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        return state.getValue(StateKeys.SWORD_MODE) +
                state.getValue(StateKeys.RECENTLY_HIT_ENEMY) +
                bot.getRandom().nextDouble() * 0.6;
    }
}
