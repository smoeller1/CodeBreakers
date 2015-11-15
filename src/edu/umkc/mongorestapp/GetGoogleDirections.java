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
import com.sun.xml.internal.bind.v2.runtime.reflect.ListIterator;

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

	public JSONObject getDirections(JSONObject mapRoute) throws IOException{
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
			//System.out.println("GetGoogleDirections: getDirections: partial data read: "+inputData);
			jsonData += inputData;
		}
		
		in.close();
		System.out.println("GetGoogleDirections: getDirections: finished reading remote URL");
		
		System.out.println("GetGoogleDirections: getDirections: jsonData: "+jsonData);
		if (jsonData.isEmpty()) {
			throw new IOException("No data read from server");
		}
		
		System.out.println("GetGoogleDirections: getDirections: Returned data: "+jsonData);
		
		JSONObject rawRouteData = JSONObject.parse(jsonData.toString());
		mapRoute.put("routes", rawRouteData.get("routes"));
		System.out.println("GetGoogleDirections: getDirections: mapRoute size: "+mapRoute.size());
		//populateTestData(mapRoute);  //used only for testing
		

		/* We assume that the route directions provided by the Google API are consistent
		 * between multiple calls in quick succession for the same start, end, and waypoint
		 * This means that we assume Google returned to us it's optimal route here, and
		 * we also assume it will provide this same optimal route again when the AngularJS
		 * frontend asks for it. We just need to format the return object properly
		 */
		JSONObject returnResults = new JSONObject(); //The JSON object to return to the caller
		JSONObject lastNode = new JSONObject();
		int TotalTime = 0;
		int TotalDistance = 0;
		JSONArray routesArr = (JSONArray) mapRoute.get("routes");
		for (int j = 0; j < routesArr.size(); j++) {
			JSONObject routes = (JSONObject) routesArr.get(j);
			JSONArray legsArr = (JSONArray) routes.get("legs");
			System.out.println("GetGoogleDirections: getDirections: Looping through returned legs");
			java.util.ListIterator legsArrIter = legsArr.listIterator();
			while (legsArrIter.hasNext()) {
				JSONObject tmpObj = (JSONObject) legsArrIter.next();
				JSONArray steps = (JSONArray) tmpObj.get("steps");
				if (!returnResults.containsKey("Node0")) {
					System.out.println("GetGoogleDirections: getDirections: Found a Node0");
					//TODO: for some reason this is not saving the assigned Node0
					assignNodeGps((JSONObject) steps.get(0), returnResults, 0);
				}
				lastNode = (JSONObject) steps.get(steps.size() - 1);
				JSONObject duration = (JSONObject) tmpObj.get("duration");
				JSONObject distance = (JSONObject) tmpObj.get("distance");
				TotalTime += Integer.parseInt(duration.get("value").toString());
				TotalDistance += Integer.parseInt(distance.get("value").toString());
			}
		}
		assignNodeGps(lastNode, returnResults, 1);
		returnResults.put("TotalTime", TotalTime);
		returnResults.put("TotalDistance", TotalDistance);
		mapRoute.put("TotalTime",  TotalTime);
		System.out.println("GetGoogleDirections: getDirections: Total time and distance: "+TotalTime+", "+TotalDistance);
		System.out.println("GetGoogleDirections: getDirections: mapRoute size before return: "+mapRoute.size());
		return returnResults;
	}

	protected void assignNodeGps(JSONObject leg, JSONObject returnResults, int nodeNumber) {
		System.out.println("GetGoogleDirections: assignNodeGps: Called with nodeNumber="+Integer.toString(nodeNumber));
		JSONObject thisNode = (JSONObject) leg.get("start_location");
		JSONObject tmpObj = new JSONObject();
		if (returnResults.containsKey("route")) {
			tmpObj = (JSONObject) returnResults.get("route");
		}
		tmpObj.put("node"+Integer.toString(nodeNumber), thisNode);
		returnResults.put("route", tmpObj);
	}

	private void populateTestData(JSONObject route) {
		/* create our generic duration object of 222 seconds */
		JSONObject duration = new JSONObject();
		duration.put("value", 222);
		
		/* create our generic GPS points for this trip */
		JSONObject start1 = new JSONObject();
		JSONObject start2 = new JSONObject();
		JSONObject start3 = new JSONObject();
		JSONObject end = new JSONObject();
		start1.put("lat", "39.0997252,");
		start1.put("lng", "-94.57853689999999");
		start2.put("lat", "39.0996221,");
		start2.put("lng", "-94.573937");
		start3.put("lat", "39.0305842,");
		start3.put("lng", "-94.27532789999999");
		end.put("lat", "38.8061467,");
		end.put("lng", "-90.84215929999999");
		
		/* now use this info to create skeleton step points */
		JSONObject step1 = new JSONObject();
		JSONObject step2 = new JSONObject();
		JSONObject step3 = new JSONObject();
		step1.put("duration", duration);
		step1.put("start_location", start1);
		step1.put("end_location", start2);
		step2.put("duration", duration);
		step2.put("start_location", start2);
		step2.put("end_location", start3);
		step3.put("duration", duration);
		step3.put("start_location", start3);
		step3.put("end_location", end);
		
		/* then create the steps array */
		JSONArray steps = new JSONArray();
		steps.add(step1);
		steps.add(step2);
		steps.add(step3);
		
		/* and then finish the skeleton for this 1 leg trip */
		JSONObject leg1 = new JSONObject();
		leg1.put("steps", steps);
		leg1.put("start_location", start1);
		leg1.put("end_location", end);
		JSONObject distance = new JSONObject();
		distance.put("value", "30000");
		JSONObject dur = new JSONObject();
		dur.put("value", "666");
		leg1.put("duration", dur);
		leg1.put("distance", distance);
		JSONArray legs = new JSONArray();
		legs.add(leg1);
		JSONObject route1 = new JSONObject();
		route1.put("legs", legs);
		JSONArray routes = new JSONArray();
		routes.add(route1);
		
		route.put("routes", routes);
	}
}
