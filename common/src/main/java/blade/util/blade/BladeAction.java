package blade.util.blade;

import blade.Bot;
import blade.planner.score.ScoreAction;
import blade.planner.score.ScoreState;
import org.slf4j.Logger;

public abstract class BladeAction implements ScoreAction {
    protected ScoreState state = new ScoreState();
    protected Bot bot;
    protected int tick = 0;
    public Logger logger;

    public void setBot(Bot bot) {
        if (logger == null || this.bot != bot) {
            logger = bot.getLogger(getClass().getSimpleName());
        }
        this.bot = bot;
    }

    public void setState(ScoreState state) {
        this.state = state;
    }

    public void prepare() {
    }

    public abstract void onTick();

    public void onRelease(BladeAction next) {
        tick = 0;
    }

    public void postTick() {
        tick++;
    }

    protected void panic(String detailedReason) {
        bot.getBlade().causePanic(detailedReason);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
