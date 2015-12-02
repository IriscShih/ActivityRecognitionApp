package shih.iris.com.activityRecognitionApp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private SensorManager            sensorManager;
    private Sensor                   sensor;
    private Button                   btn;
    private EditText                 etOffset;
    private EditText                 etDuration;
    private TextView                 tvResult;
    private TextView                 output;
    private long                     buttonPressedTime;
    private long                     offset;
    private long                     duration;
    private ActivityAlgorithm algorithm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        output = (TextView)findViewById(R.id.output);
        output.setText("Checkout Your Activity!");

        algorithm = new ActivityAlgorithm(this);

        btn = (Button) findViewById(R.id.cmdRecord);
        btn.setOnClickListener(this);

        etOffset = (EditText) findViewById(R.id.startTimeOffset);
        etDuration = (EditText) findViewById(R.id.recordDuration);
        tvResult = (TextView) findViewById(R.id.result);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

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


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (buttonPressedTime == 0) {
            buttonPressedTime = event.timestamp;
        } else if (event.timestamp > buttonPressedTime + offset && event.timestamp <= buttonPressedTime + offset + duration) {
            System.out.println("start recording");
            algorithm.addData(new ActivityAlgorithm.SensorData(event.timestamp, event.values[0], event.values[1], event.values[2]));
        } else if (event.timestamp > buttonPressedTime + offset + duration) {
            sensorManager.unregisterListener(this);
            displayResult();
            System.out.println("end recording");
        }
    }

    private void displayResult() {
        tvResult.setText(algorithm.calculate() ? "You are running" : "You are not running");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        if (v == btn) {
            offset = Long.valueOf(etOffset.getText().toString())*1000000;
            duration = Long.valueOf(etDuration.getText().toString())*1000000;
            algorithm.clearData();
            buttonPressedTime = 0;

            tvResult.setText("start recording");
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        }
    }
}