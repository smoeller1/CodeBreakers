package edu.umkc.mongorestapp;

import java.io.IOException;

import com.ibm.json.java.JSONObject;

public class GetBingDirections {
	private String startLoc;
	private String endLoc;
	private String waypoint;
	
	public GetBingDirections(String startLoc, String endLoc) {
		super();
		this.startLoc = startLoc;
		this.endLoc = endLoc;
	}

	public GetBingDirections(String startLoc, String endLoc, String waypoint) {
		super();
		this.startLoc = startLoc;
		this.endLoc = endLoc;
		this.waypoint = waypoint;
	}

	public void setWaypoint(String waypoint) {
		this.waypoint = waypoint;
	}
	
	public JSONObject getDirections() throws IOException{
		String url = "http://dev.virtualearth.net/REST/v1/Routes/travelMode?";
		JSONObject route = new JSONObject();
		return route;
	}
}
