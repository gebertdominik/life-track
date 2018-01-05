package pl.gebert.lifetrack.data;

/**
 * Created by Dominik Gebert on 2015-11-15.
 */
public class SensorData {
    float x;
    float y;
    float z;
    float step;
    final long time = System.currentTimeMillis();

    public SensorData(float x, float y, float z, float step){
        this.x = x;
        this.y = y;
        this.z = z;
        this.step = step;
    }

    public String toString() {
        return time + ", " + x + ", " + y + ", " + z + "\n";
    }
}
