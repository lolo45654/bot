package blade.debug;

import blade.debug.planner.ScorePlannerDebug;
import blade.planner.score.ScoreState;

import java.util.ArrayList;
import java.util.List;

public class DebugFrame {
    private final List<ReportError> errors = new ArrayList<>();

    private ScoreState state;
    private ScorePlannerDebug planner = new ScorePlannerDebug();

    public List<ReportError> getErrors() {
        return errors;
    }

    public void addError(ReportError error) {
        errors.add(error);
    }

    public ScoreState getState() {
        return state;
    }

    public void setState(ScoreState state) {
        this.state = state;
    }

    public ScorePlannerDebug getPlanner() {
        return planner;
    }
}
