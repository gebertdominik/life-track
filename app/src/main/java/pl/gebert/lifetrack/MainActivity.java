package pl.gebert.lifetrack;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {

    private SensorManager sensorManager;
    private Button buttonStart;
    private Button buttonStop;
    private Button buttonSave;
    private Button buttonReset;

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
                break;
            case R.id.buttonStop:
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
                buttonSave.setEnabled(true);
                buttonReset.setEnabled(true);
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
}
