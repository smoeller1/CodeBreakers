package edu.umkc.mongorestapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import com.ibm.json.java.JSON;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

/* Uses the Weather Underground (www.wunderground.com) API to get weather information for multiple points
 * along a driving route. The driving route information is expected to be in Google Maps JSON format
 */

public class GetWeather {
	private JSONObject directions;
	int numberOfLegs; //There will be +1 more than this is set to, due to the starting point

	public GetWeather(JSONObject directions) {
		super();
		this.directions = directions;
		numberOfLegs = 9; //default to 10 total legs (9 + starting point)
	}
	
	public GetWeather(JSONObject directions, int numberOfLegs) {
		super();
		this.directions = directions;
		this.numberOfLegs = numberOfLegs - 1; //account for the starting point taking a spot
	}
	
	public void setNumberOfLegs(int numberOfLegs) {
		this.numberOfLegs = numberOfLegs - 1; //account for the starting point taking a spot
	}

	/* Takes a Google Maps formatted JSON route and adds the following JSON object to returnObj.
	 * with an ordered array with the same number of elements as numberOfLegs+1
	 * WeatherInfo: [
	 *   { temp_f: value,
	 *     temp_c: value,
	 *     icon: short description of weather (generally 1 word),
	 *     icon_url: URL of pertinent weather icon image,
	 *     travelTime: estimated time, in seconds, to reach this weather location,
	 *     extremeEvent: (optional) if there is an extreme event, like tornado, this element will be populated with details
	 *   }
	 * ]
	 */
	public void parseDirections(JSONObject returnObj) throws IOException {
		JSONObject[] weatherInfo = new JSONObject[numberOfLegs + 1];
		int weatherInfoC = 0;
		
		int totalTime = Integer.parseInt((String) directions.get("TotalTime"));
		
		int legTime = totalTime / numberOfLegs;
		if (legTime < 1) {
			/* just a safety check, in case a very short route is passed in */
			legTime = 1;
		}
		
		JSONObject routes = (JSONObject) directions.get("routes");
		JSONArray legsArr = (JSONArray) routes.get("legs");
		int secondsSinceLast = 0;
		
		/* Walk through all of the legs of the trip. Each leg can contain multiple smaller steps,
		 * and there can be multiple legs, particularly on longer cross country trips */
		for (int i = 0; i < legsArr.size(); i++) {
			JSONObject tmpObj = (JSONObject) legsArr.get(i);
			JSONArray stepsArr = (JSONArray) tmpObj.get("steps");
			
			/* Now to walk through all of the sub steps that make up this leg, since we will frequently
			 * want weather information for points within a single leg */
			for (int j = 0; j < stepsArr.size(); j++) {
				JSONObject thisStep = (JSONObject) stepsArr.get(j);
				JSONObject duration = (JSONObject) thisStep.get("duration");
				int dValue = Integer.parseInt((String) duration.get("value"));	
				JSONObject startLoc = (JSONObject) thisStep.get("start_location");
				float startLat = Float.parseFloat(startLoc.get("lat").toString());
				float startLng = Float.parseFloat(startLoc.get("lng").toString());
				JSONObject endLoc = (JSONObject) thisStep.get("end_location");
				float endLat = Float.parseFloat(endLoc.get("lat").toString());
				float endLng = Float.parseFloat(endLoc.get("lng").toString());
				
				/* Check if this is the first time through, and if so save the starting point weather */
				if (weatherInfoC == 0) {
					weatherInfo[weatherInfoC++] = getSingleWeather(startLoc.get("lat").toString(), startLoc.get("lng").toString(), weatherInfoC * legTime);
				}
				
				/* Check if the duration of this leg will put us past our next update point */
				while (dValue + secondsSinceLast > legTime) {
					float ratioNeeded = ((float) (legTime - secondsSinceLast)) / (float) dValue;
					String legLat = Float.toString((endLat - startLat) * ratioNeeded + startLat);
					String legLng = Float.toString((endLng - startLng) * ratioNeeded + startLng);
					weatherInfo[weatherInfoC++] = getSingleWeather(legLat, legLng, weatherInfoC * legTime);
					
					/* Now to see if this leg.step in the path needs to be subdivided into even more weather points */
					if (secondsSinceLast + dValue >= legTime) {
						dValue -= legTime - secondsSinceLast;
						if (dValue < legTime) {
							/* This was the last subdivided point in this leg.step */
							secondsSinceLast = dValue;
							dValue = 0;
						} else {
							/* There are even more subdivided points that we need to get out of this leg.step */
							secondsSinceLast = 0;
						}
					} else {
						dValue -= legTime - secondsSinceLast;
						secondsSinceLast = (secondsSinceLast + dValue) % legTime;
					} //if
				} //while
			} //for steps
		} //for legs
		returnObj.put("WeatherInfo", weatherInfo);
	}
	
	/* getSingleWeather returns a JSON object of:
	 * { temp_f: value,
	 *   temp_c: value,
	 *   icon: short description of weather (generally 1 word),
	 *   icon_url: URL of pertinent weather icon image,
	 *   travelTime: estimated time, in seconds, to reach this weather location,
	 *   extremeEvent: (optional) if there is an extreme event, like tornado, this element will be populated with details
	 * }
	 * Data comes from the Weather Underground REST API
	 */
	private JSONObject getSingleWeather(String lat, String lon, int travelTime) throws IOException {
		JSONObject returnObj = new JSONObject(); //parsed results to be returned to calling method
		
		String url = "http://api.wunderground.com/api/84d3abefc608dbdd/conditions/q/" + lat + "," + lon + ".json";
		
		URL wUnderground = new URL(url);
		URLConnection wuc = wUnderground.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(wuc.getInputStream()));
		String inputData;
		String jsonData = "";
		while ((inputData = in.readLine()) != null) {
			jsonData.concat(inputData);
		}
		in.close();
		
		JSONObject weatherResults = (JSONObject) JSON.parse(jsonData);
		JSONObject currentObservation = (JSONObject) weatherResults.get("current_observation");
		returnObj.put("temp_f", currentObservation.get("temp_f").toString());
		returnObj.put("temp_c", currentObservation.get("temp_c").toString());
		returnObj.put("icon", currentObservation.get("icon").toString());
		returnObj.put("icon_url", currentObservation.get("icon_url").toString());
		returnObj.put("travelTime", travelTime);
		
		return returnObj;
	}
}
