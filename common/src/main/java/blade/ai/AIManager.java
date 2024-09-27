package blade.ai;

import blade.Bot;
import blade.planner.score.ScoreAction;
import blade.planner.score.ScorePlanner;
import blade.planner.score.ScoreState;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class AIManager {
    public static final int[] DEFAULT_HIDDEN_LAYERS = new int[] { 48, 48, 24 };

    private final Map<ScoreAction, AI> neuralNetworks = new WeakHashMap<>();
    private final Bot bot;
    private double learningRate = 0.01;
    private State state = State.DISABLED;

    public AIManager(Bot bot) {
        this.bot = bot;
    }

    public void learn(double reward, ScoreState botState, Map<ScoreAction, ScorePlanner.Score> scores) {
        if (state == State.DISABLED) return;
        for (Map.Entry<ScoreAction, AI> entry : neuralNetworks.entrySet()) {
            if (state == State.ONLY_LEARN) {
                entry.getValue().learn(reward, botState, scores.get(entry.getKey()).score(), learningRate);
            } else {
                entry.getValue().learn(reward, learningRate);
            }
        }
        for (AI ai : neuralNetworks.values()) {
        }
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public void setState(State state) {
        this.state = state;
    }

    public AI getOrCreate(ScoreAction action) {
        return neuralNetworks.computeIfAbsent(action, $ -> AI.of(bot, 0, action.getHiddenLayers()));
    }

    public State getState() {
        return state;
    }

    public void copy(AIManager other, List<? extends ScoreAction> newActions) {
        learningRate = other.learningRate;
        state = other.state;
        for (ScoreAction newAction : newActions) {
            AI oldAI = null;
            for (Map.Entry<ScoreAction, AI> entry : other.neuralNetworks.entrySet()) {
                if (entry.getKey().getClass() == newAction.getClass()) {
                    oldAI = entry.getValue();
                }
            }
            if (oldAI != null) neuralNetworks.put(newAction, oldAI);
        }
    }

    public enum State {
        DISABLED,
        ONLY_LEARN,
        ONLY_AI,
        BOTH
        ;

        public State next() {
            State[] values = values();
            int index = Arrays.binarySearch(values, this);
            return values[index >= values.length - 1 ? 0 : index + 1];
        }
    }
}
