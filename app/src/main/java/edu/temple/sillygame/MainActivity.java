package edu.temple.sillygame;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements GameView.Rotatable {

    GameView gameView;

    SensorManager sensorManager;
    Sensor mag, acc;
    SensorEventListener sensorEventListener;

    float[] magValues, accValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        magValues = new float[3];
        accValues = new float[3];

        gameView = findViewById(R.id.gameView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {

                if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(sensorEvent.values, 0, magValues, 0, magValues.length);

                } else if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(sensorEvent.values, 0, accValues, 0, accValues.length);
                }

                gameView.invalidate();
            }


            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        sensorManager.registerListener(sensorEventListener, mag, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorEventListener, acc, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public float[] getRotationInfo() {

        float[] r = new float[9], values = new float[3];


        SensorManager.getRotationMatrix(r, null, accValues, magValues);
        SensorManager.getOrientation(r, values);

        return values;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(sensorEventListener);
    }

}
