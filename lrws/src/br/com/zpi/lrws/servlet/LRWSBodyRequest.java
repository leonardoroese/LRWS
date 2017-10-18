package br.com.zpi.lrws.servlet;

import java.io.BufferedReader;
import java.net.URLDecoder;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import br.com.zpi.lrws.conn.Configurations;

public class LRWSBodyRequest {
	private String body = null;
	private HttpServletRequest request = null;
	private Map<String, String[]> Mappars = null;
	private String encoding = null;
	
	/*
	 * #########################################################################
	 * ##### CONSTRUCTOR
	 * #########################################################################
	 * #####
	 */

	public LRWSBodyRequest(HttpServletRequest req, String enc) {
		this.request = req;
		this.encoding = enc;
		if(enc == null)
			this.encoding = "UTF-8";
		
		if (request == null)
			return;
		try {
			Mappars = request.getParameterMap();
		} catch (Exception e) {

		}
		try {
			StringBuilder sb = new StringBuilder();
			BufferedReader br = request.getReader();
			String str;
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}
			this.body = sb.toString();
		} catch (Exception e) {
		}
	}

	/*
	 * #########################################################################
	 * ##### GETTER
	 * #########################################################################
	 * #####
	 */

	public String getBody() {
		return this.body;
	}

	/*
	 * #########################################################################
	 * ##### GET JSON PARS IN REQUEST PARAMETERS
	 * #########################################################################
	 * #####
	 */

	public JSONObject getParametersJSON() {
		if (Mappars != null && Mappars.size() > 0) {
			JSONObject jsonObj = new JSONObject();
			try {
				for (Map.Entry<String, String[]> entry : Mappars.entrySet()) {
					String v[] = entry.getValue();
					Object o = (v.length == 1) ? v[0] : v;
					String s = (String) o;
					try {
						s = URLDecoder.decode(s, encoding);
					} catch (Exception e) {

					}
					jsonObj.put(entry.getKey(), s);
				}
				return jsonObj;
			} catch (Exception e) {
			}
		}
		return null;
	}

	/*
	 * #########################################################################
	 * ##### GET JSON PARS IN REQUEST BODY
	 * #########################################################################
	 * #####
	 */

	public JSONObject getBodyJSON() {
		if (this.body == null)
			return null;
		try {
			JSONObject jso = new JSONObject(this.body);
			if (jso != null) {
				jso = decodeObject(jso);
				return jso;
			}
		} catch (Exception e) {
		}
		return null;
	}

	/*
	 * #########################################################################
	 * ##### GET JSON ARRAY IN REQUEST BODY
	 * #########################################################################
	 * #####
	 */

	public JSONArray getBodyJSONA() {
		if (this.body == null)
			return null;
		try {
			JSONArray jsa = new JSONArray(this.body);
			if (jsa != null) {
				jsa = decodeArray(jsa);
				return jsa;
			}
		} catch (Exception e) {

		}
		return null;
	}

	
	/*
	 * #########################################################################
	 * ##### DECODE JSON OBJECT
	 * #########################################################################
	 * #####
	 */
	private JSONObject decodeObject(JSONObject jso) {
		for (String s : JSONObject.getNames(jso)) {
			//Check if Array
			try {
				JSONArray jsa = new JSONArray(jso.getString(s));
				jsa = decodeArray(jsa);
				jso.put(s, jsa.toString());
				continue;
			} catch (Exception e) {
			}
			//Check if Another Object
			try {
				JSONObject ajso = new JSONObject(jso.getString(s));
				ajso = decodeObject(ajso);
				jso.put(s, ajso.toString());
				continue;
			} catch (Exception e) {
			}			
			try {
				jso.put(s, URLDecoder.decode(jso.getString(s),
						encoding));
			} catch (Exception e) {
			}
		}
		return jso;
	}
	
	/*
	 * #########################################################################
	 * ##### DECODE JSON ARRAY
	 * #########################################################################
	 * #####
	 */
	
	private JSONArray decodeArray(JSONArray jsa) {
		for (Object o : jsa) {
			JSONObject jso = (JSONObject)o;
			jso = decodeObject(jso);
			o = jso;
		}
		return jsa;
	}
}
