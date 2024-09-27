package blade.ai;

import blade.Bot;
import blade.planner.score.ScoreState;
import blade.planner.score.StateKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public final class AI {
    public final NeuralNetwork actor;
    public final NeuralNetwork critic;
    private double[] lastInputs;
    private double lastReward = 0;
    private double lastPredictedReward = 0;
    private double gamma = 0.6;

    public AI(NeuralNetwork actor, NeuralNetwork critic, double[] lastInputs) {
        this.actor = actor;
        this.critic = critic;
        this.lastInputs = lastInputs;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public static AI of(Bot bot, int additionalInputs, int... hidden) {
        int[] layerNodes = new int[hidden.length + 2];
        layerNodes[0] = bot.getBlade().getState().getKeys().size() + additionalInputs;
        System.arraycopy(hidden, 0, layerNodes, 1, hidden.length);
        layerNodes[layerNodes.length - 1] = 1;
        Random random = new Random();
        return new AI(NeuralNetwork.ofRandom(random, layerNodes), NeuralNetwork.ofRandom(random, layerNodes), null);
    }

    public double predict(ScoreState state, double... additional) {
        List<StateKey> keys = new ArrayList<>(state.getKeys());
        keys.sort(Comparator.comparing(StateKey::getName, String.CASE_INSENSITIVE_ORDER));
        double[] inputs = new double[keys.size() + additional.length];
        for (int i = 0; i < keys.size(); i++) {
            inputs[i] = state.getValue(keys.get(i));
        }
        System.arraycopy(additional, 0, inputs, keys.size() - 1, additional.length);
        return predict(inputs);
    }

    public double predict(double[] inputs) {
        lastInputs = inputs;
        return actor.predict(inputs)[0];
    }

    public void learn(double reward, double learningRate) {
        if (lastInputs == null) return;
        double predictedReward = critic.predict(lastInputs)[0];
        double tdError = lastReward + gamma * predictedReward - lastPredictedReward;
        critic.learnFromTarget(new double[] { lastReward + gamma * predictedReward }, learningRate);
        actor.learnFromError(new double[] { 2 * (reward - predictedReward) * tdError }, learningRate);
        lastReward = reward;
        lastPredictedReward = predictedReward;
    }

    public void learn(double reward, ScoreState state, double score, double learningRate) {
        predict(state);
        double predictedReward = critic.predict(lastInputs)[0];
        critic.learnFromTarget(new double[] { lastReward + gamma * predictedReward }, learningRate);
        actor.learnFromTarget(new double[] { score }, learningRate);
        lastReward = reward;
        lastPredictedReward = predictedReward;
    }
}
