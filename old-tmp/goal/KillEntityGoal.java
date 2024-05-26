package me.loloed.bot.api.blade.impl.goal;

import me.loloed.bot.api.blade.BladeGoal;
import me.loloed.bot.api.blade.impl.StateKeys;
import me.loloed.bot.api.blade.state.BladeState;

public class KillEntityGoal extends BladeGoal {

    public KillEntityGoal() {
        super("kill_entity");
    }

    @Override
    public double getScore(BladeState state, BladeState difference) {
        double score = 0;
        score += Math.abs(difference.getValue(StateKeys.BOT_HEALTH)) * 2;
        return score;
    }
}
