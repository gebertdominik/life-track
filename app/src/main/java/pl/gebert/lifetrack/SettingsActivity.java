package pl.gebert.lifetrack;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import pl.gebert.lifetrack.config.UserParam;

public class SettingsActivity extends AppCompatActivity implements OnClickListener {
    EditText weightText;
    EditText heightText;
    UserParam weightTextParam;
    UserParam heightTextParam;
    Button buttonSaveSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        buttonSaveSettings = (Button) findViewById(R.id.buttonSaveSettings);
        buttonSaveSettings.setOnClickListener(this);

        DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());

        weightTextParam = db.findUserParamByCode(DatabaseHelper.WEIGHT_USER_PARAM_CODE);
        weightText = (EditText) findViewById(R.id.weightValue);
        weightText.setText(weightTextParam.getValue());

        heightTextParam = db.findUserParamByCode(DatabaseHelper.HEIGHT_USER_PARAM_CODE);
        heightText = (EditText) findViewById(R.id.heightValue);
        heightText.setText(heightTextParam.getValue());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSaveSettings:
                weightTextParam.setValue(weightText.getText().toString());
                heightTextParam.setValue(heightText.getText().toString());
                DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
                db.updateUserParameter(weightTextParam);
                db.updateUserParameter(heightTextParam);
                break;
            default:
                break;
        }
    }
}
