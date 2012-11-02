package my.location;

import android.location.Location;

public class GPSUpdateManager {	

	public static float getAlphaAngle(float currentTrueOrientation, float bearing)
	{
		float absoluteAngleBetweenBearingAndOrientation = Math.abs(currentTrueOrientation - bearing);
		if (absoluteAngleBetweenBearingAndOrientation >= 180)
			absoluteAngleBetweenBearingAndOrientation = 360 - absoluteAngleBetweenBearingAndOrientation;
		else if (absoluteAngleBetweenBearingAndOrientation <= -180)
			absoluteAngleBetweenBearingAndOrientation = absoluteAngleBetweenBearingAndOrientation + 360;
		return absoluteAngleBetweenBearingAndOrientation;// it is between 0 - 180
	}

	public static long getNewGPSUpdateInterval(Location currentLocation,Location nearestLocation,float radius,double directionAngle, double speed)

	{
		long updateInterval = 0;
		double transformedAngle = directionAngle < 0 ? -directionAngle * Math.PI / 180.0d:directionAngle * Math.PI / 180.0d;
		double transformedSpeed = speed * 10.0d / 36.0d;
		double destinationArea = getDestinationAngleArea(currentLocation, nearestLocation, radius);
		float distance = currentLocation.distanceTo(nearestLocation);
		
		
		if (transformedAngle < destinationArea) {
			//This mean the subject is going directly to the area
			updateInterval = Math.round((distance/transformedSpeed)/3);
		}
		else {
			//Calculates the component of the speed where the subject is moving, reducing the speed.
			updateInterval = Math.round((distance/(transformedSpeed*Math.cos(transformedAngle)))/3);
		}
		
		return updateInterval*1000;
	}
	
	private static double getDestinationAngleArea(Location currentLocation, Location nearestLocation, float radius) {
		
		//hipotenuse = distance from currentLocation to nearestLocation
		float distance = currentLocation.distanceTo(nearestLocation);
		//leg1 = radius 
		//leg2 = distance from the origin (currentLocation) to the point that intersect with the circle
		//Radius is the adjacent side of the Hipotenuse so..
		double angle = Math.acos(radius/distance); 
		
		
		return angle;
	}

}