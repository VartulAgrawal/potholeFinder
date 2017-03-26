package vartul.makeithappen.potholefinder;

import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Vartul on 14/03/2017.
 */
public class TestNeuralNet {

    @Test
    public void testEvaluation() throws MalformedURLException {
        URL fileURL = new URL("file:http://potholefinder.5gbfree.com/MyMultiLayerNetwork.zip");
        File neuralNetwork = new File(fileURL.getFile());
        MultiLayerNetwork restoredNetwork = null;
        try {
            restoredNetwork = ModelSerializer.restoreMultiLayerNetwork(neuralNetwork);
        } catch (IOException e) {
            e.printStackTrace();
        }

        INDArray features = Nd4j.zeros(3);
        INDArray predicted = Nd4j.zeros(1);
        INDArray output = Nd4j.zeros(1);

        features.putScalar(new int[] {0,0}, 1.65887);
        features.putScalar(new int[] {0,1}, 8.88990);
        features.putScalar(new int[] {0,2}, 8.90876);
        output.putScalar(new int[] {0,0}, 1);

        Evaluation evaluation = new Evaluation(1);

        // Load model
        predicted = restoredNetwork.output(features, false);
        System.out.println(predicted);
        evaluation.eval(output,predicted);
        System.out.println(evaluation.accuracy());


    }
}
