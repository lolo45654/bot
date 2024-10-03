package blade.ai;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

public final class AI {
    public final NeuralNetwork actor;
    public final NeuralNetwork critic;
    private double[] lastInputs;
    private double lastReward = 0;
    private double lastPredictedReward = 0;
    private double gamma = 0.9;

    public AI(NeuralNetwork actor, NeuralNetwork critic, double[] lastInputs) {
        this.actor = actor;
        this.critic = critic;
        this.lastInputs = lastInputs;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public static AI of(int... layerNodes) {
        if (layerNodes.length < 2) throw new IllegalArgumentException("at least 1 layers");
        Random random = new Random();
        return new AI(NeuralNetwork.ofRandom(random, layerNodes), NeuralNetwork.ofRandom(random, layerNodes), null);
    }

    public double[] predict(double[] inputs) {
        lastInputs = inputs;
        return actor.predict(inputs);
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

    public void learn(double reward, double[] inputs, double score, double learningRate) {
        predict(inputs);
        double predictedReward = critic.predict(lastInputs)[0];
        critic.learnFromTarget(new double[] { lastReward + gamma * predictedReward }, learningRate);
        actor.learnFromTarget(new double[] { score }, learningRate);
        lastReward = reward;
        lastPredictedReward = predictedReward;
    }

    public void printInfo() {
        System.out.println("--- Actor:");
        actor.printInfo();
        System.out.println("--- Critic:");
        critic.printInfo();
    }

    public static void main(String[] args) {
        AI ai = AI.of(3, 192, 192, 192, 1);
        ai.printInfo();

        System.out.println("\n".repeat(2));

        double previousAction = 0;
        for (int i = 0; i < 10000; i++) {
            double[] state = { Math.random(), Math.random(), Math.random() };
            double action = ai.predict(state)[0];

            // double reward = action < 0.5 ? 1 : -1;
            // double reward = action > 0.5 ? 1 : -1;
            double reward = action > 0.4 && action < 0.6 ? 1 : Math.min(Math.abs(action - 0.4), Math.abs(action - 0.6)) / 2;

            previousAction = reward;

            ai.learn(reward, 0.01);
            if (i % 1000 == 0) {
                System.out.printf("Action: %.3f\t\tReward: %.3f%n", action, reward);
            }
        }

        System.out.println("\n".repeat(2));

        ai.printInfo();
    }
}
