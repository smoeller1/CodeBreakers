package edu.umkc.mongorestapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.json.java.JSON;
import com.ibm.json.java.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteResult;

/**
 * Servlet implementation class UserServlet
 */
@WebServlet("/user")
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
        
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    private void runDbQuery(DBCollection users, BasicDBObject queryObj, BasicDBObject results) {
    	System.out.println("UserServlet: runDbQuery: Starting");
    	DBCursor docs = users.find(queryObj);
    	try {
    		if (docs.hasNext()) { //we will only support pulling a single user for now
    			DBObject thisResult = docs.next();
    			results.putAll(thisResult);
    		}
    	} finally {
    		docs.close();
    	}
    	System.out.println("UserServlet: runDbQuery: Finished");
    }


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		StringBuilder buffer = new StringBuilder();
		BufferedReader reader = request.getReader();
		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println("UserServlet: doGet: partial buffer data: " + line + " :end");
			buffer.append(line);
		}
		String data = buffer.toString();
		System.out.println("UserServlet: doGet: Put buffer: " + data);
		
		Map<String,String[]> queryStr = request.getParameterMap();
		System.out.println("UserServlet: doGet: Recieved queryStr: " + queryStr.toString());
		

		MongoClientURI uri = new MongoClientURI("mongodb://mongodbi:password@ds035004.mongolab.com:35004/quasily");
		MongoClient client = new MongoClient(uri);
		DB db = client.getDB(uri.getDatabase());
		DBCollection users = db.getCollection("BuRMA");

		JSONObject returnObj = new JSONObject();
		
		if (data.isEmpty()) {
			System.out.println("ERROR: No POST data");
			returnObj.put("Status",  2);
			returnObj.put("StatusReason", "No data passed");
		} else {
			JSONObject postData = (JSONObject) JSON.parse(data);
			if (postData.get("RequestType") == null) {
				System.out.println("ERROR: No RequestType passed");
				returnObj.put("Status",  2);
				returnObj.put("StatusReason", "No RequestType passed");
			} else {
				switch (postData.get("RequestType").toString()) {
				case "1":
					createNewUser(returnObj, users, postData);
					break;
				case "2":
					authenticateUser(returnObj, users, postData);
					break;
				case "3":
					updateUser(returnObj, users, postData);
					break;
				case "4":
					deleteAccount(returnObj, users, postData);
					break;
				case "5":
					forgotPassword(returnObj, users, postData);
					break;
				case "6":
					lockUser(returnObj, users, postData);
					break;
				case "10":
					getDirections(returnObj, users, postData);
					break;
				default:
					returnObj.put("Status", 2);
					returnObj.put("StatusReason", "Invalid RequestType");
					break;
				}
			}
		}

				
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
		response.setHeader("Access-Control-Max-Age",  "86400");
		response.getWriter().append(returnObj.toString());
	}

	private void getWeather(JSONObject returnObj, JSONObject fullRoute) {
		if (fullRoute == null) {
			returnObj.put("Status", 0);
			returnObj.put("StatusReason", "Driving route information not available");
			return;
		}
		if (fullRoute.isEmpty()) {
			returnObj.put("Status", 0);
			returnObj.put("StatusReason", "Driving route information not available");
			return;
		}
		GetWeather weather = new GetWeather(fullRoute);
		try {
			weather.parseDirections(returnObj);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void lockUser(JSONObject returnObj, DBCollection users, JSONObject postData) {
		if (postData.get("Username") == null) {
			returnObj.put("Status", 0);
			returnObj.put("StatusReason", "No username included");
			return;
		}
		
		BasicDBObject queryObj = new BasicDBObject("Username", postData.get("Username"));
		BasicDBObject queryResults = new BasicDBObject();
		runDbQuery(users, queryObj, queryResults);
		
		if (queryResults.get("Username").toString().isEmpty()) {
			returnObj.put("Status", 0);
			returnObj.put("StatusReason", "Username does not exist");
			return;
		}
		
		BasicDBObject updateValues = new BasicDBObject();
		updateValues.put("accountLocked", "1");
		
		BasicDBObject update = new BasicDBObject("$set", updateValues);
		WriteResult result = users.update(queryObj, update);
		returnObj.put("Status", 1);
		returnObj.put("StatusReason", "Account locked");
	}

	private void getDirections(JSONObject returnObj, DBCollection users, JSONObject postData) {
		JSONObject fullRoute = new JSONObject();
		if (postData.get("RouteStartAddress") == null) {
			returnObj.put("Status", 0);
			returnObj.put("StatusReason", "No starting address");
			return;
		}
		if (postData.get("RouteEndAddress") == null) {
			returnObj.put("Status",  0);
			returnObj.put("StatusReason", "No ending address");
			return;
		}
		GetDirections directions = new GetDirections(postData.get("RouteStartAddress").toString(), postData.get("RouteEndAddress").toString());
		if (postData.get("WaypointAddress") != null) {
			directions.setWaypoint(postData.get("WaypointAddress").toString());
		}
		try {
			JSONObject routeMap = directions.getDirections(fullRoute);
			System.out.println("UserServlet: getDirections: fullRoute size: "+fullRoute.size());
			returnObj.put("Route", routeMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("UserServlet: getDirections: Calling getWeather with: "+returnObj.size()+" and "+fullRoute.size());
		getWeather(returnObj, fullRoute);
	}

	private void forgotPassword(JSONObject returnObj, DBCollection users, JSONObject postData) {
		if (postData.get("Username") == null) {
			returnObj.put("Status", 0);
			returnObj.put("StatusReason", "No username included");
			return;
		}
		if (postData.get("EmailAddress") == null) {
			returnObj.put("Status", 0);
			returnObj.put("StatusReason", "No email address included");
		}
		
		BasicDBObject queryObj = new BasicDBObject("Username", postData.get("Username"));
		BasicDBObject queryResults = new BasicDBObject();
		runDbQuery(users, queryObj, queryResults);
		
		if (queryResults.get("EmailAddress").toString().isEmpty()) {
			returnObj.put("Status", 0);
			returnObj.put("StatusReason", "No email address on file for user");
			return;
		}
		
		String msg = "Password for user "+postData.get("Username").toString()+" is "+queryResults.get("Password").toString();
		emailUser(queryResults.get("EmailAddress").toString(), msg);
		
		returnObj.put("Status", 1);
		returnObj.put("StatusReason", "Password emailed");
	}
    
    private void emailUser(String string, String msg) {
		// TODO Auto-generated method stub
	}

	private void authenticateUser(JSONObject returnObj, DBCollection users, JSONObject postData) {
    	System.out.println("UserServlet: authenticateUser: Starting");
		if (checkNullUP(postData)) {
			returnObj.put("Status", 0);
			returnObj.put("StatusReason", "Username and password are required");
			return;
		}
		
		BasicDBObject queryObj = new BasicDBObject("Username", postData.get("Username"));
		BasicDBObject queryResults = new BasicDBObject();
		runDbQuery(users, queryObj, queryResults);
		if (queryResults.containsKey("accountLocked")) {
			if (queryResults.get("accountLocked").toString().equals("1")) {
				returnObj.put("Status", 0);
				returnObj.put("StatusReason", "User account is locked");
				return;
			}
		}
		if (queryResults.get("Password").toString().equals(postData.get("Password"))) {
			System.out.println("UserServlet: authenticateUser: User successfully authenticated");
			returnObj.put("Status", 1);
			returnObj.put("StatusReason", "User authenticated");
			if (queryResults.containsKey("HomeAddress")) {
				returnObj.put("HomeAddress", queryResults.get("HomeAddress").toString());
			}
			if (queryResults.containsKey("OfficeAddress")) {
				returnObj.put("OfficeAddress", queryResults.get("OfficeAddress").toString());
			}
			if (queryResults.containsKey("EmailAddress")) {
				returnObj.put("EmailAddress", queryResults.get("EmailAddress").toString());
			}
			if (queryResults.containsKey("MobileNumber")) {
				returnObj.put("MobileNumber", queryResults.get("MobileNumber").toString());
			}
		} else {
			System.out.println("UserServlet: authenticateUser: User failed authentication: "
					+ queryResults.get("Password").toString() + " != " + postData.get("Password"));
			returnObj.put("Status", 0);
			returnObj.put("StatusReason", "invalid password");
		}
		System.out.println("UserServlet: authenticateUser: Finished");
    }
    
	private void deleteAccount(JSONObject returnObj, DBCollection users, JSONObject postData) {
		System.out.println("UserServlet: deleteAccount: Starting");
		if (checkNullUP(postData)) {
			returnObj.put("Status", 0);
			returnObj.put("StatusReason", "Username and password required");
			return;
		}
		
		JSONObject validateUser = new JSONObject();
		authenticateUser(validateUser, users, postData);
		if (validateUser.get("Status").toString().equals("1")) {
			//Do nothing
		} else {
			returnObj.put("Status",  0);
			returnObj.put("StatusReason", validateUser.get("StatusReason").toString());
			return;
		}
		
		BasicDBObject search = new BasicDBObject("Username", postData.get("Username").toString());
		WriteResult result = users.remove(search);
		returnObj.put("Status", 1);
		returnObj.put("StatusReason", "Account deleted");
		System.out.println("UserServlet: deleteAccount: Delete result: " + result.toString());
		
		System.out.println("UserServlet: deleteAccount: Finished");
	}

	private void updateUser(JSONObject returnObj, DBCollection users, JSONObject postData) {
		System.out.println("UserServlet: updateUser: Starting");
		if (checkNullUP(postData)) {
			returnObj.put("Status", 0);
			returnObj.put("StatusReason", "Username and password required");
			return;
		}
		
		JSONObject validateUser = new JSONObject();
		authenticateUser(validateUser, users, postData);
		if (validateUser.get("Status").toString().equals("1")) {
			//Do nothing
		} else {
			returnObj.put("Status", 0);
			returnObj.put("StatusReason", validateUser.get("StatusReason").toString());
			return;
		}
		
		BasicDBObject updateValues = new BasicDBObject();
		
		if (postData.get("NewPassword") != null) {
			updateValues.put("Password", postData.get("NewPassword"));
		}
		if (postData.get("HomeAddress") != null) {
			try {
				JSONObject address = (JSONObject) JSON.parse(postData.get("HomeAddress").toString());
				updateValues.put("HomeAddress",  address);
			} catch (NullPointerException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (postData.get("OfficeAddress") != null) {
			try {
				JSONObject address = (JSONObject) JSON.parse(postData.get("OfficeAddress").toString());
				updateValues.put("OfficeAddress", address);
			} catch (NullPointerException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (postData.get("EmailAddress") != null) {
			updateValues.put("EmailAddress",  postData.get("EmailAddress"));
		}
		if (postData.get("MobileNumber") != null) {
			updateValues.put("MobileNumber", postData.get("MobileNumber"));
		}
		
		if (updateValues.isEmpty()) {
			returnObj.put("Status", 2);
			returnObj.put("StatusReason", "No data to update");
			return;
		}
		
		BasicDBObject search = new BasicDBObject("Username", postData.get("Username").toString());
		BasicDBObject update = new BasicDBObject("$set", updateValues);
		WriteResult result = users.update(search, update);
		returnObj.put("Status", 1);
		returnObj.put("StatusReason", "Updates applied");
		System.out.println("UserServlet: updateUser: Update result: " + result.toString());
		
		System.out.println("UserServlet: updateUser: Finished");
	}
	
	private boolean checkNullUP(JSONObject postData) {
		System.out.println("UserServlet: checkNullUP: Starting");
    	if (postData.get("Username") == null) {
			System.out.println("UserServlet: checkNullUP: Username is null");
			return true;
		}
		if (postData.get("Password") == null) {
			System.out.println("UserServlet: checkNullUP: Password is null");
			return true;
		}
		System.out.println("UserServlet: checkNullUP: Starting");
		return false;
	}

	private void createNewUser(JSONObject returnObj, DBCollection users, JSONObject postData) {
		System.out.println("UserServlet: createNewUser: Starting");
		if (checkNullUP(postData)) {
			returnObj.put("Status", 0);
			returnObj.put("StatusReason", "Username and password required");
			return;
		}
		
		BasicDBObject queryObj = new BasicDBObject("Username", postData.get("Username").toString());
		BasicDBObject queryResults = new BasicDBObject();
		runDbQuery(users, queryObj, queryResults);
		if (queryResults.isEmpty()) {
			BasicDBObject insert = new BasicDBObject("Username", postData.get("Username").toString());
			insert.put("Password", postData.get("Password").toString());
			insert.put("EmailAddress", postData.get("EmailAddress").toString());
			insert.put("MobileNumber", postData.get("MobileNumber").toString());
			if (postData.get("HomeAddress") != null) {
				System.out.println("UserServlet: createNewUser: Home Address is included");
				try {
					JSONObject address = (JSONObject) JSON.parse(postData.get("HomeAddress").toString());
					insert.put("HomeAddress", address);
				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (postData.get("OfficeAddress") != null) {
				System.out.println("UserServlet: createNewUser: Office Address is included");
				try {
					JSONObject address = (JSONObject) JSON.parse(postData.get("OfficeAddress").toString());
					insert.put("OfficeAddress", address);
				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			WriteResult result = users.insert(insert);
			returnObj.put("Status", 1);
			returnObj.put("StatusReason", "New user added");
			System.out.println("UserServlet: createNewUser: Insert result: " + result.toString());
		} else {
			returnObj.put("Status", 0);
			returnObj.put("StatusReason", "User already exists");
			System.out.println("UserServlet: createNewUser: Insert failed - user already exists");
		}
		
		System.out.println("UserServlet: createNewUser: Finished");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	public void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	    //The following are CORS headers. Max age informs the 
	    //browser to keep the results of this call for 1 day.
	    resp.setHeader("Access-Control-Allow-Origin", "*");
	    resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
	    resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
	    resp.setHeader("Access-Control-Max-Age", "86400");
	    //Tell the browser what requests we allow.
	    resp.setHeader("Allow", "GET, HEAD, POST, TRACE, OPTIONS");
	}
}
