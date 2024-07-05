package blade.planner.score;

public interface ScoreAction {
    boolean isSatisfied();

    void getResult(ScoreState result);

    double getScore();
}
