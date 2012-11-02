package my.location;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import my.location.locationPolicy.DefinedAction;
import my.location.locationPolicy.DefinedLocation;
import my.location.locationPolicy.LocationPolicy;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.thoughtworks.xstream.XStream;

import dataStore.AudioData;
import dataStore.CompassData;
import dataStore.DefinedEvent;
import dataStore.RunningData;
import dataStore.SimpleLocation;
import dataStore.StorePolicy;


public class LocationService extends Service {
	private static final String TAG = "MyService";
	private LocationManager locationManager;
	private List<LocationPolicy> listofLP;
	private List<StorePolicy> currentDataList;
	private LocationPolicy locationPolicy;
	private List<SimpleLocation> locationList;
	private DefinedLocation refLocation;
	private float nearestLocRadius;
	private static final String FILE_NAME = "configure.xml";
	private static final String DATA_FILE_NAME = "data.xml";
	private static final String GPS_DATA_FILE_NAME = "data_gps.xml";
	private AudioRecorder audioRecorder;
	private SensorManager sm;
	private Sensor mSensor; // magnetic sensor
 	private Sensor aSensor; // accelerometer
    private float[] magneticFieldValues = new float[3];
    private float[] accelerometerValues = new float[3];
    private float[] valuesOfRotationMatrix = new float[9];
    private float[] valuesOfAzimuth = new float[3];
    private float[] magneticFieldValuesForAlpha = new float[3];
    private float[] accelerometerValuesForAlpha = new float[3];
    private float[] valuesOfRotationMatrixForAlpha = new float[9];
    private float[] valuesOfAzimuthForAlpha = new float[3];
    private static String orientation;
    private float bearing;
    private Location nearestLocation;
    private float nearestDistance;
    private float[] orientationDegrees = new float[6];
    private static int sensoringTimes = 0;
    private float orientationTrueDegree;
    private float orientationDegreeToBeStored; 
    private GeomagneticField geoField;
    private float AlphaAngle;
    private static boolean outOfRangeForAtLeastOneSpot = false;
    private Location estimatedNextLocation;
    private float velocity;
    private Location currentLocation;
    private Location currentLocationToGetSpeed;
    private Location estimatedNextNearestLocation;
    private Location interestOfLocation;
    private boolean IsTimerOn = false;
    private boolean isFirstTimeRunningGPSDetection = true;
    private static final int initialInterval =  60000;
	public IBinder onBind(Intent intent) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		Toast.makeText(this, "My Location Service Created", Toast.LENGTH_LONG)
				.show();
		Log.i(TAG, "onCreate");
		super.onCreate();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, initialInterval, 0, locationListener);
		nearestLocation = new Location(LocationManager.GPS_PROVIDER);
		estimatedNextLocation = new Location(LocationManager.GPS_PROVIDER);
		currentLocation = new Location(LocationManager.GPS_PROVIDER);
		estimatedNextNearestLocation = new Location(LocationManager.GPS_PROVIDER);
		interestOfLocation = new Location(LocationManager.GPS_PROVIDER);
		currentLocationToGetSpeed = new Location(LocationManager.GPS_PROVIDER);
		listofLP = readXML(FILE_NAME); 
		currentDataList = readDataXML(DATA_FILE_NAME);
		locationList = readGPSDataXML(GPS_DATA_FILE_NAME);
		for (LocationPolicy policy : listofLP) {
			Toast.makeText(LocationService.this, "Policy " + policy.getPolicyId(), Toast.LENGTH_SHORT).show();			
		}
		// initialize sensor of magnetic one and accelerometer one 
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		aSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}
	
	// This function should be called by the OrientationDetectionThread.
	public float OrientationDetectionOnRegularFrequency(Location estimatedCurrentLocation) 
	{
		//1. get the nearest Location somehow, be careful about if we are in any of range or not
		//2. Calculate the bearing based on : theEstimatedLocation and theNearestLocation
			//bearing = location.bearingTo(nearestLocation);
		//3. RigsterListenerJustForobtainingAlpha and also set the geoField.
			//sm.registerListener(sensorListenerJustForObtainingAlpha, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
			//sm.registerListener(sensorListenerJustForObtainingAlpha, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
		//4. Enter the onSensorChanged part of sensorListenerJustForObtainingAlpha, trying to get the Estimate Alpha.
		Log.i(TAG,"get the alpha");
		return AlphaAngle;
	}
	
	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			Log.i(TAG, "Get New Location");
						
			double lat = location.getLatitude();;
			double lon = location.getLongitude();
			float bear = location.getBearing();
			float accuracy = location.getAccuracy();
			float speed = location.getSpeed();
			long time = location.getTime();				
			SimpleLocation locationData = new SimpleLocation(lat, lon, bear, accuracy, speed, time);				
			locationList.add(locationData);			
			updateGPSDataXML(locationList);	
			/*
			if (isFirstTimeRunningGPSDetection)
			{
				isFirstTimeRunningGPSDetection = false;
				
				currentLocationToGetSpeed = location;
			}
			else 
			{
				 float[] cal_result = new float[3];
				 Location.distanceBetween(currentLocationToGetSpeed.getLatitude(), currentLocationToGetSpeed.getLongitude(), location.getLatitude(), location.getLongitude(),cal_result);
				 currentLocationToGetSpeed = location;
				// velocity = cal_result[0]/ initialInterval * 1000;
				 //Toast.makeText(LocationService.this, String.valueOf(velocity) + " m/s calculated in the difference between gps detect", Toast.LENGTH_SHORT).show();
			}
			*/
			//Get the currentLocation
			currentLocation = new Location(location);
			
			// set to big enough to calculate the nearest distance
			nearestDistance = 1000000;
			//Get the nearestLocation to the current point,
			//and chk if needs to do any action if in one of the circle if it has one
			nearestLocation = checkWithinRangeAndPerform(location.getLatitude(), location.getLongitude(),false); 
			
			//Get the velocity
			getVelocity(location);
			
			Toast.makeText(LocationService.this, "La: "+location.getLatitude()+"Lo: "+location.getLongitude() +
					"\n nearestLocationLa: " + nearestLocation.getLatitude()+
					"\n nearestLocationLo: " + nearestLocation.getLongitude() +
					"\n Distance to that nearest one is " + nearestDistance + 
					"\n the speed is " + String.valueOf(velocity) +" km/h calculated in getSpeed()" , Toast.LENGTH_LONG).show();
			
			Log.i(TAG,"speed is " + String.valueOf(velocity));
			
			//Turn on the listener for Orientation detection
			RegisterSensorsForObtainingAlpha();

			Toast.makeText(LocationService.this, "La: "+location.getLatitude()+"Lo: "+location.getLongitude() +
						"\n nearestLocationLa: " + nearestLocation.getLatitude()+
						"\n nearestLocationLo: " + nearestLocation.getLongitude() +
						"\n Distance to that nearest one is " + nearestDistance, Toast.LENGTH_LONG).show();
		}
		public void onProviderDisabled(String provider) {
			Log.i(TAG, "Provider now is disabled..");
		}
		public void onProviderEnabled(String provider) {
			Log.i(TAG, "Provider now is enabled..");
		}
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};
	
	public void RegisterSensorsForObtainingAlpha() {
		Log.i(TAG,"SensorForAlphaRegistered!");
		sm.registerListener(sensorListenerJustForObtainingAlpha, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sm.registerListener(sensorListenerJustForObtainingAlpha, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	private void getVelocity(Location location) {
		velocity = location.getSpeed();		
		velocity = velocity * 3.6f ; // km/h
		if(velocity < 0.1){
			velocity = 0.1f;
		}
	}
	private Location checkWithinRangeAndPerform(double latitude, double longitude, boolean PreCheckForNextTime) {
			
			for (int i = 0; i < listofLP.size(); i++) {
				locationPolicy = listofLP.get(i);
				refLocation = locationPolicy.getDefinedLocation();
				float[] cal_result = new float[3];
				Location.distanceBetween(latitude, longitude, refLocation.getLatitude(), refLocation.getLongitude(), cal_result);
				String distanceMsg;
				if (PreCheckForNextTime == false)
				{
					 distanceMsg = "The current location is " + Float.toString(cal_result[0]) + 
							 " meters away from " + refLocation.getLocationName();
				}
				else {
					 distanceMsg = "The expected next location is " + Float.toString(cal_result[0]) + 
							 " meters away from " + refLocation.getLocationName();
				}
				Location.distanceBetween(latitude, longitude, refLocation.getLatitude(), refLocation.getLongitude(),cal_result);				
				Log.i(TAG, distanceMsg);
				Toast.makeText(LocationService.this, distanceMsg,
						Toast.LENGTH_LONG).show();
				if (PreCheckForNextTime == false && cal_result[0] < refLocation.getRadius()) // if right now we are within the range
				{
						Log.i(TAG, "Gonna perform action soon...");
						performAction(locationPolicy.getActions(), locationPolicy.getPolicyId());// perform actions
				} 
				if (nearestDistance >= cal_result[0]) // current spot to the location distance is shorter than the previous ones
				{
					nearestLocRadius = refLocation.getRadius();
					nearestDistance = cal_result[0];
					interestOfLocation.setLatitude(refLocation.getLatitude());
					interestOfLocation.setLongitude(refLocation.getLongitude());
				}
				
			}
			return interestOfLocation;
		}
	private final SensorEventListener sensorListenerJustForObtainingAlpha = new SensorEventListener() {
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			{
				//Toast.makeText(LocationService.this, "SensorChangedForAccelerometer (Just for obtaining alpha)\n", Toast.LENGTH_SHORT).show();
				accelerometerValuesForAlpha = event.values;
			}
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			{
				magneticFieldValuesForAlpha = event.values;
				//Toast.makeText(LocationService.this, "SensorChangedForMagneticField (Just for obtaining alpha)\n", Toast.LENGTH_SHORT).show();
			}
			if (sensoringTimes >= 6)
			{
				sensoringTimes = 0;
			}
			//Toast.makeText(LocationService.this, "thesensoringTime is " + sensoringTimes, Toast.LENGTH_SHORT).show();
			orientationDegrees[sensoringTimes] = CalculateOrientation(valuesOfAzimuthForAlpha,valuesOfRotationMatrixForAlpha,accelerometerValuesForAlpha,magneticFieldValuesForAlpha);
			//Toast.makeText(LocationService.this, "now get the orientation sample is " + String.valueOf(orientationDegrees[sensoringTimes])+ " Sampling time: "+sensoringTimes, Toast.LENGTH_SHORT).show();
			sensoringTimes++;
			if (sensoringTimes == 6)
			{
				sm.unregisterListener(sensorListenerJustForObtainingAlpha);
				orientationTrueDegree = 0;
				for (int i = 0; i < 6; i++ )
				{
					orientationTrueDegree += orientationDegrees[i];
				}
				orientationTrueDegree = orientationTrueDegree/6;
				//Toast.makeText(LocationService.this, "orientationTrueDegree avaragely is " + orientationTrueDegree, Toast.LENGTH_SHORT).show();
				geoField = new GeomagneticField(
				         Double.valueOf(currentLocation.getLatitude()).floatValue(),
				         Double.valueOf(currentLocation.getLongitude()).floatValue(),
				         Double.valueOf(currentLocation.getAltitude()).floatValue(),
				         System.currentTimeMillis()
				      );
				orientationTrueDegree += geoField.getDeclination();
				//Toast.makeText(LocationService.this, "the final orientationTrueDegree after fix is " + orientationTrueDegree, Toast.LENGTH_SHORT).show();
				
				bearing = currentLocation.bearingTo(nearestLocation);
				AlphaAngle = GPSUpdateManager.getAlphaAngle(orientationTrueDegree, bearing);
				//Toast.makeText(LocationService.this, "the final seeking alpha is " + AlphaAngle, Toast.LENGTH_SHORT).show();
				
				/* UpdateGPS interval here */
				// TO BE DONE
				
				long updateInt = GPSUpdateManager.getNewGPSUpdateInterval(currentLocation, nearestLocation, nearestLocRadius, AlphaAngle, velocity);
				if (updateInt > 0l) {
					Toast.makeText(LocationService.this,"The interval time is " + updateInt, Toast.LENGTH_SHORT).show();
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateInt, 0, locationListener);
				}
				else {
					Toast.makeText(LocationService.this,"Going away. Non interval update ", Toast.LENGTH_SHORT).show();
				}
				
				/* Prediction Work Starts! */
				// Get the estimated Next Location and the nearest destination to that location 
				estimatedNextLocation = estimateNextLocation(orientationTrueDegree, velocity, currentLocation, (double)1/60);
				
				float[] cal_result1 = new float[3];
				 Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), estimatedNextLocation.getLatitude(), estimatedNextLocation.getLongitude(),cal_result1);
				 Toast.makeText(LocationService.this, String.valueOf(cal_result1[0]) + " m  between current and estimate ", Toast.LENGTH_SHORT).show();
				
				estimatedNextLocation.setSpeed(currentLocation.getSpeed());

				Toast.makeText(LocationService.this, "estimate next location: " + estimatedNextLocation.getLatitude() + " , " + estimatedNextLocation.getLongitude() +
						" \n current Location is "+ currentLocation.getLatitude() +" , "+ currentLocation.getLongitude(), 4000).show();
			
				currentLocation = new Location(estimatedNextLocation);
																
				estimatedNextNearestLocation = checkWithinRangeAndPerform(estimatedNextLocation.getLatitude(), 
						estimatedNextLocation.getLongitude(), true);
				if (estimatedNextNearestLocation != nearestLocation)// the closest destination will likely CHANGE to new one
				{
					nearestLocation = new Location(estimatedNextNearestLocation);
				}
				if (!IsTimerOn)
				{
					//Starting new thread for timer to get estimate angles (They don't have to be in Oncreate function)
					OrientationDetectionThread orientationThread = new OrientationDetectionThread(LocationService.this,60000);
					orientationThread.start();
					Toast.makeText(LocationService.this, "New thread started", Toast.LENGTH_SHORT);
					Log.i(TAG, "New thread started");
					IsTimerOn = true;
				}
			}
			
		}
};
	private final SensorEventListener sensorListener= new SensorEventListener() {
		
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			{
				accelerometerValues = event.values;
				/*	Toast.makeText(LocationService.this, "SensorChangedForAccelerometer\n", Toast.LENGTH_SHORT).show();
				float accelerometerValueOfXandY = (float) Math.sqrt(accelerometerValues[0]*accelerometerValues[0]
						+accelerometerValues[1]*accelerometerValues[1]);
				Toast.makeText(LocationService.this,
						"   acc[0] = " + String.valueOf(accelerometerValues[0]) +
						"\n acc[1] = " + String.valueOf(accelerometerValues[1]) +
						"\n acc[2] = " + String.valueOf(accelerometerValues[2]) +
						"\n Moving a is : " + String.valueOf(accelerometerValueOfXandY), Toast.LENGTH_SHORT).show();
			*/
			}
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			{
				magneticFieldValues = event.values;
				//Toast.makeText(LocationService.this, "SensorChangedForMagneticField\n", Toast.LENGTH_SHORT).show();
			}
			orientationDegreeToBeStored = CalculateOrientation(valuesOfAzimuth,valuesOfRotationMatrix,accelerometerValues,magneticFieldValues);
			
			sm.unregisterListener(sensorListener);
		}
		
		
};
	private float CalculateOrientation(float[] valuesOfAzimuth, float[] valuesOfRotationMatrix, float[] accelerometerValues, float[] magneticFieldValues) 
	{
	SensorManager.getRotationMatrix(valuesOfRotationMatrix, null, accelerometerValues, magneticFieldValues);
	SensorManager.getOrientation(valuesOfRotationMatrix, valuesOfAzimuth);
	valuesOfAzimuth[0] = (float)Math.toDegrees(valuesOfAzimuth[0]);
	Log.i("ORIENTATION", String.valueOf(valuesOfAzimuth[0]));
	
	if ( -10 <= valuesOfAzimuth[0] && valuesOfAzimuth[0] < 10 && orientation !="North")
	{
		orientation = "North";
		Log.i("ORIENTATION", "NORTH");
		//Toast.makeText(this, "North", Toast.LENGTH_SHORT).show();
	}
	else if (10 <= valuesOfAzimuth[0] && valuesOfAzimuth[0] < 80 &&  orientation !="NorthEast")
	{
		orientation ="NorthEast";
		//Toast.makeText(this, "NorthEast", Toast.LENGTH_SHORT).show();
	}
	else if ( 80 <= valuesOfAzimuth[0] && valuesOfAzimuth[0] < 100 && orientation !="East")
	{
		orientation ="East";
		Log.i("ORIENTATION", "EAST");
		//Toast.makeText(this, "EAST", Toast.LENGTH_SHORT).show();
	}
	else if (100 <= valuesOfAzimuth[0] && valuesOfAzimuth[0] < 170 && orientation !="EastSouth")
	{
		orientation = "EastSouth";
		//Toast.makeText(this, "EastSouth", Toast.LENGTH_SHORT).show();
	}
	else if ( (170 <= valuesOfAzimuth[0] && valuesOfAzimuth[0] <= 180) || ( -180 <= valuesOfAzimuth[0] && valuesOfAzimuth[0] <= -170)  )
	{
		if (orientation !="South")
		{
			orientation ="South";
			Log.i("ORIENTATION", "SOUTH");
			//Toast.makeText(this, "SOUTH", Toast.LENGTH_SHORT).show();
		}
		
	}
	else if (-170 <= valuesOfAzimuth[0] && valuesOfAzimuth[0] < -100 && orientation !="SouthWest")
	{
		orientation ="SouthWest";
		//Toast.makeText(this, "SouthWest", Toast.LENGTH_SHORT).show();
	}
	else if (-100 <= valuesOfAzimuth[0] && valuesOfAzimuth[0] < -80 && orientation !="West" )
	{
		orientation ="West";
		Log.i("ORIENTATION", "WEST");
		//Toast.makeText(this, "WEST", Toast.LENGTH_SHORT).show();
	}
	else if (-80 <= valuesOfAzimuth[0] && valuesOfAzimuth[0] < -10&& orientation !="NorthWest")
	{
		orientation ="NorthWest";
		//Toast.makeText(this, "NorthWest", Toast.LENGTH_SHORT).show();
	}
	return valuesOfAzimuth[0];
	//Toast.makeText(this, "Perform Action 3 and also unregisterListener: "+ String.valueOf(valuesOfAzimuth[0]), Toast.LENGTH_SHORT).show();
}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		Toast.makeText(this, "My Location Service Destroy", Toast.LENGTH_LONG).show();
		Log.i(TAG, "onDestroy");
		locationManager.removeUpdates(locationListener);
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Toast.makeText(this, "My Location Service Started", Toast.LENGTH_LONG)
				.show();
		Log.i(TAG, "onStart");
	}

	private void performAction(List<DefinedAction> list, int policyId){
		// AlertDialog.Builder(LocationActivity.this).setMessage(R.string.welcome).show();
		Iterator it = list.iterator();
		while (it.hasNext()) {
			DefinedAction action = (DefinedAction) it.next();
			ExecuteAction(action.getActionId(), policyId); // execute actions based on the ActionID
		}
	}

	private void ExecuteAction(int actionId, int policyId) {
		// initialize the time stamp
		final String DATE_FORMAT_NOW = "yyyyMMddHHmmss";
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		Date date = cal.getTime();
		String dateString = sdf.format(date);
		String basicFileName = "policy" + policyId + dateString + "action" + actionId;
		StorePolicy newData = new StorePolicy(policyId);
		switch (actionId) {
		case 0:
			Log.i(TAG, "Performing action Download XML");
			Toast.makeText(this, "Performing action Download XML", Toast.LENGTH_LONG).show();
			String NEW_FILE_NAME = "configure_NEW.xml";
			String NEW_FILE_LOCATION = "/data/data/my.location/files/" + NEW_FILE_NAME;
			XMLService.downloadXML("configure.xml", NEW_FILE_LOCATION);			
			List<LocationPolicy> NewList = readXML(NEW_FILE_LOCATION);			
			listofLP = updatePolicyList(listofLP, NewList);
			mergeXML(NEW_FILE_NAME);
			break;
		case 1:
			Log.i(TAG, "Performing action Recording Audio");
			Toast.makeText(this, "Performing action Recording Audio", Toast.LENGTH_LONG).show();
			// External Storage-- SD Card, create the path of the audio file
			String audioFileName = sanitizePath(basicFileName, 1);
			// If we want to use Internal storage, use the following path
			// File newAudioDirectory = getDir(audioFileDirectory, MODE_PRIVATE);
			// String audioFileName = newAudioDirectory.getAbsolutePath() + "/"
			// + audioFileDirectory + ".3gp";
			audioRecorder = new AudioRecorder(audioFileName);
			// Start thread for recording, it will record audio for 10 seconds
			RecordThread audioRecordThread = new RecordThread(audioRecorder,10000);
			audioRecordThread.start();						
			DefinedEvent audioEvent = new DefinedEvent(date, audioFileName);
			newData.addEvent(audioEvent);
			currentDataList = updateDataList(newData, currentDataList);
			updateDataXML(currentDataList);
			break;
		case 2:
			Log.i(TAG, "Performing action Detect the orientation from the compass");
			Toast.makeText(this, "Performing action Detect the orientation from the compass", Toast.LENGTH_SHORT).show();
			sm.registerListener(sensorListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
			sm.registerListener(sensorListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);			
			DefinedEvent compassEvent = new DefinedEvent(date, orientationDegreeToBeStored);
			newData.addEvent(compassEvent);
			currentDataList = updateDataList(newData, currentDataList);
			updateDataXML(currentDataList);
			break;
		case 3:
			Log.i(TAG, "Performing action Running Data collection");
			Time runningData = new Time();
			runningData.setToNow();
			DefinedEvent runningEvent = new DefinedEvent(date, runningData);
			newData.addEvent(runningEvent);
			currentDataList = updateDataList(newData, currentDataList);
			updateDataXML(currentDataList);
			break;
		default:
			Log.i(TAG, "Action code wrong, no action taken");
			Toast.makeText(this, "Action code wrong, no action taken", Toast.LENGTH_LONG).show();
			break;
		}
	}


	private Location estimateNextLocation(Float orientation, Float speed, Location location, Double time){
		Location newLocation = new Location(location);		
		
		double R = 6378.1; //Earth radius in kilometers
		double d = speed * time; //Distance in kilometers
		double brng = orientation * Math.PI / 180.0; //orientation in radians
		double lat1 = location.getLatitude() * Math.PI / 180.0;
		double lon1 = location.getLongitude() * Math.PI / 180.0;
		
		double lat2 = Math.asin(Math.sin(lat1)*Math.cos(d/R) + Math.cos(lat1)*Math.sin(d/R)*Math.cos(brng)) * 180 / Math.PI;
		double lon2 = (lon1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(lat1), 
										Math.cos(d/R)-Math.sin(lat1)*Math.sin(lat2))) * 180 / Math.PI;		

		newLocation.setLatitude(lat2);
		newLocation.setLongitude(lon2);				
		return newLocation;

	}
	
	
	
	private String sanitizePath(String path, int type) {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		if (!path.contains(".")) {
			if(type == 1){//Type for audio files
				path += ".3gp";
			} else if(type == 2){//Type for video files
				path += ".mp4";
			}
		}
		return Environment.getExternalStorageDirectory().getAbsolutePath() + path;
	}

	protected List<LocationPolicy> readXML(String filename) {
		String eol = System.getProperty("line.separator");
		BufferedReader input = null;
		List<LocationPolicy> policies = new ArrayList<LocationPolicy>();
		try {
			input = new BufferedReader(new InputStreamReader(openFileInput(filename)));
			String line;
			StringBuffer buffer = new StringBuffer();
			while ((line = input.readLine()) != null) {
				buffer.append(line + eol);
			}
			String xmlString = buffer.toString();
			XStream xstr = new XStream();
			xstr.alias("location", DefinedLocation.class);
			xstr.alias("action", DefinedAction.class);
			xstr.alias("policy", LocationPolicy.class);
			policies = (List<LocationPolicy>) xstr.fromXML(xmlString);
		} catch (FileNotFoundException e) {
			updateXML(policies);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return policies;
	}

	protected List<LocationPolicy> updatePolicyList(List<LocationPolicy> currentList, List<LocationPolicy> newList) {
		List<LocationPolicy> updatedList = new ArrayList<LocationPolicy>();
		for (LocationPolicy currentLocation : currentList) {
			boolean found = false;
			for (LocationPolicy newLocation : newList) {
				if (newLocation.getPolicyId() == currentLocation.getPolicyId()) {
					updatedList.add(newLocation);
					found = true;
					break;
				}
			}
			if (!found) {
				updatedList.add(currentLocation);
			}
		}
		for (LocationPolicy newLocation: newList) {
			boolean found = false;
			for (LocationPolicy currentLocation: currentList) {
				if (newLocation.getPolicyId() == currentLocation.getPolicyId()) {
					found = true;
					break;
				}
			}
			if (!found) {
				updatedList.add(newLocation);
			}
		}
		return updatedList;
	}

	protected void updateXML(List<LocationPolicy> currentList) {
		XStream xstr = new XStream();
		xstr.alias("location", DefinedLocation.class);
		xstr.alias("action", DefinedAction.class);
		xstr.alias("policy", LocationPolicy.class);
		try {
			FileOutputStream fout = openFileOutput(FILE_NAME, MODE_WORLD_READABLE);
			OutputStreamWriter osw = new OutputStreamWriter(fout);
			xstr.toXML(currentList, osw);
			osw.flush();
			osw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void mergeXML(String newXML) {
		List<LocationPolicy> currentList = readXML(FILE_NAME);
		List<LocationPolicy> newList = readXML(newXML);

		List<LocationPolicy> mergedList;
		// Merge two list
		mergedList = updatePolicyList(currentList, newList);

		updateXML(mergedList);

		deleteFile(newXML);
	}
	
	protected List<StorePolicy> readDataXML(String filename) {
		String eol = System.getProperty("line.separator");
		BufferedReader input = null;
		List <StorePolicy> dataList = new ArrayList<StorePolicy>();
		try {
			input = new BufferedReader(new InputStreamReader(openFileInput(filename)));
			String line;
			StringBuffer buffer = new StringBuffer();
			while ((line = input.readLine()) != null) {
				buffer.append(line + eol);
			}
			String xmlString = buffer.toString();
			XStream xstr = new XStream();
			xstr.alias("policy", StorePolicy.class);
			xstr.alias("event", DefinedEvent.class);
			xstr.alias("data", DefinedEvent.class);
			xstr.alias("audio", AudioData.class);
			xstr.alias("compass", CompassData.class);
			xstr.alias("running", RunningData.class);			
			dataList = (List<StorePolicy>) xstr.fromXML(xmlString);
		} catch (FileNotFoundException e) {
			updateDataXML(dataList);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return dataList;
	}
	
	public List <StorePolicy> updateDataList(StorePolicy newData, List <StorePolicy> dataList){
		int id = newData.getId();
		StorePolicy oldPolicy = null; 
		for (StorePolicy policy: dataList){
			if(policy.getId() == id){
				oldPolicy = policy;
				break;
			}
		}
		if(oldPolicy != null){
			dataList.remove(oldPolicy);
			List <DefinedEvent> events = oldPolicy.getEvents();
			events.addAll(newData.getEvents());
			oldPolicy.setEvents(events);
		} else { //oldPolicy == null
			oldPolicy = newData;
		}
		dataList.add(oldPolicy);
		return dataList;
	}
	
	public void updateDataXML(List <StorePolicy> newDataList){
		XStream xstr = new XStream();
		xstr.alias("policy", StorePolicy.class);
		xstr.alias("event", DefinedEvent.class);
		xstr.alias("data", DefinedEvent.class);
		xstr.alias("audio", AudioData.class);
		xstr.alias("compass", CompassData.class);
		xstr.alias("running", RunningData.class);
		try {
			FileOutputStream fout = openFileOutput(DATA_FILE_NAME, MODE_WORLD_READABLE);
			OutputStreamWriter osw = new OutputStreamWriter(fout);			
			xstr.toXML(newDataList, osw);
			osw.flush();
			osw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	protected List<SimpleLocation> readGPSDataXML(String filename) {
		String eol = System.getProperty("line.separator");
		BufferedReader input = null;
		List <SimpleLocation> dataList = new ArrayList<SimpleLocation>();
		try {
			input = new BufferedReader(new InputStreamReader(openFileInput(filename)));
			String line;
			StringBuffer buffer = new StringBuffer();
			while ((line = input.readLine()) != null) {
				buffer.append(line + eol);
			}
			String xmlString = buffer.toString();
			XStream xstr = new XStream();
			xstr.alias("gpsData", SimpleLocation.class);			
			dataList = (List<SimpleLocation>) xstr.fromXML(xmlString);
		} catch (FileNotFoundException e) {
			updateGPSDataXML(dataList);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return dataList;
	}
	
	public void updateGPSDataXML(List <SimpleLocation> dataList){		
		XStream xstr = new XStream();
		xstr.alias("gpsData", SimpleLocation.class);
		try {
			FileOutputStream fout = openFileOutput(GPS_DATA_FILE_NAME, MODE_WORLD_READABLE);
			OutputStreamWriter osw = new OutputStreamWriter(fout);
			xstr.toXML(dataList, osw);
			osw.flush();
			osw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}