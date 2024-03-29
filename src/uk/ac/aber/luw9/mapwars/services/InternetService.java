package uk.ac.aber.luw9.mapwars.services;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import uk.ac.aber.luw9.mapwars.User;
import uk.ac.aber.luw9.mapwars.units.UnitType;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetService {

	Context context;
	TCPClient tcpClient;
	User user;
	
	public InternetService(Context context) {
		this.context = context;
		tcpClient = new TCPClient();
	}
	
	/**
	 * Check for network connection
	 * @return is network connection available
	 */
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	   
	    return (activeNetworkInfo != null
	    		&& activeNetworkInfo.isAvailable()
                && activeNetworkInfo.isConnected());
	}
	
	public boolean getNetworkAvailability() {
		return isNetworkAvailable();
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	public boolean startThread() {
		if (!isNetworkAvailable())
			return false;
		
		tcpClient.startThread();
		return true;
	}
	
	public void stopThread() {
		tcpClient.stopThread();
	}
	
	public boolean login(String user, String pass) {
		if (!isNetworkAvailable())
			return false;
		
		JSONObject jsonObject = new JSONObject();
        try {
        	jsonObject.put("action", "user.login");
			jsonObject.put("user", user);
	        jsonObject.put("pass", pass);
			tcpClient.sendMessage(jsonObject.toString());
			
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
		}
        
        return false;
	}
	
	/**
	 * Create json to update user location and send to server
	 * 
	 * @param location Users location
	 */
	public void updateLocation(Location location) {	
		if (location == null)
			return;
		
		JSONObject jsonObject = new JSONObject();
        try {
        	jsonObject.put("action", "user.location");
			jsonObject.put("userID", user.getUserId());
			jsonObject.put("sess", user.getSession());
	        jsonObject.put("lat", location.getLatitude());
	        jsonObject.put("lon", location.getLongitude());
			tcpClient.sendMessage(jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create json to make new unit and send to server
	 * 
	 * @param type unit type
	 * @param loc units location
	 */
	public void createUnit(UnitType type, Location loc) {
		JSONObject jsonObject = new JSONObject();
        try {
        	jsonObject.put("action", "unit.create");
			jsonObject.put("userID", user.getUserId());
			jsonObject.put("sess", user.getSession());
	        jsonObject.put("lat", loc.getLatitude());
	        jsonObject.put("lon", loc.getLongitude());
	        jsonObject.put("type", type);
			tcpClient.sendMessage(jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create json to move unit and send to server
	 * 
	 * @param type unit type
	 * @param loc units location
	 */	
	public void moveUnit(int i, GeoPoint pt) {
		JSONObject jsonObject = new JSONObject();
        try {
        	jsonObject.put("action", "unit.move");
			jsonObject.put("userID", user.getUserId());
			jsonObject.put("sess", user.getSession());
			jsonObject.put("id", i);
	        jsonObject.put("lat", pt.getLatitudeE6()/1E6);
	        jsonObject.put("lon", pt.getLongitudeE6()/1E6);
			tcpClient.sendMessage(jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
