package my.location;

import android.app.Service;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class OrientationDetectionThread extends Thread {
	
	private Timer timer;
	private int delay;
	private static final String TAG = "OriDetThread";
	private LocationService myLocationService;
	Handler handler = new Handler()
	{
		public void handleMessage(Message msg) 
		{
			 Log.i(TAG, String.valueOf(msg.what)); 
			 myLocationService.RegisterSensorsForObtainingAlpha();
			 if (msg.what == 0) 
			 {
				 timer.cancel();
			 }
		}
	};
	public OrientationDetectionThread(LocationService service, int time) {
	
		delay = time;
		myLocationService = service;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		Log.i(TAG, "New thread run function");
		// TODO Auto-generated method stub
		timer = new Timer();
		timer.schedule(new TimerTask() 
		{
			int j = 10;
			@Override
			public void run() 
			{
				// TODO Auto-generated method stub
				
				Message msg = new Message();
				msg.what = j;
				handler.handleMessage(msg);
				j--;
			}
		}
		, delay,60000);//60 seconds between each task and wait for delay time for the first execution
		super.run();
	}
	
	
	
}
