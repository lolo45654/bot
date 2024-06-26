package blade.debug;

import blade.debug.planner.ScoreDebug;
import blade.state.BladeState;

import java.util.ArrayList;
import java.util.List;

public class DebugFrame {
    private final List<ReportError> errors = new ArrayList<>();

    private BladeState state;
    private ScoreDebug planner = new ScoreDebug();

    public List<ReportError> getErrors() {
        return errors;
    }

    public void addError(ReportError error) {
        errors.add(error);
    }

    public BladeState getState() {
        return state;
    }

    public void setState(BladeState state) {
        this.state = state;
    }

    public ScoreDebug getPlanner() {
        return planner;
    }
}
