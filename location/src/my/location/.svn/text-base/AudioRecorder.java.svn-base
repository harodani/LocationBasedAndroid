package my.location;

import java.io.File;
import java.io.IOException;

import android.media.MediaRecorder;
import android.util.Log;

public class AudioRecorder {

	MediaRecorder recorder = new MediaRecorder();
	private String path;
	/**
	 * Creates a new audio recording at the given path (relative to root of SD
	 * card).
	 */
	public AudioRecorder(String path) {
		this.path = path;
	}

	/**
	 * Starts a new recording.
	 */
	public void start() throws IOException {
		String state = android.os.Environment.getExternalStorageState();
	    if(!state.equals(android.os.Environment.MEDIA_MOUNTED))  {
	    	throw new IOException("SD Card is not mounted.  It is " + state + ".");
	    }	
	    // make sure the directory we plan to store the recording in exists
		File directory = new File(path).getParentFile();
		if (!directory.exists() && !directory.mkdirs()) {
			throw new IOException("Path to file could not be created.");
		}		
		Log.i("AudioRecorder", "Start Audio Recording!");
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setAudioSamplingRate(8000);
		recorder.setOutputFile(path);
		try{
			recorder.prepare();
			recorder.start();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Stops a recording that has been previously started.
	 */
	public void stop() throws IOException {
		Log.i("AudioRecorder", "Stop Audio Recording!");
		try{
			recorder.stop();			
		} catch(Exception e){
			e.printStackTrace();
		}
		recorder.release();
		recorder = null;
	}

	public void setPath(String path) {
		this.path = path;
	}
}