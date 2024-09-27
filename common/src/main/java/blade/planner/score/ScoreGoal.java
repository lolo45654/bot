package blade.planner.score;

public interface ScoreGoal {
    double getScore(ScoreState state, ScoreState difference);

    double getReward();
}
