package blade.debug;

import blade.util.blade.BladePlannedAction;

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
