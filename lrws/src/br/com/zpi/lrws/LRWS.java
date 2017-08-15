package br.com.zpi.lrws;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by leonardo on 19/12/15.
 */
public class LRWS {

	private LRWSConverter converter = new LRWSConverter();

	/*
	 * #########################################################################
	 * ##### CALL WS Return Array of Object
	 * #########################################################################
	 * #####
	 */

	public Object[] callArray(String classname, String arrayparam, String wshost, String wsname, String[][] params,
			JSONObject jso, boolean encoded, int method) throws LRWSException {
		LRWSException e = new LRWSException();

		if (classname == null || classname.trim().length() <= 0) {
			e.setMessage("You have to inform a classname.");
			throw e;
		}

		LRWSConnector ws = new LRWSConnector();
		String callres = ws.callWSAS(wshost, wsname, params, jso, encoded, method);
		if (callres != null && callres.trim().indexOf("[") >= 0) {
			try {
				JSONArray jsa = null;
				if (arrayparam != null && arrayparam.trim().length() > 0) {
					JSONObject jsoret = new JSONObject(callres);
					if (jso.has(arrayparam))
						jsa = jsoret.getJSONArray(arrayparam);
				} else {
					jsa = new JSONArray(callres);
				}
				if (jsa != null) {
					Object uout = Array.newInstance(Class.forName(classname), jsa.length());
					Object[] rout = (Object[]) uout;
					rout = converter.json2ObjectA(rout, jsa, true);
					return rout;
				}
				return null;
			} catch (Exception ex) {
				e.setMessage("Error parsing Data");
				throw e;
			}
		} else {
			e.setMessage("Web Service not executed");
			throw e;
		}
	}

	/*
	 * #########################################################################
	 * ##### CALL WS Return Simple Object
	 * #########################################################################
	 * #####
	 */

	public Object call(String classname, String wshost, String wsname, String[][] params, JSONObject jso,
			boolean encoded, int method) throws LRWSException {
		LRWSException e = new LRWSException();

		if (classname == null || classname.trim().length() <= 0) {
			e.setMessage("You have to inform a classname.");
			throw e;
		}

		LRWSConnector ws = new LRWSConnector();
		String callres = ws.callWSAS(wshost, wsname, params, jso, encoded, method);
		if (callres != null) {
			try {
				Object ref = Class.forName(classname).newInstance();
				JSONObject jsoret = new JSONObject(callres);
				return converter.json2Object(ref, jsoret, true);
			} catch (Exception ex) {
				e.setMessage("Error parsing Data");
				throw e;
			}
		} else {
			e.setMessage("Web Service not executed");
			throw e;
		}
	}

	/*
	 * #########################################################################
	 * ##### CALL WS Return String JSON
	 * #########################################################################
	 * #####
	 */

	public String callSimple(String wshost, String wsname, String[][] params, JSONObject jso, boolean encoded,
			int method) throws LRWSException {
		LRWSException e = new LRWSException();
		LRWSConnector ws = new LRWSConnector();
		String callres = ws.callWSAS(wshost, wsname, params, jso, encoded, method);
		if (callres != null) {
			return callres;
		} else {
			e.setMessage("Web Service not executed");
			throw e;
		}
	}

	/*
	 * #########################################################################
	 * ##### Convert Object to JSONObject
	 * #########################################################################
	 * #####
	 */

	public JSONObject lin2json(Object o, boolean encoded) {
		return lin2json(o, encoded, false);
	}

	public JSONObject lin2json(Object o, boolean encoded, boolean ignorenull) {
		if (o == null)
			return null;
		JSONObject jout = new JSONObject();

		// Get current class and superclass fields
		Field[] ofields = null;
		Field[] supfields = null;
		Field[] allfields = null;
		ofields = o.getClass().getDeclaredFields();
		if (o.getClass().getSuperclass() != null) {
			supfields = o.getClass().getSuperclass().getDeclaredFields();
			allfields = new Field[ofields.length + supfields.length];
			int c = 0;
			for (Field f : ofields) {
				allfields[c] = f;
				c++;
			}
			for (Field f : supfields) {
				allfields[c] = f;
				c++;
			}
		} else {
			allfields = ofields;
		}

		for (Field f : allfields) {
			// Only public fields
			if (Modifier.isPublic(f.getModifiers())) {
				try {
					if (f.get(o) != null && f.get(o).getClass().isArray()) {
						JSONArray ao = lin2json((Object[]) f.get(o), encoded);
						jout.put(f.getName(), ao);
					} else {
						String val = null;
						if (f.get(o).getClass().getName().indexOf("String") < 0) {
							JSONObject t_jo = lin2json(f.get(o), encoded);
							if (t_jo != null)
								jout.put(f.getName(), t_jo);
							else
								jout.put(f.getName(), "");
						} else {
							val = (String) f.get(o);
							if (val != null)
								if (encoded)
									jout.put(f.getName(), URLEncoder.encode((String) f.get(o), "UTF-8"));
								else
									jout.put(f.getName(), f.get(o));
							else if (!ignorenull)
								jout.put(f.getName(), "");
						}
					}
				} catch (Exception e) {

				}
			}
		}
		return jout;
	}

