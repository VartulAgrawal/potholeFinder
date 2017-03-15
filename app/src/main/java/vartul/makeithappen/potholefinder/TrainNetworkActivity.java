package vartul.makeithappen.potholefinder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class TrainNetworkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_network);

        Button button = (Button) findViewById(R.id.train_network);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NeuralNetwork neuralNetwork = new NeuralNetwork();
                try {
                    neuralNetwork.createAndUseNetwork();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
