package edu.umkc.mongorestapp;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.ibm.json.java.JSONObject;

public class GetGoogleDirectionsTest {
	String start;
	String end;
	String way;

	@Test
	public void testGetGoogleDirectionsStringString() {
		setTestData();
		GetGoogleDirections testObject = new GetGoogleDirections(start, end);
		assertEquals(start, testObject.getStartLoc());
		assertEquals(end, testObject.getEndLoc());
	}

	@Test
	public void testGetGoogleDirectionsStringStringString() {
		setTestData();
		GetGoogleDirections testObject = new GetGoogleDirections(start, end, way);
		assertEquals(start, testObject.getStartLoc());
		assertEquals(end, testObject.getEndLoc());
		assertEquals(way, testObject.getWaypoint());
	}

	@Test
	public void testSetWaypoint() {
		setTestData();
		GetGoogleDirections testObject = new GetGoogleDirections(start, end);
		testObject.setWaypoint(way);
		assertEquals(way, testObject.getWaypoint());
	}

	@Test
	public void testGetDirections() {
		setTestData();
		GetGoogleDirections testObject = new GetGoogleDirections(start, end);
		try {
			JSONObject fullResults = new JSONObject();
			JSONObject jsonResults = testObject.getDirections(fullResults);
			assertFalse(jsonResults.isEmpty());
			assertFalse(fullResults.isEmpty());
		} catch (IOException e) {
			e.printStackTrace();
		}
		testObject.setWaypoint(way);
		try {
			JSONObject fullResults = new JSONObject();
			JSONObject jsonResults = testObject.getDirections(fullResults);
			assertFalse(jsonResults.isEmpty());
			assertFalse(fullResults.isEmpty());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAssignNodeGps() {
		setTestData();
		JSONObject leg = new JSONObject();
		JSONObject startLocation = new JSONObject();
		JSONObject returnResults = new JSONObject();
		GetGoogleDirections testObject = new GetGoogleDirections(start, end);
		int nodeNumber = 0;
		startLocation.put("lat", 38.1234);
		startLocation.put("lng", 91.234);
		leg.put("start_location", startLocation);
		testObject.assignNodeGps(leg, returnResults, nodeNumber);
		//route->node0->start_location->lat/lng
		assertTrue(returnResults.containsKey("route"));
		JSONObject route = (JSONObject) returnResults.get("route");
		assertTrue(route.containsKey("node0"));
		JSONObject start_location = (JSONObject) route.get("node0");
		assertTrue(start_location.containsKey("lat"));
		assertEquals(start_location.get("lat").toString(), startLocation.get("lat").toString());
	}

	private void setTestData() {
		start = "Kansas City, MO";
		end = "St Louis, MO";
		way = "Springfield, MO";
	}
}
