package blade.util.blade;

import blade.Bot;
import blade.debug.DebugPlanner;
import blade.state.BladeState;
import org.slf4j.Logger;

public abstract class BladeAction<T extends BladeAction<T>> {
    protected BladeState state = new BladeState();
    protected DebugPlanner parentDebugPlanner;
    protected Bot bot;
    protected int tick = 0;
    public Logger logger;

    public void setBot(Bot bot) {
        if (this.logger == null || this.bot != bot) {
            this.logger = bot.getLogger(getClass().getSimpleName());
        }
        this.bot = bot;
    }

    public void setState(BladeState state) {
        this.state = state;
    }

    public void setParentDebugPlanner(DebugPlanner parentDebugPlanner) {
        this.parentDebugPlanner = parentDebugPlanner;
    }

    public abstract void prepare();

    public abstract void onTick();

    public void onRelease(T next) {
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
