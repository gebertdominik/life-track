package pl.gebert.lifetrack;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import pl.gebert.lifetrack.data.SensorData;

public class MainActivity extends Activity implements OnClickListener,SensorEventListener {

    private static final String fileNameSuffix = "_lt.csv";

    private SensorManager sensorManager;
    private Button buttonStart;
    private Button buttonStop;
    private Button buttonSave;
    private Button buttonReset;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonReset = (Button) findViewById(R.id.buttonReset);
        buttonStart.setOnClickListener(this);
        buttonStop.setOnClickListener(this);
        buttonSave.setOnClickListener(this);
        buttonReset.setOnClickListener(this);
        buttonStart.setEnabled(true);
        buttonStop.setEnabled(false);
        buttonSave.setEnabled(false);
        buttonReset.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonStart:
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);
                buttonSave.setEnabled(false);
                buttonReset.setEnabled(false);
                Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_UI);
                file = new File(getExternalFilesDir(null), generateFileName());
                break;
            case R.id.buttonStop:
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
                buttonSave.setEnabled(true);
                buttonReset.setEnabled(true);
                sensorManager.unregisterListener(this);
                break;
            case R.id.buttonSave:
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
                buttonSave.setEnabled(false);
                buttonReset.setEnabled(false);
                break;
            case R.id.buttonReset:
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
                buttonSave.setEnabled(false);
                buttonReset.setEnabled(false);
                break;
            default:
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0]; // Acceleration force along the x axis (including gravity) m/s^2
        float y = event.values[1]; // Acceleration force along the y axis (including gravity) m/s^2
        float z = event.values[2]; // Acceleration force along the z axis (including gravity) m/s^2
        SensorData data = new SensorData(x,y,z);
        try {
            Files.append(data.toString() + "\n", file, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private String generateFileName(){
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return date + fileNameSuffix;
    }
}
