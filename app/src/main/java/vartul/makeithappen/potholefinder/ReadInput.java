package vartul.makeithappen.potholefinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


/**
 * Created by Vartul on 13/03/2017.
 */

class ReadInput {
    
    private File file;
    ReadInput(File file) throws IOException {
        this.file = file;
    }
    
    Float[][] getInputAsArray() throws IOException {
        /// "/Users/Vartul/Desktop/Acceleration.csv"
        Float[][] output = new Float[5340][4];
        BufferedReader read = null;
        read = new BufferedReader(new FileReader(file));
        String[][] values = new String[5340][4];

        String line;
        int a=0;
        while ((line = read.readLine()) != null) {
            values[a] = line.split(",");
            for (int b = 0; b < 4; b++) {
                output[a][b] = Float.parseFloat(values[a][b]);
            }
            a++;
        }
        read.close();

    return output;
    }
}