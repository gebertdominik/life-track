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

    private static final String fileNameSuffix = "_lt.csv";
    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";

    private SensorManager sensorManager;
    private PowerManager.WakeLock wakeLock;
    private Button buttonStart;
    private Button buttonStop;
    private Button buttonSave;
    private Button buttonReset;
    private Button buttonSettings;
    private Button buttonFiles;
    private TextView stepCount;
    File file;
    ProgressDialog progressBar;
    private int progressBarStatus = 0;
    private Handler progressBarHandler = new Handler();
    private long fileSize = 0;
    private LinkedList<SensorData> collectedData = new LinkedList<SensorData>();

    private float startStepCount;
    private float actualStepCount;
    private boolean isFirstStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
        db.clearData();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonReset = (Button) findViewById(R.id.buttonReset);
        buttonSettings = (Button) findViewById(R.id.buttonSettings);
        buttonFiles = (Button) findViewById(R.id.buttonFiles);

        stepCount = (TextView) findViewById(R.id.stepCount);

        buttonStart.setOnClickListener(this);
        buttonStop.setOnClickListener(this);
        buttonSave.setOnClickListener(this);
        buttonReset.setOnClickListener(this);
        buttonSettings.setOnClickListener(this);
        buttonFiles.setOnClickListener(this);

        buttonStart.setEnabled(true);
        buttonStop.setEnabled(false);
        buttonSave.setEnabled(false);
        buttonReset.setEnabled(false);
        buttonSettings.setEnabled(true);
        buttonFiles.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonStart:
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);
                buttonSave.setEnabled(false);
                buttonReset.setEnabled(false);
                buttonSettings.setEnabled(false);
                wakeLock = createWakeLock();
                Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                Sensor stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                isFirstStep = true;
                actualStepCount = 0.0f;
                sensorManager.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);
                file = new File(getExternalFilesDir(null), generateFileName());
                break;
            case R.id.buttonStop:
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
                buttonSave.setEnabled(true);
                buttonReset.setEnabled(true);
                sensorManager.unregisterListener(this);
                wakeLock.release();
                break;
            case R.id.buttonSave:
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
                buttonSave.setEnabled(false);
                buttonReset.setEnabled(false);
                buttonSettings.setEnabled(true);
                saveCollectedData();
                break;
            case R.id.buttonReset:
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
                buttonSave.setEnabled(false);
                buttonReset.setEnabled(false);
                buttonSettings.setEnabled(true);
                collectedData.clear();
                break;
            case R.id.buttonSettings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.buttonFiles:
                Intent filesIntent = new Intent(this, FilePickActivity.class);
                startActivity(filesIntent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0]; // Acceleration force along the x axis (without gravity) m/s^2
            float y = event.values[1]; // Acceleration force along the y axis (without gravity) m/s^2
            float z = event.values[2]; // Acceleration force along the z axis (without gravity) m/s^2
          //  collectedData.add(new SensorData(x, y, z, actualStepCount)); //TODO actualStepCount ustawiany na 0
            try {
                Files.append(System.currentTimeMillis() + ", " + x + ", " + y + ", " + z + "\n", file, Charset.defaultCharset());
            }catch (Exception e)
            {}

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

    private String generateFileName(){
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        date += "_" + UUID.randomUUID().toString().replace("-","").substring(0,10);
        return date + fileNameSuffix;
    }

    private void saveCollectedData(){
        prepareProgressBar();
        progressBar.show();

        new Thread(new Runnable() {
            public void run() {
                file = new File(getExternalFilesDir(null), generateFileName());
                double dataSize = collectedData.size();
                double progress = 1;
                try {
                    for(SensorData sd : collectedData){
                        progressBarStatus = (int) (progress/dataSize * 100);
                        progress++;
                        Files.append(sd.toString() + "\n", file, Charset.defaultCharset());
                        progressBarHandler.post(new Runnable() {
                            public void run() {
                                progressBar.setProgress(progressBarStatus);
                            }
                        });
                    }
                    Thread.sleep(1000); //sleep at the end of saving
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                progressBar.dismiss();
            }
        }).start();

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
