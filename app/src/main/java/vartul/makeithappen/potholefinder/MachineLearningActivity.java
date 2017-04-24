package vartul.makeithappen.potholefinder;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.json.JSONException;
import org.json.JSONObject;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MachineLearningActivity extends AppCompatActivity implements SensorEventListener, OnChartValueSelectedListener {

    // gps variables
    private Button locationButton;
    private TextView viewLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude;
    private double longitude;

    // sensor variables
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean initialized;
    private double lastX, lastY, lastZ;
    private static final double NOISE =  0.2;

    // graph variables
    private LineChart mChart;
    private LineDataSet setX;
    private LineDataSet setY;
    private LineDataSet setZ;
    private float count;
    private long currentTime;

    // database
    private static final String URL= "http://potholefinder.byethost11.com/potholes.php";
    private RequestQueue requestQueue;
    private StringRequest request;

    private PotholeEntryTask mPotholeTask;

    private MultiLayerNetwork restoredNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_learning);

        //File neuralNetwork = new File(Environment.getExternalStorageDirectory(), "MyMultiLayerNetwork.zip");
        URL fileURL = null;
        try {
            fileURL = new URL("http://potholefinder.byethost11.com/MyMultiLayerNetwork.zip");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        File neuralNetwork = new File(Environment.getExternalStorageDirectory(), "MyMultiLayerNetwork.zip");

        NeuralNet asyncNet = new NeuralNet(fileURL, neuralNetwork);
        asyncNet.execute();

        try {
            restoredNetwork = ModelSerializer.restoreMultiLayerNetwork(neuralNetwork);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FloatingActionButton cameraButton = (FloatingActionButton) findViewById(R.id.cameraButtonMainScreen);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    goToCamera();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        locationButton = (Button) findViewById(R.id.button);
        viewLocation = (TextView) findViewById(R.id.textView);

        configureButton();

        // default value
        if (latitude == 0.0 || longitude == 0.0) {
            latitude = 53.467778;
            longitude = -2.233863;
            viewLocation.setText("\nLatitude: " + latitude + "\nLongitude: " + longitude);
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                viewLocation.setText("\nLatitude: " + latitude + "\nLongitude: " + longitude);
                //configureButton();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        requestLocation();

        initialized = false;

        // accelerometer sensor data
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        count = 0;
        currentTime = System.currentTimeMillis();
        mChart = (LineChart) findViewById(R.id.lineChart);
        initializeGraph();

        Button changeToZThreshold = (Button) findViewById(R.id.machineLearning);
        changeToZThreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent zThreshold = new Intent(getApplicationContext(), MainScreen.class);
                zThreshold.putExtra("email", getIntent().getStringExtra("email"));
                startActivity(zThreshold);
            }
        });

        Button potholesReported = (Button) findViewById(R.id.potholesReported);
        potholesReported.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewPotholes = new Intent(getApplicationContext(), UserPotholesActivity.class);
                viewPotholes.putExtra("email", getIntent().getStringExtra("email"));
                startActivity(viewPotholes);
            }
        });

        requestQueue = Volley.newRequestQueue(this);
    }

    private void initializeGraph() {
        mChart.setOnChartValueSelectedListener(this);
        mChart.getDescription().setEnabled(false);
        mChart.setNoDataText("No data!!");

        mChart.setTouchEnabled(true);

        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // enable pinch zoom to avoid scaling x and y axis separately
        mChart.setPinchZoom(true);

        mChart.setBackgroundColor(Color.BLACK);
        setX = createSet(Color.BLUE, "Acceleration Dir X");
        setY = createSet(Color.RED, "Acceleration Dir Y");
        setZ = createSet(Color.YELLOW, "Acceleration Dir Z");

        Legend legend = mChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.WHITE);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextColor(Color.BLUE);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLUE);
        leftAxis.setAxisMaximum(40f);
        leftAxis.setAxisMinimum(-40f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void goToCamera() throws IOException {
        Intent cameraIntent = new Intent(getApplicationContext(), CameraActivity.class);
        startActivity(cameraIntent);
    }

    private void goToMapScreen() {
        Intent mapIntent = new Intent(getApplicationContext(), MapsActivity.class);
        mapIntent.putExtra("Latitude", latitude);
        mapIntent.putExtra("Longitude", longitude);
        startActivity(mapIntent);
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.INTERNET, android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            //configureButton();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    //configureButton();
                }
        }
    }

    private void configureButton() {
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                goToMapScreen();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private INDArray features = Nd4j.zeros(3);
    private INDArray predicted = Nd4j.zeros(1);
    private INDArray output = Nd4j.zeros(1);
    private static final int numberOfOutputs = 1;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            long lastTime = currentTime;
            currentTime = System.currentTimeMillis();

            DecimalFormat vectorFormat = new DecimalFormat();
            vectorFormat.setMaximumFractionDigits(1);

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            features.putScalar(new int[] {0,0}, x);
            features.putScalar(new int[] {0,1}, y);
            features.putScalar(new int[] {0,2}, z);
            output.putScalar(new int[] {0,0}, 1);

            Evaluation evaluation = new Evaluation(numberOfOutputs);

            // Load model
            predicted = restoredNetwork.output(features, false);
            System.out.println(predicted);
            evaluation.eval(output,predicted);
            double accuracy = evaluation.accuracy();
            System.out.println(accuracy);

            /*addEntry(count, x, y, z);
            count+=0.1;*/

            if (!initialized) {
                lastX = x;
                lastY = y;
                lastZ = z;
                //accVector.setText("0.0");
                initialized = true;
            } else {
                double deltaX = lastX - x;
                double deltaY = lastY - y;
                double deltaZ = lastZ - z;

                if (deltaX < NOISE) deltaX = 0.0;
                if (deltaY < NOISE) deltaY = 0.0;
                if (deltaZ < NOISE) deltaZ = 0.0;

                // Detect pothole based on Z threshold
                if (accuracy == 1 && deltaZ >11) {
                    mPotholeTask = new PotholeEntryTask(getIntent().getStringExtra("email"), x, y, z, (float)latitude, (float)longitude);
                    mPotholeTask.execute((Void) null);
                    goToMapScreen();
                    accuracy = 0;
                }

                lastX = x;
                lastY = y;
                lastZ = z;

                addEntry(count, deltaX, deltaY, deltaZ);
                count+=0.5;

                //accVector.setText(vectorFormat.format(deltaX + deltaY + deltaZ));
            }
            //accVector.setText(vectorFormat.format(event.values[0] + event.values[1] + event.values[2]));

        }
    }

    public class NeuralNet extends AsyncTask<Void, Void, Void> {

        private final URL url;
        private final File file;

        public NeuralNet(URL url, File file) {
            this.url = url;
            this.file = file;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                FileUtils.copyURLToFile(url, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private boolean success = true;

    /**
     * Represents an asynchronous pothole location upload task
     */
    public class PotholeEntryTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final Float mAccX;
        private final Float mAccY;
        private final Float mAccZ;
        private final Float mLatitude;
        private final Float mLongitude;


        PotholeEntryTask(String email, Float accX, Float accY, Float accZ, Float latitude, Float longitude) {
            mEmail = email;
            mAccX = accX;
            mAccY = accY;
            mAccZ = accZ;
            mLatitude = latitude;
            mLongitude = longitude;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        success = jsonObject.names().get(0).equals("success");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("email", mEmail);
                    hashMap.put("aacX", mAccX.toString());
                    hashMap.put("accY", mAccY.toString());
                    hashMap.put("accZ", mAccZ.toString());
                    hashMap.put("latitude", mLatitude.toString());
                    hashMap.put("longitude", mLongitude.toString());

                    return hashMap;
                }
            };

            requestQueue.add(request);

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mPotholeTask = null;

            if (success) {
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mPotholeTask = null;
        }
    }

    private void addEntry(float count, double x, double y, double z) {

        ArrayList<ILineDataSet> lines = new ArrayList<>();

        // setX.addEntry(...); // can be called as well

        setX.addEntry(new Entry(count, (float) x));
        lines.add(setX);
        setY.addEntry(new Entry(count, (float) y));
        lines.add(setY);
        setZ.addEntry(new Entry(count, (float) z));
        lines.add(setZ);

        mChart.setData(new LineData(lines));

        // let the chart know it's data has changed
        mChart.notifyDataSetChanged();

        // limit the number of visible entries
        mChart.setVisibleXRangeMaximum(120);
        // mChart.setVisibleYRange(30, AxisDependency.LEFT);

        // move to the latest entry
        mChart.moveViewToX(mChart.getData().getEntryCount());

        // this automatically refreshes the chart (calls invalidate())
        // mChart.moveViewTo(data.getXValCount()-7, 55f,
        // AxisDependency.LEFT);
    }

    private LineDataSet createSet(int color, String description) {

        LineDataSet set = new LineDataSet(null, description);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawCircles(false);
        set.setLineWidth(2f);
        set.setColor(color);
        set.setHighLightColor(color);
        set.setDrawValues(false);
        return set;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
