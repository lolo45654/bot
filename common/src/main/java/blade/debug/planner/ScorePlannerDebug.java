package blade.debug.planner;

import blade.planner.score.ScoreAction;
import blade.planner.score.ScorePlanner;

import java.util.Map;

public record ScorePlannerDebug(double temperature, ScoreAction action, Map<ScoreAction, ScorePlanner.Score> scores) {
}
