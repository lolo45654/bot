package blade.impl.goal;

import blade.Bot;
import blade.BotAI;
import blade.ai.AI;
import blade.planner.score.ScoreState;
import blade.utils.blade.BladeGoal;

/**
 * sad attempt at open-ended bots
 */
public class OpenEndedGoal extends BladeGoal {
    private final AI ai;

    public OpenEndedGoal(AI ai) {
        this.ai = ai;
    }

    public OpenEndedGoal(Bot bot) {
         this(AI.of(bot.getBlade().getState().getKeys().size(), 256, 256, 192, 128, 64, 1));
    }

    @Override
    public void tick() {
        ai.learn(Math.random() - 0.5, 0.01);
    }

    @Override
    public double getScore(ScoreState state, ScoreState difference) {
        return 0;
    }

    @Override
    public double getReward() {
        return ai.predict(BotAI.produceInputs(bot.getBlade().getState()))[0];
    }
}
