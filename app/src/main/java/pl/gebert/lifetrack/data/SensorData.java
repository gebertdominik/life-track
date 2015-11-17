package pl.gebert.lifetrack.data;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Dominik Gebert on 2015-11-15.
 */
public class SensorData {
    float x;
    float y;
    float z;
    final Date date = new Date();

    public SensorData(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String toString() {
        return getCurrentTimeStamp() + ", " + x + ", " + y + ", " + z;
    }

    private String getCurrentTimeStamp() {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(date);
    }
}
