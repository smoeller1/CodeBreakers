package edu.umkc.mongorestapp;

import java.io.IOException;

import com.ibm.json.java.JSONObject;

public class GetDirections {
	private String startLoc;
	private String endLoc;
	private String waypoint;
	
	
	
	public GetDirections(String startLoc, String endLoc) {
		super();
		this.startLoc = startLoc;
		this.endLoc = endLoc;
	}
	
	public String getStartLoc() {
		return startLoc;
	}
	public void setStartLoc(String startLoc) {
		this.startLoc = startLoc;
	}
	public String getEndLoc() {
		return endLoc;
	}
	public void setEndLoc(String endLoc) {
		this.endLoc = endLoc;
	}
	public String getWaypoint() {
		return waypoint;
	}
	public void setWaypoint(String waypoint) {
		this.waypoint = waypoint;
	}
	
	public JSONObject getDirections(JSONObject fullRoute) throws IOException {
		//Query Google first
		GetGoogleDirections googleRoute = new GetGoogleDirections(startLoc, endLoc);
		if (waypoint != null) {
			googleRoute.setWaypoint(waypoint);
		}
		JSONObject googleDirections = googleRoute.getDirections(fullRoute);
		System.out.println("GetDirections: getDirections: fullRoute size: "+fullRoute.size());
		
		//TODO once we add more APIs, we will have to compare to find the best route
		
		return googleDirections;
	}

}
