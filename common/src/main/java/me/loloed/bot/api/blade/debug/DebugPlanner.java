package me.loloed.bot.api.blade.debug;

import me.loloed.bot.api.blade.BladePlannedAction;

public class DebugPlanner {
    private BladePlannedAction<?> action = null;
    private DebugPlanner children = null;

    public BladePlannedAction<?> getAction() {
        return action;
    }

    public void setAction(BladePlannedAction<?> action) {
        this.action = action;
    }

    public void setChildren(DebugPlanner children) {
        this.children = children;
    }

    public DebugPlanner getChildren() {
        return children;
    }
}
