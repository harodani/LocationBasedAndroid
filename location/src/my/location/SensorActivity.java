package my.location;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


import com.thoughtworks.xstream.XStream;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SensorActivity extends Activity implements SensorEventListener {
	
	
	private TextView textView;
	private List<Double> testing;
	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    double ax,ay,az;
	
    /*
    public SensorActivity() {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
    */
    public void onCreate(Bundle savedInstanceState){	
    	super.onCreate(savedInstanceState);
    	textView = new TextView(this);
    	testing = new ArrayList<Double>();
    	mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    	mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	if(!mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME)){
    		textView.setText("Error, could not register sensor listener");
    	}
    	setContentView(textView);
   
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        XStream xstr = new XStream();
		try {
			FileOutputStream fout = openFileOutput("accelerometer.xml", MODE_WORLD_READABLE);
			OutputStreamWriter osw = new OutputStreamWriter(fout);
			xstr.toXML(testing, osw);
			osw.flush();
			osw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    }


	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
	    	ax=event.values[0];
	        ay=event.values[1];
	        az=event.values[2];
        }
		String message = "X-axis: " + ax + " Y-axis: " + ay + " Z-axis: " + az;
		textView.setText(message); 
		double acc = Math.sqrt(ax*ax + ay*ay + az*az) - 9.80665;
		testing.add(Double.valueOf(acc));
		textView.setText("Acceleration: " + acc); 
	}

	public void onClick(View v) {		
	}

}
