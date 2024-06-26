package blade.util.blade;

import blade.Bot;
import blade.planner.score.ScoreGoal;

public abstract class BladeGoal implements ScoreGoal {
    protected final String name;
    protected Bot bot;

    public BladeGoal(String name) {
        this.name = name;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    @Override
    public String getName() {
        return name;
    }
}
