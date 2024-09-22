package blade.utils.blade;

import blade.Bot;
import blade.planner.score.ScoreGoal;

public abstract class BladeGoal implements ScoreGoal {
    protected Bot bot;

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public abstract void tick();
}
