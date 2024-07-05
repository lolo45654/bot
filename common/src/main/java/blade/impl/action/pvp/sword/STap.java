package blade.impl.action.pvp.sword;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.planner.score.ScoreState;
import blade.util.blade.BladeAction;
import net.minecraft.world.entity.LivingEntity;

import java.util.concurrent.ThreadLocalRandom;

public class STap extends BladeAction implements Sword {
    @Override
    public void onTick() {
        lookAtEnemy(bot, tick);

        bot.setMoveBackward(true);
        bot.setMoveForward(false);
    }

    @Override
    public boolean isSatisfied() {
        return isPvPSatisfied(bot);
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        return getSwordScore(bot) +
                target.hurtTime / 6.0 +
                ThreadLocalRandom.current().nextDouble() * 0.6;
    }
}
