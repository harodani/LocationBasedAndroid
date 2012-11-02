package dataStore;

import android.location.Location;

public class SimpleLocation {
	private double latitude;
	private double longitude;
	private float bearing;
	private float accuracy;
	private float speed;
	private long time;
	
	public SimpleLocation(){		
	}
	
	public SimpleLocation(double latitude, double longitude, float bearing, float accuracy, float speed, long time) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.bearing = bearing;
		this.accuracy = accuracy;
		this.speed = speed;
		this.time = time;
	}
	
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public float getBearing() {
		return bearing;
	}
	public void setBearing(float bearing) {
		this.bearing = bearing;
	}
	public float getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}
	public float getSpeed() {
		return speed;
	}
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	
}
