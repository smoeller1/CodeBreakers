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
			JSONObject jsonResults = testObject.getDirections();
			assertFalse(jsonResults.isEmpty());
		} catch (IOException e) {
			e.printStackTrace();
		}
		testObject.setWaypoint(way);
		try {
			JSONObject jsonResults = testObject.getDirections();
			assertFalse(jsonResults.isEmpty());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setTestData() {
		start = "Kansas City, MO";
		end = "St Louis, MO";
		way = "Springfield, MO";
	}
}
