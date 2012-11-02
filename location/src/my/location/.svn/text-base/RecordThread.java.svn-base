package my.location;

import java.io.IOException;

import android.util.Log;

public class RecordThread extends Thread {

	private AudioRecorder audioRecorder;
	private int timeOfRecordingAudio;
	
	
	public RecordThread(AudioRecorder audioRecorder,int time) {
		super();
		this.audioRecorder = audioRecorder;
		timeOfRecordingAudio=time;
	}


	@Override
	public void run() {
		Log.i("Thread","Start Recording");
		try {
			audioRecorder.start();
			//record an audio for timeOfRecordingAudio seconds 
			sleep(timeOfRecordingAudio);
			audioRecorder.stop();
		} catch (IOException e) {
			e.printStackTrace();
			Log.i("AudioRecorder Exception", e.toString());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		super.run();
	}
}