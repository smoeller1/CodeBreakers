package edu.umkc.mongorestapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import com.ibm.json.java.JSON;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

import jdk.nashorn.internal.parser.JSONParser;

public class GetGoogleDirections {
	private String startLoc;
	private String endLoc;
	private String waypoint;
	
	public GetGoogleDirections(String startLoc, String endLoc) {
		super();
		this.startLoc = startLoc;
		this.endLoc = endLoc;
	}

	public GetGoogleDirections(String startLoc, String endLoc, String waypoint) {
		super();
		this.startLoc = startLoc;
		this.endLoc = endLoc;
		this.waypoint = waypoint;
	}
	
	public void setWaypoint(String waypoint) {
		this.waypoint = waypoint;
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

	public JSONObject getDirections() throws IOException{
		String startLocEnc = URLEncoder.encode(startLoc);
		String endLocEnc = URLEncoder.encode(endLoc);
		String url = "https://maps.googleapis.com/maps/api/directions/json?key=AIzaSyAu_MQiOajO2EJJbjWUKHPZQqkcygqzF2E&origin="+startLocEnc+"&destination="+endLocEnc;
		if (waypoint != null) {
			String waypointEnc = URLEncoder.encode(waypoint);
			url.concat("&waypoints="+waypointEnc);
		}
		System.out.println("GetGoogleDirections: getDirections: Using: "+url);
		URL googleMaps = new URL(url);
		URLConnection gmc = googleMaps.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(gmc.getInputStream()));
		String inputData;
		String jsonData = "";
		while ((inputData = in.readLine()) != null) {
			System.out.println("GetGoogleDirections: getDirections: partial data read: "+inputData);
			jsonData += inputData;
		}
		
		in.close();
		System.out.println("GetGoogleDirections: getDirections: finished reading remote URL");
		
		System.out.println("GetGoogleDirections: getDirections: jsonData: "+jsonData);
		if (jsonData.isEmpty()) {
			throw new IOException("No data read from server");
		}
		
		System.out.println("GetGoogleDirections: getDirections: Returned data: "+jsonData);
		
		JSONObject route;
		route = JSONObject.parse(jsonData.toString()); //(JSONObject) JSON.parse(jsonData);
		
		
		JSONObject routes = (JSONObject) route.get("routes");
		JSONArray legsArr = (JSONArray) routes.get("legs");
		int TotalTime = 0;
		int TotalDistance = 0;
		System.out.println("GetGoogleDirections: getDirections: Looping through returned legs");
		for (int i = 0; i < legsArr.size(); i++) {
			JSONObject tmpObj = (JSONObject) legsArr.get(i);
			JSONObject duration = (JSONObject) tmpObj.get("duration");
			JSONObject distance = (JSONObject) tmpObj.get("distance");
			TotalTime += Integer.parseInt(duration.get("value").toString());
			TotalDistance += Integer.parseInt(distance.get("value").toString());
		}
		route.put("TotalTime", TotalTime);
		route.put("TotalDistance", TotalDistance);
		System.out.println("GetGoogleDirections: getDirections: Total time and distance: "+TotalTime+", "+TotalDistance);
		return route;
	}
}
