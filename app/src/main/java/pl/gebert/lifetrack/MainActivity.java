package pl.gebert.lifetrack;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

import pl.gebert.lifetrack.data.SensorData;

public class MainActivity extends Activity implements OnClickListener,SensorEventListener {

    //Managers
    private SensorManager sensorManager;
    private PowerManager.WakeLock wakeLock;

    //UI Elements
    private Button buttonStart;
    private Button buttonStop;
    private Button buttonSave;
    private Button buttonReset;
    private Button buttonFiles;
    private TextView stepCount;

    //File saving fields
    private File file;
    private ProgressDialog progressBar;
    private int progressBarStatus = 0;
    private Handler progressBarHandler = new Handler();
    private static final String fileNameSuffix = "_lt.csv";
    private LinkedList<SensorData> collectedData = new LinkedList<>();

    //Step fields
    private float startStepCount;
    private float actualStepCount;
    private boolean isFirstStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        buttonStart = findViewById(R.id.buttonStart);
        buttonStop = findViewById(R.id.buttonStop);
        buttonSave = findViewById(R.id.buttonSave);
        buttonReset = findViewById(R.id.buttonReset);
        buttonFiles = findViewById(R.id.buttonFiles);
        stepCount = findViewById(R.id.stepCount);

        buttonStart.setOnClickListener(this);
        buttonStop.setOnClickListener(this);
        buttonSave.setOnClickListener(this);
        buttonReset.setOnClickListener(this);
        buttonFiles.setOnClickListener(this);

        buttonStart.setEnabled(true);
        buttonStop.setEnabled(false);
        buttonSave.setEnabled(false);
        buttonReset.setEnabled(false);
        buttonFiles.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonStart:
                buttonStartPressed();
                break;
            case R.id.buttonStop:
                buttonStopPressed();
                break;
            case R.id.buttonSave:
                buttonSavePressed();
                break;
            case R.id.buttonReset:
                buttonResetPressed();
                break;
            case R.id.buttonFiles:
                buttonFilesPressed();
                break;
            default:
                break;
        }
    }

    private void buttonStartPressed() {
        buttonStart.setEnabled(false);
        buttonStop.setEnabled(true);
        buttonSave.setEnabled(false);
        buttonReset.setEnabled(false);
        wakeLock = createWakeLock();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        isFirstStep = true;
        actualStepCount = 0.0f;
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);
        file = new File(getExternalFilesDir(null), generateFileName());
        return;
    }

    private void buttonStopPressed() {
        buttonStart.setEnabled(true);
        buttonStop.setEnabled(false);
        buttonSave.setEnabled(true);
        buttonReset.setEnabled(true);
        sensorManager.unregisterListener(this);
        wakeLock.release();
    }
    private void buttonSavePressed() {
        buttonStart.setEnabled(true);
        buttonStop.setEnabled(false);
        buttonSave.setEnabled(false);
        buttonReset.setEnabled(false);
        saveCollectedData();
    }

    private void buttonResetPressed() {
        buttonStart.setEnabled(true);
        buttonStop.setEnabled(false);
        buttonSave.setEnabled(false);
        buttonReset.setEnabled(false);
        collectedData.clear();
    }

    private void buttonFilesPressed() {
        Intent filesIntent = new Intent(this, FilePickActivity.class);
        startActivity(filesIntent);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0]; // Acceleration force along the x axis m/s^2
            float y = event.values[1]; // Acceleration force along the y axis m/s^2
            float z = event.values[2]; // Acceleration force along the z axis m/s^2
            collectedData.add(new SensorData(x, y, z, actualStepCount));

        }
         else if(sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            if(isFirstStep){
                startStepCount = (int) event.values[0];
                isFirstStep = false;
            }
            actualStepCount = event.values[0] - startStepCount;
            stepCount.setText(String.valueOf(actualStepCount));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @NonNull
    private String generateFileName(){
        String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        date += "_" + UUID.randomUUID().toString().replace("-","").substring(0,10);
        return date + fileNameSuffix;
    }

    private void saveCollectedData(){
        prepareProgressBar();
        progressBar.show();

        new Thread(new Runnable() {
            public void run() {
                file = new File(getExternalFilesDir(null), generateFileName());
                try {
                    saveActivityFile();
                } catch (InterruptedException e) {
                    Log.e("MainActivity",
                            "The thread is interrupted: " + e.getMessage());
                } catch (IOException e) {
                    Log.e("MainActivity",
                            "The  file can't be created: " + e.getMessage());
                } catch (Exception e) {
                    Log.e("MainActivity",
                            "Unexpected exception " + e.getMessage());
                }
                progressBar.dismiss();
            }
        }).start();

    }

    private void saveActivityFile() throws IOException, InterruptedException {
        double dataSize = collectedData.size();
        double progress = 1;
        for(SensorData sd : collectedData){
            progressBarStatus = (int) (progress/dataSize * 100);
            progress++;
            Files.append(sd.toString(), file, Charset.defaultCharset());

            progressBarHandler.post(new Runnable() {
                public void run() {
                    progressBar.setProgress(progressBarStatus);
                }
            });
        }
        Thread.sleep(1000); //sleep at the end of saving
    }

    private void prepareProgressBar() {
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Save in progress...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBarStatus = 0;
    }

    private PowerManager.WakeLock createWakeLock(){
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "accel_wake_lock");
        wakeLock.acquire();
        return wakeLock;
    }

}
