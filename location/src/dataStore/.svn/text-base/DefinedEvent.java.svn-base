package dataStore;

import android.text.format.Time;
import java.util.Date;

public class DefinedEvent {
	private Date id;
	private Data data;

	public DefinedEvent() {	
	}
	
	public DefinedEvent(Date id, String fileName){		
		this.id = id;
		this.data = new AudioData(fileName);		
	}
	
	public DefinedEvent(Date id, Float direction){
		this.id = id;
		this.data = new CompassData(direction);
	}
	
	public DefinedEvent(Date id, Time time){
		this.id = id;
		this.data = new RunningData(time);		
	}	
	
	public Date getId() {
		return id;
	}

	public void setId(Date id) {
		this.id = id;
	}

	public Data getData() {
		return this.data;
	}

	public void setData(Data data) {
		this.data = data;
	} 	
}