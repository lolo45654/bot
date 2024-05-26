package me.loloed.bot.api.blade.impl.goal;

import me.loloed.bot.api.blade.BladeGoal;
import me.loloed.bot.api.blade.impl.StateKeys;
import me.loloed.bot.api.blade.state.BladeState;

public class KillTargetGoal extends BladeGoal {

    public KillTargetGoal() {
        super("kill_entity");
    }

    @Override
    public double getScore(BladeState state, BladeState difference) {
        double score = 0;
        score += -difference.getValue(StateKeys.TARGET_HEALTH) * 4;
        return score;
    }
}
