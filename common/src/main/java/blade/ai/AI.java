package blade.ai;

import java.nio.ByteBuffer;
import java.util.Random;

public final class AI {
    public final NeuralNetwork actor;
    public final NeuralNetwork critic;
    private double[] lastInputs;
    private double[] lastPolicy;
    private double lastReward = 0;
    private double lastPredictedValue = 0;
    private double gamma = 0.99;
    private double epsilon = 0.2;

    public AI(NeuralNetwork actor, NeuralNetwork critic) {
        this.actor = actor;
        this.critic = critic;
    }

    public static AI of(int... layerNodes) {
        if (layerNodes.length < 2) throw new IllegalArgumentException("at least 1 layers");
        Random random = new Random();
        int[] criticLayers = layerNodes.clone();
        criticLayers[criticLayers.length - 1] = 1;
        return new AI(NeuralNetwork.ofRandom(random, layerNodes), NeuralNetwork.ofRandom(random, criticLayers));
    }

    public static AI read(ByteBuffer buffer) {
        AI ai = new AI(NeuralNetwork.read(buffer), NeuralNetwork.read(buffer));
        ai.setGamma(buffer.getDouble());
        ai.setEpsilon(buffer.getDouble());
        return ai;
    }

    public void write(ByteBuffer buffer) {
        actor.write(buffer);
        critic.write(buffer);
        buffer.putDouble(gamma);
        buffer.putDouble(epsilon);
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public double[] predict(double[] inputs) {
        lastInputs = inputs;
        return actor.predict(inputs);
    }

    public void learn(double reward, double learningRate) {
        if (lastInputs == null) return;
        double predictedValue = critic.predict(lastInputs)[0];
        double advantage = lastReward + gamma * predictedValue - lastPredictedValue;

        double[] newPolicy = actor.layers()[actor.layers().length - 1].outputs();
        if (lastPolicy == null) {
            lastPolicy = newPolicy;
        }

        double[] gradients = new double[lastPolicy.length];
        for (int i = 0; i < gradients.length; i++) {
            double ratio = newPolicy[i] / lastPolicy[i];
            double clippedRatio = Math.max(1 - epsilon, Math.min(1 + epsilon, ratio));
            gradients[i] = Math.min(ratio * advantage, clippedRatio * advantage);
        }

        actor.learnFromError(gradients, learningRate);
        critic.learnFromTarget(new double[] { lastReward + gamma * predictedValue }, learningRate);
        lastReward = reward;
        lastPredictedValue = predictedValue;
        lastPolicy = newPolicy;
    }

    public void learn(double reward, double[] inputs, double[] target, double learningRate) {
        predict(inputs);
        double predictedValue = critic.predict(lastInputs)[0];
        critic.learnFromTarget(new double[] { lastReward + gamma * predictedValue }, learningRate);
        actor.learnFromTarget(target, learningRate);
        lastReward = reward;
        lastPredictedValue = predictedValue;
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
            double reward = action > 0.4 && action < 0.6 ? 1 : Math.min(1 - Math.abs(action - 0.4), 1 - Math.abs(action - 0.6)) / 2;

            previousAction = action;

            ai.learn(reward, 0.01);
            if (i % 1000 == 0) {
                System.out.printf("Action: %.3f\t\tReward: %.3f%n", action, reward);
            }
        }

        System.out.println("\n".repeat(2));

        ai.printInfo();
    }
}
