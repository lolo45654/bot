package blade.ai;

import java.util.Random;

public record Layer(int nodes, double[][] weights, double[] biases, double[] inputs, double[] outputs) {
    public static Layer ofInput(double[] inputs) {
        return new Layer(inputs.length, new double[0][0], new double[0], new double[0], inputs);
    }

    public static Layer ofRandom(int lastNodes, int nodes, Random random) {
        double[][] weights = new double[lastNodes][nodes];
        double[] biases = new double[nodes];
        double[] inputs = new double[lastNodes];
        double[] outputs = new double[nodes];
        for (int i = 0; i < lastNodes; i++) {
            inputs[i] = random.nextDouble() * 2 - 1;
            for (int j = 0; j < nodes; j++) {
                weights[i][j] = random.nextDouble() * 2 - 1;
            }
        }
        for (int i = 0; i < nodes; i++) {
            biases[i] = random.nextDouble() * 2 - 1;
            outputs[i] = random.nextDouble() * 2 - 1;
        }
        return new Layer(nodes, weights, biases, inputs, outputs);
    }
}
