package blade.planner.score;

import blade.state.BladeState;
import blade.util.blade.BladeAction;

public abstract class ScoreAction extends BladeAction<ScoreAction> {
    @Override
    public void prepare() {
    }

    public abstract void getResult(BladeState result);

    public abstract double getScore();
}
