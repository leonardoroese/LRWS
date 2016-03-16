package br.com.zpi.lrws;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;

/**
 * Created by leonardo on 19/12/15.
 */
public class LRWSConverter {
	
	// ####################################################################
	// JSONObject to Java Line Object
	// ####################################################################
	
    public Object json2Object(Object o, JSONObject jso, boolean decode){
        if(jso == null)
            return null;
        if(o == null)
            return null;
        if(jso.length() <= 0)
            return null;

        JSONArray jsa = new JSONArray();
        Iterator<String> iter = jso.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            try {
                if(jso.get(key).getClass().equals(jsa.getClass())){
                    String clname = o.getClass().getDeclaredField(key).getType().getName();
                    if(clname.trim().indexOf("[") == 0)
                    	clname = clname.substring(2, clname.length() - 1);
                    Object[] ao = (Object[]) Array.newInstance(Class.forName(clname),((JSONArray)jso.get(key)).length());
                    int cnob = 0;
                    for(Object xo : ao){
                        ao[cnob] = Class.forName(clname).newInstance();
                        cnob++;
                    }
                    ao = json2ObjectA(ao, (JSONArray) jso.get(key), decode);
                    if(ao != null){
                        o.getClass().getDeclaredField(key).set(o, ao);
                    }
                }else if(jso.get(key).getClass().equals(jso.getClass())){
                    String clname = o.getClass().getDeclaredField(key).getType().getName();
                    if(clname.trim().indexOf("[") == 0)
                    	clname = clname.substring(2, clname.length() - 1);
                    Object no = Class.forName(clname).newInstance();
                    no = json2Object(no, jso.getJSONObject(key), decode);
                    o.getClass().getDeclaredField(key).set(o, no);
                }else{
                    if (o.getClass().getDeclaredField(key) != null) {
                        if(decode)
                            o.getClass().getDeclaredField(key).set(o, URLDecoder.decode((String) jso.get(key).toString(), "UTF-8"));
                        else
                            o.getClass().getDeclaredField(key).set(o, (String) jso.get(key).toString());
                    }
                }
            } catch (Exception e) {
                String emsg = e.getMessage();
            }
        }

        return o;
    }

    
	// ####################################################################
	// JSONArray to Java Line Object Array
	// ####################################################################
    public Object[] json2ObjectA(Object[] o, JSONArray jsa, boolean decode){
        if(jsa == null)
            return null;
        if(o == null)
            return null;
        if(jsa.length() <= 0)
            return null;
        for(int i = 0; i < jsa.length(); i++)
        {
            try {
                String clname = o.getClass().getName();
                String clnameredux = clname.substring(2, clname.length() - 1);
                o[i] = Class.forName(clnameredux).newInstance();
                if(jsa.get(i).getClass().equals(jsa.getClass())){
                    //is array
                    Object[] ao = json2ObjectA(o, jsa.getJSONArray(i), decode);
                    o[i] = ao;
                }else{
                    // Is object
                    Object oo = json2Object(o[i], jsa.getJSONObject(i), decode);
                    o[i] = oo;
                }
            }catch(Exception e){
                String emsg = e.getMessage();
            }
        }
        return o;
    }

    public JSONObject mod2json(Object o, boolean encoded) {
        if (o == null)
            return null;
        JSONObject jout = new JSONObject();
        for (Field f : o.getClass().getDeclaredFields()) {
            try {
                if (f.get(o) != null && f.get(o).getClass().isArray()) {
                    JSONArray ao = mod2json((Object[]) f.get(o), true);
                    jout.put(f.getName(), ao);
                } else {
                    String val = (String) f.get(o);
                    if (val != null)
                        if (encoded)
                            jout.put(f.getName(), URLEncoder.encode((String) f.get(o), "UTF-8"));
                        else
                            jout.put(f.getName(), f.get(o));
                    else
                        jout.put(f.getName(), "");
                }
            } catch (Exception e) {

            }
        }
        return jout;
    }


    public JSONArray mod2json(Object[] ao, boolean encoded) {
        if (ao == null)
            return null;
        JSONArray jout = new JSONArray();
        for (Object o : ao) {
            JSONObject jso = new JSONObject();
            for (Field f : o.getClass().getDeclaredFields()) {
                try {
                    if (f.get(o) != null && f.get(o).getClass().isArray()) {
                        JSONArray aao = mod2json((Object[]) f.get(o), true);
                        jso.put(f.getName(), aao);
                    } else {
                        String val = (String) f.get(o);
                        if (val != null)
                            if (encoded)
                                jso.put(f.getName(), URLEncoder.encode((String) f.get(o), "UTF-8"));
                            else
                                jso.put(f.getName(), f.get(o));
                        else
                            jso.put(f.getName(), "");
                    }
                } catch (Exception e) {

                }
            }
            jout.put(jso);
        }
        return jout;
    }
}
