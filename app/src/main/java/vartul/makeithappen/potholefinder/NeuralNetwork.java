package vartul.makeithappen.potholefinder;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Vartul on 11/03/2017.
 */

public class NeuralNetwork extends Activity {

    public NeuralNetwork() {
    }

    public void createAndUseNetwork() throws IOException /*extends AsyncTask<INDArray, Void, String>*/ {
        DenseLayer inputLayer = new DenseLayer.Builder()
                .nIn(3)
                .nOut(4)
                .name("Input")
                .build();

        DenseLayer hiddenLayer1 = new DenseLayer.Builder()
                .nIn(4)
                .nOut(3)
                .name("Hidden1")
                .build();

        DenseLayer hiddenLayer2 = new DenseLayer.Builder()
                .nIn(3)
                .nOut(3)
                .name("Hidden2")
                .build();

        OutputLayer outputLayer = new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nIn(3)
                .nOut(1)
                .name("Output")
                .build();

        NeuralNetConfiguration.Builder nncBuilder = new NeuralNetConfiguration.Builder();
        nncBuilder.iterations(1000);
        nncBuilder.learningRate(0.0000001);
        nncBuilder.activation(Activation.SIGMOID);

        NeuralNetConfiguration.ListBuilder listBuilder = nncBuilder.list();
        listBuilder.layer(0, inputLayer);
        listBuilder.layer(1, hiddenLayer1);
        listBuilder.layer(2, hiddenLayer2);
        listBuilder.layer(3, outputLayer);

        listBuilder.backprop(true).pretrain(false);

        MultiLayerNetwork myNetwork = new MultiLayerNetwork(listBuilder.build());
        myNetwork.init();

        final int NUM_SAMPLES = 5340;

        INDArray trainingInputs = Nd4j.zeros(NUM_SAMPLES, inputLayer.getNIn());
        INDArray trainingOutput = Nd4j.zeros(NUM_SAMPLES, outputLayer.getNOut());

        ReadInput readInput = null;
        try {
            readInput = new ReadInput(new File(Environment.getExternalStorageDirectory(), "Acceleration.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Float[][] inputData = readInput.getInputAsArray();

            for (int i = 0; i < 5340; i++) {
                for (int j = 0; j < 3; j++) {
                    trainingInputs.putScalar(new int[]{i, j}, inputData[i][j]);
                }
                trainingOutput.putScalar(new int[]{i, 0}, inputData[i][3]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        DataSet myData = new DataSet(trainingInputs, trainingOutput);

        myNetwork.fit(myData);

        // save the model
        File locationToSave = new File(Environment.getExternalStorageDirectory(), "my_multi_layer_networkork.zip");      //Where to save the network. Note: the file is in .zip format - can be opened externally
        //Updater: i.e., the state for Momentum, RMSProp, Adagrad etc. Save this if you want to train your network more in the future
        ModelSerializer.writeModel(myNetwork, locationToSave, true);

    }
}
