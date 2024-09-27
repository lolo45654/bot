package blade.planner.score;

import blade.ai.AIManager;

public interface ScoreAction {
    boolean isSatisfied();

    void getResult(ScoreState result);

    double getScore();

    default int[] getHiddenLayers() {
        return AIManager.DEFAULT_HIDDEN_LAYERS;
    }
}
