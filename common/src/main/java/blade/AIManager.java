package blade;

import blade.ai.AI;
import blade.planner.score.ScoreAction;
import blade.planner.score.ScorePlanner;
import blade.planner.score.ScoreState;
import blade.planner.score.StateKey;

import java.util.*;

public class AIManager {
    public static final int[] DEFAULT_HIDDEN_LAYERS = new int[] { 192, 192, 128 };

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
                entry.getValue().learn(reward, produceInputs(botState), scores.get(entry.getKey()).score(), learningRate);
            } else {
                entry.getValue().learn(reward, learningRate);
            }
        }
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public void setState(State state) {
        this.state = state;
    }

    public AI getOrCreate(ScoreAction action) {
        return neuralNetworks.computeIfAbsent(action, $ -> {
            int[] hidden = action.getHiddenLayers();
            int[] layerNodes = new int[hidden.length + 2];
            layerNodes[0] = bot.getBlade().getState().getKeys().size();
            System.arraycopy(hidden, 0, layerNodes, 1, hidden.length);
            layerNodes[layerNodes.length - 1] = 1;
            return AI.of(layerNodes);
        });
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

    public double produceScore(ScoreAction action, ScoreState state) {
        return getOrCreate(action).predict(produceInputs(state))[0];
    }

    public double[] produceInputs(ScoreState state, double... additional) {
        List<StateKey> keys = new ArrayList<>(state.getKeys());
        keys.sort(Comparator.comparing(StateKey::getName, String.CASE_INSENSITIVE_ORDER));
        double[] inputs = new double[keys.size() + additional.length];
        for (int i = 0; i < keys.size(); i++) {
            inputs[i] = state.getValue(keys.get(i));
        }
        System.arraycopy(additional, 0, inputs, keys.size(), additional.length);
        return inputs;
    }

    public enum State {
        DISABLED,
        ONLY_LEARN,
        ONLY_AI,
        BOTH
        ;

        public State next() {
            State[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }
}
