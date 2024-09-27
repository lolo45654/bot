package blade.ai;

import blade.utils.BotMath;

import java.util.Random;

public record NeuralNetwork(Layer[] layers) {
    public static NeuralNetwork ofRandom(Random random, int... layerNodes) {
        if (layerNodes.length < 2) throw new IllegalArgumentException("at least two layers");
        Layer[] layers = new Layer[layerNodes.length - 1];
        for (int i = 1; i < layerNodes.length; i++) {
            int nodes = layerNodes[i];
            int lastNodes = layerNodes[i - 1];
            layers[i - 1] = Layer.ofRandom(lastNodes, nodes, random);
        }
        return new NeuralNetwork(layers);
    }

    public double[] predict(double[] inputs) {
        Layer lastLayer = Layer.ofInput(inputs);
        for (int layerIdx = 0; layerIdx < layers.length; layerIdx++) {
            Layer layer = layers[layerIdx];
            if (lastLayer.nodes() != layer.weights().length) throw new IllegalStateException(String.format("output of last layer does not match current input layer (last size: %s, current size: %s, layer idx: %s)", lastLayer.nodes(), layer.weights().length, layerIdx));

            int nodes = layer.nodes();
            for (int node = 0; node < nodes; node++) {
                double sum = 0.0;
                for (int lastNode = 0; lastNode < lastLayer.nodes(); lastNode++) {
                    layer.inputs()[lastNode] = lastLayer.outputs()[lastNode];
                    try {
                        sum += lastLayer.outputs()[lastNode] * layer.weights()[lastNode][node];
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        throw new IllegalStateException(String.format("last size: %s, current size: %s, layer idx: %s, lastnode: %s, node: %s", lastLayer.nodes(), layer.weights().length, layerIdx, lastNode, node), ex);
                    }
                }
                sum += layer.biases()[node];
                layer.outputs()[node] = BotMath.sigmoid(sum);
            }
            lastLayer = layer;
        }
        return lastLayer.outputs();
    }

    public void learnFromTarget(double[] target, double learningRate) {
        double[] prediction = layers[layers.length - 1].outputs();
        double[] error = new double[target.length];
        for (int i = 0; i < target.length; i++) {
            error[i] = target[i] - prediction[i];
        }

        learnFromError(error, learningRate);
    }

    public void learnFromError(double[] error, double learningRate) {
        Layer lastLayer = layers[layers.length - 1];
        for (int layerIdx = layers.length - 1; layerIdx >= 0; layerIdx--) {
            Layer layer = layers[layerIdx];
            double[] nextError = new double[layer.weights().length];

            for (int node = 0; node < layer.nodes(); node++) {
                double gradient = error[node];

                for (int inputNode = 0; inputNode < layer.weights().length; inputNode++) {
                    try {
                        nextError[inputNode] += layer.weights()[inputNode][node] * gradient;
                        layer.weights()[inputNode][node] += learningRate * gradient * layer.inputs()[inputNode];
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        throw new IllegalStateException(String.format("last size: %s, current size: %s, layer idx: %s, inputNode: %s, node: %s", lastLayer.nodes(), layer.weights().length, layerIdx, inputNode, node), ex);
                    }
                }

                layer.biases()[node] += learningRate * gradient;
            }

            error = nextError;
            lastLayer = layer;
        }
    }
}