	/*
	 * #########################################################################
	 * ##### Convert Object to JSONArray
	 * #########################################################################
	 * #####
	 */
	public JSONArray lin2json(Object[] ao, boolean encoded) {
		if (ao == null)
			return null;
		JSONArray jout = new JSONArray();
		for (Object o : ao) {
			JSONObject jso = new JSONObject();
			for (Field f : o.getClass().getDeclaredFields()) {
				// Only public fields
				if (Modifier.isPublic(f.getModifiers())) {
					try {
						if (f.get(o) != null && f.get(o).getClass().isArray()) {
							JSONArray aao = lin2json((Object[]) f.get(o), true);
							jso.put(f.getName(), aao);
						} else {
							String val = null;
							if (f.get(o).getClass().getName().indexOf("String") < 0) {
								JSONObject t_jo = lin2json(f.get(o), encoded);
								if (t_jo != null)
									jso.put(f.getName(), t_jo);
								else
									jso.put(f.getName(), "");
							} else {
								val = (String) f.get(o);
								if (val != null)
									if (encoded)
										jso.put(f.getName(), URLEncoder.encode((String) f.get(o), "UTF-8"));
									else
										jso.put(f.getName(), (String) f.get(o));
								else
									jso.put(f.getName(), "");
							}

						}
					} catch (Exception e) {
						jso.put(f.getName(), "");
					}
				} // public fields
			}
			jout.put(jso);
		}
		return jout;
	}

	/*
	 * #########################################################################
	 * ##### Convert JSONObject to Object
	 * #########################################################################
	 * #####
	 */

	public Object json2Object(Object o, JSONObject jso, boolean decode) {
		if (jso == null)
			return null;
		if (o == null)
			return null;
		if (jso.length() <= 0)
			return null;

		JSONArray jsa = new JSONArray();
		Iterator<String> iter = jso.keys();
		while (iter.hasNext()) {
			String key = iter.next();
			try {
				if (jso.get(key).getClass().equals(jsa.getClass())) {
					// IS JSON ARRAY?
					String clname = o.getClass().getDeclaredField(key).getType().getName();
					if (clname.trim().indexOf("[") == 0)
						clname = clname.substring(2, clname.length() - 1);
					Object[] ao = (Object[]) Array.newInstance(Class.forName(clname),
							((JSONArray) jso.get(key)).length());
					int cnob = 0;
					for (Object xo : ao) {
						ao[cnob] = Class.forName(clname).getConstructors()[0].newInstance();
						cnob++;
					}
					ao = json2ObjectA(ao, (JSONArray) jso.get(key), decode);
					if (ao != null) {
						o.getClass().getDeclaredField(key).set(o, ao);
						if (o.getClass().getSuperclass() != null)
							o.getClass().getSuperclass().getDeclaredField(key).set(o, ao);
					}
				} else if (jso.get(key).getClass().equals(jso.getClass())) {
					// IS JSON OBJECT?
					String clname = o.getClass().getDeclaredField(key).getType().getName();
					if (clname.trim().indexOf("[") == 0)
						clname = clname.substring(2, clname.length() - 1);
					Object no = Class.forName(clname).getConstructors()[0].newInstance();
					no = json2Object(no, jso.getJSONObject(key), decode);
					o.getClass().getDeclaredField(key).set(o, no);
					if (o.getClass().getSuperclass() != null)
						o.getClass().getSuperclass().getDeclaredField(key).set(o, no);
				} else {
					// IS OTHER OBJECT (STRING)?
					if (o.getClass().getDeclaredField(key) != null) {
						if (decode) {
							o.getClass().getDeclaredField(key).set(o,
									URLDecoder.decode((String) jso.get(key).toString(), "UTF-8"));
							if (o.getClass().getSuperclass() != null)
								o.getClass().getSuperclass().getDeclaredField(key).set(o,
										URLDecoder.decode((String) jso.get(key).toString(), "UTF-8"));
						} else {
							o.getClass().getDeclaredField(key).set(o, (String) jso.get(key).toString());
							if (o.getClass().getSuperclass() != null)
								o.getClass().getSuperclass().getDeclaredField(key).set(o,
										(String) jso.get(key).toString());
						}
					}
				}
			} catch (Exception e) {
				String emsg = e.getMessage();
			}
		}

		return o;
	}

	/*
	 * #########################################################################
	 * ##### Convert JSON Array to Object Array
	 * #########################################################################
	 * #####
	 */
	public Object[] json2ObjectA(Object[] o, JSONArray jsa, boolean decode) {
		if (jsa == null)
			return null;
		if (o == null)
			return null;
		if (jsa.length() <= 0)
			return null;
		for (int i = 0; i < jsa.length(); i++) {
			try {
				String clname = o.getClass().getName();
				String clnameredux = clname.substring(2, clname.length() - 1);
				if (o[i] == null)
					o[i] = Class.forName(clnameredux).getConstructors()[0].newInstance();
				if (jsa.get(i).getClass().equals(jsa.getClass())) {
					// is array
					Object[] ao = json2ObjectA(o, jsa.getJSONArray(i), decode);
					o[i] = ao;
				} else {
					// Is object
					Object oo = json2Object(o[i], jsa.getJSONObject(i), decode);
					o[i] = oo;
				}
			} catch (Exception e) {
				String emsg = e.getMessage();
			}
		}
		return o;
	}
}
