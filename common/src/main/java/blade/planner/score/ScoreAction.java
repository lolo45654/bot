package blade.planner.score;

import blade.BotAI;

public interface ScoreAction {
    boolean isSatisfied();

    void getResult(ScoreState result);

    double getScore();

    default int[] getHiddenLayers() {
        return BotAI.DEFAULT_HIDDEN_LAYERS;
    }
}
