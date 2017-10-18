package br.com.zpi.lrws.servlet;
import org.json.JSONArray;
import org.json.JSONObject;

public class LRWSEndpointMap {
	
	public JSONArray map = null;
	
	public LRWSEndpointMap(String jsonmap) {
		try {
			map = new JSONArray(jsonmap);
		}catch(Exception e) {
			
		}
	}
	
	public String getServletReceiver(String endpoint) {
		if(map != null) {
			for(int i = 0; i < map.length(); i++) {
				JSONObject jo = map.getJSONObject(i);
				if(jo.has("endpoint") && jo.has("servlet")) {
					if(jo.getString("endpoint").toUpperCase().trim().equals(endpoint.trim().toUpperCase())) {
						return jo.getString("servlet");
					}
				}
			}
		}
		return null;
	}
}
