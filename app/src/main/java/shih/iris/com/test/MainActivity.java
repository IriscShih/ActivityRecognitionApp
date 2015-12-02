package shih.iris.com.test;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;


public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private TextView output;
    private SensorManager sensorManager;
    private Sensor sensor;
    private long currentTime;
    private long startTime;

    Button startButton;
    Button stopButton;

    File myFile;
    FileOutputStream fOut;
    OutputStreamWriter myOutWriter;
    BufferedWriter myBufferedWriter;
    PrintWriter myPrintWriter;

    boolean stopFlag = false;
    boolean startFlag = false;
    boolean isFirstSet = true;

    float linear_acceleration[]= new float[3];
    private TextView accX;
    private TextView accY;
    private TextView accZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        output = (TextView)findViewById(R.id.output);
        output.setText("Checkout Your Activity!");

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if(sensor==null){
            Toast.makeText(this,"This device does not have accelerometer!",Toast.LENGTH_LONG).show();
        }
        accX = (TextView)findViewById(R.id.x);
        accY = (TextView)findViewById(R.id.y);
        accZ = (TextView)findViewById(R.id.z);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Is it correct?", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

        });

        // start button
        startButton = (Button) findViewById(R.id.button1);
        startButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                // start recording the sensor data
                try {
                    myFile = new File(Environment.getExternalStorageDirectory()+"/SensorData.txt");
                    myFile.createNewFile();

                    fOut = new FileOutputStream(myFile);
                    myOutWriter = new OutputStreamWriter(fOut);
                    myBufferedWriter = new BufferedWriter(myOutWriter);
                    myPrintWriter = new PrintWriter(myBufferedWriter);

                    Toast.makeText(getBaseContext(), "Start recording the data set", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    startFlag = true;
                }
            }
        });

        // stop button
        stopButton = (Button) findViewById(R.id.button2);
        stopButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                // stop recording the sensor data
                try {
                    stopFlag = true;
                    Toast.makeText(getBaseContext(), "Done recording the data set", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate
        if(startFlag){

            final float alpha = 0.8f;
            float gravity[]= new float[3];
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];//g in X dir
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];//g in Y dir
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];//g in Z dir

            linear_acceleration[0] = event.values[0] - gravity[0];//X
            linear_acceleration[1] = event.values[1] - gravity[1];//Y
            linear_acceleration[2] = event.values[2] - gravity[2];//Z

            if (isFirstSet) {
                startTime = System.currentTimeMillis();
                isFirstSet = false;
            }

            currentTime = System.currentTimeMillis();

            for (int i=0;i<1;i++) {
                if (!stopFlag) {

                    accX.setText(Float.toString(linear_acceleration[0]));
                    accY.setText(Float.toString(linear_acceleration[1]));
                    accZ.setText(Float.toString(linear_acceleration[2]));

                    save(currentTime, startTime, linear_acceleration);
                }

                else {

                    myPrintWriter.flush();
                    myPrintWriter.close();

                    try {
                        myOutWriter.close();
                    } catch (IOException | NullPointerException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    try {
                        fOut.close();
                    } catch (IOException | NullPointerException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private void save(long currentTime, long startTime, float[] linear_acceleration){
        myPrintWriter.write(currentTime - startTime + ", " + linear_acceleration[0] + ", " + linear_acceleration[1] + ", " + linear_acceleration[2] + "\n");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    /**
     * Called when the accuracy of the registered sensor has changed.
     * <p/>
     * <p>See the SENSOR_STATUS_* constants in
     * {@link SensorManager SensorManager} for details.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);

    }
}
