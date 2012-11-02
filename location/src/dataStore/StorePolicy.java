package dataStore;

import java.util.ArrayList;
import java.util.List;

import my.location.locationPolicy.LocationPolicy;

public class StorePolicy {
	private int id;
	private List<DefinedEvent> events;
	
	public StorePolicy() {
		events = new ArrayList<DefinedEvent>();
	}
	
	public StorePolicy(int id){		
		this.id = id;
		events = new ArrayList<DefinedEvent>();
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public List<DefinedEvent> getEvents() {
		return events;
	}
	public void setEvents(List<DefinedEvent> events) {
		this.events = events;
	}
	
	public void addEvent(DefinedEvent event){
		this.events.add(event);
	}
}