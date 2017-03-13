package vartul.makeithappen.potholefinder;

import android.app.Activity;
import android.os.AsyncTask;

import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.BufferedReader;
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

public class NeuralNetwork extends Activity{

    private class createAndUseNetwork extends AsyncTask<INDArray, Void, String> {

        @Override
        protected String doInBackground(INDArray... params) {
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

            DenseLayer outputLayer = new DenseLayer.Builder()
                    .nIn(3)
                    .nOut(1)
                    .name("Output")
                    .build();

            NeuralNetConfiguration.Builder nncBuilder = new NeuralNetConfiguration.Builder();
            nncBuilder.iterations(10000);
            nncBuilder.learningRate(0.01);

            NeuralNetConfiguration.ListBuilder listBuilder = nncBuilder.list();
            listBuilder.layer(0, inputLayer);
            listBuilder.layer(1, hiddenLayer1);
            listBuilder.layer(2, hiddenLayer2);
            listBuilder.layer(3, outputLayer);

            listBuilder.backprop(true);

            MultiLayerNetwork myNetwork = new MultiLayerNetwork(listBuilder.build());
            myNetwork.init();

            final int NUM_SAMPLES = 15;

            INDArray trainingInputs = Nd4j.zeros(NUM_SAMPLES, inputLayer.getNIn());
            INDArray trainingOutput = Nd4j.zeros(NUM_SAMPLES, outputLayer.getNOut());



            return null;
        }
    }
}
