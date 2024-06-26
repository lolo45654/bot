package blade.planner.score;

import blade.state.BladeState;

public interface ScoreGoal {
    double getScore(BladeState state, BladeState difference);

    String getName();

    void tick();
}
