package me.loloed.bot.api.blade;

import me.loloed.bot.api.Bot;
import me.loloed.bot.api.blade.debug.DebugPlanner;
import me.loloed.bot.api.blade.state.BladeState;

public abstract class BladeAction<T extends BladeAction<T>> {
    protected BladeState state = new BladeState();
    protected DebugPlanner parentDebugPlanner;
    protected Bot bot;
    protected int tick = 0;

    public void setBot(Bot bot) {
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
}
