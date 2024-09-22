package blade.debug;

import blade.debug.planner.ScorePlannerDebug;
import blade.debug.visual.VisualDebug;
import blade.planner.score.ScoreState;

import java.util.List;

public record DebugFrame(List<ReportError> errors, ScoreState state, ScorePlannerDebug planner, List<VisualDebug> visuals) {
}
