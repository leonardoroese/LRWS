package br.com.zpi.lrws;

/*import android.content.Context;
import br.com.zpi.lrws.tools.MIGBase64;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;*/

public class wsCallerAndroid {
/*
    public final static int METHOD_GET = 1;
    public final static int METHOD_POST = 2;
    public final static int METHOD_PUT = 3;
    public final static int METHOD_DELETE = 4;

    private String auth_user = null;
    private String auth_pass = null;
    private String auth_string = null;
    private Context ctx = null;

    //----------------------- CONSTRUCTOR (Auth basic)
    public wsCallerAndroid(Context ctx, String user, String pass) {
        auth_user = user;
        auth_pass = pass;
        this.ctx = ctx;
    }

    //----------------------- CONSTRUCTOR (Auth JWT)
    public wsCallerAndroid(Context ctx, String authStr) {
        auth_string = authStr;
        this.ctx = ctx;
    }


    //##############################################################################################
    // CALL ACTIONS
    //##############################################################################################
    private String callWSAS(String host, String name, String[][] params, int method) throws LRWSException {
        return callWSAS(host, name, params, method, null);
    }

    private String callWSAS(String host, String name, String[][] params, int method, Object jobj) throws LRWSException {
        String[] sendpar = null;
        
        if (host == null || host.trim().length() <= 0) {
            throw new LRWSException(LRWSException.E_PARAMS, "Forgot to inform host endpoint?");
        }
        if (name == null || name.trim().length() <= 0) {

            throw new LRWSException(LRWSException.E_PARAMS, "Forgot to inform endpoint name?");
        }

        if (method != METHOD_GET && method != METHOD_POST && method != METHOD_PUT && method != METHOD_DELETE) {
            throw new LRWSException(LRWSException.E_PARAMS, "Invalid Method");
        }

        switch (method) {
            case METHOD_GET:
                try {
                    return doGET(host + name, params);
                } catch (LRWSException e) {
                    throw e;
                }
            case METHOD_POST:
                try {
                    return doPOST(host + name, params, true, jobj);
                } catch (LRWSException e) {
                    throw e;
                }
            case METHOD_PUT:
                try {
                    return doPUT(host + name, params, true, jobj);
                } catch (LRWSException e) {
                    throw e;
                }

            case METHOD_DELETE:
                try {
                    return doDELETE(host + name, params, true, jobj);
                } catch (LRWSException e) {
                    throw e;
                }
        }
        return null;
    }

    //##############################################################################################
    // GET HTTP CALL
    //##############################################################################################

    private String doGET(String endpoint, String[][] params) throws LRWSException {
        String authbasic = null;
        String authjwt = null;
        String parcon = "";
        String linha = "";
        if (auth_user != null && auth_user.trim().length() > 0)
            authbasic = MIGBase64.encodeToString(new String(auth_user.toLowerCase() + ":" + auth_pass).getBytes(), false);
        else if (auth_string != null && auth_string.trim().length() > 0)
            authjwt = auth_string;

        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                parcon = "&" + params[i][0] + "=" + params[i][1];
                i++;
            }
        }
        try {
            HttpClient client = new DefaultHttpClient();
            HttpResponse resposta = null;
            if (parcon != null && parcon.length() > 0)
                endpoint = endpoint + "?" + parcon.substring(1);

            HttpGet wschama = new HttpGet(endpoint);
            if (authbasic != null && authbasic.trim().length() > 0)
                wschama.addHeader("Authorization", "Basic " + authbasic);
            if (authjwt != null && authjwt.trim().length() > 0)
                wschama.addHeader("JWT", authjwt);
            resposta = client.execute(wschama);
            int stt = resposta.getStatusLine().getStatusCode();

            if(resposta != null && resposta.getEntity() != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        resposta.getEntity().getContent()));
                StringBuffer sb = new StringBuffer("");
                while ((linha = br.readLine()) != null) {
                    sb.append(linha);
                }

                br.close();
                linha = sb.toString();
            }
            if (stt > 400d)
                throw new LRWSException(String.valueOf(stt), linha);
            return linha;

        } catch (Exception e) {
            throw new LRWSException(LRWSException.E_CALLERROR, e.getMessage());
        }
    }

    //##############################################################################################
    // POST HTTP CALL
    //##############################################################################################

    private String doPOST(String endpoint, String[][] params, boolean jsonpars, Object jobj) throws LRWSException {
        String authbasic = null;
        String authjwt = null;
        ArrayList<NameValuePair> par = new ArrayList<NameValuePair>();
        JSONObject jsonP = new JSONObject();
        String linha = "";
        if (auth_user != null && auth_user.trim().length() > 0)
            authbasic = MIGBase64.encodeToString(new String(auth_user.toLowerCase() + ":" + auth_pass).getBytes(), false);
        else if (auth_string != null && auth_string.trim().length() > 0)
            authjwt = auth_string;

        if (jsonpars) {
            if (jobj != null) {
                jsonP = mod2json(jobj, false, true);
            } else if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    try {
                        jsonP.put(params[i][0], params[i][1]);
                    } catch (Exception e) {

                    }
                }
            }
        } else {
            for (int i = 0; i < params.length; i++) {
                par.add(new BasicNameValuePair(params[i][0], params[i][1]));
            }
        }
        try {
            HttpClient client = new DefaultHttpClient();
            HttpResponse resposta = null;
            HttpPost wschama = new HttpPost(endpoint);
            if (authbasic != null && authbasic.trim().length() > 0)
                wschama.addHeader("Authorization", "Basic " + authbasic);
            if (authjwt != null && authjwt.trim().length() > 0)
                wschama.addHeader("JWT", authjwt);
            if (jsonpars) {
                wschama.addHeader("Content-Type", "application/json");
                wschama.setEntity(new StringEntity(jsonP.toString(), "UTF-8"));
            } else {
                wschama.setEntity(new UrlEncodedFormEntity(par, "UTF-8"));
            }

            resposta = client.execute(wschama);
            int stt = resposta.getStatusLine().getStatusCode();
            if(resposta != null && resposta.getEntity() != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        resposta.getEntity().getContent()));
                StringBuffer sb = new StringBuffer("");

                if(br != null) {
                    while ((linha = br.readLine()) != null) {
                        sb.append(linha);
                    }

                    br.close();
                }
                linha = sb.toString();
            }

            if (stt > 400d)
                throw new LRWSException(String.valueOf(stt), linha);
            return linha;

        } catch (Exception e) {
            throw new LRWSException(LRWSException.E_CALLERROR, e.getMessage());
        }
    }


    //##############################################################################################
    // PUT HTTP CALL
    //##############################################################################################

    private String doPUT(String endpoint, String[][] params, boolean jsonpars, Object jobj) throws LRWSException {
        String authbasic = null;
        String authjwt = null;
        String parcon = "";
        String linha = "";
        JSONObject jsonP = new JSONObject();
        ArrayList<NameValuePair> par = new ArrayList<NameValuePair>();

        if (auth_user != null && auth_user.trim().length() > 0)
            authbasic = MIGBase64.encodeToString(new String(auth_user.toLowerCase() + ":" + auth_pass).getBytes(), false);
        else if (auth_string != null && auth_string.trim().length() > 0)
            authjwt = auth_string;

        if (jsonpars) {
            if (jobj != null) {
                jsonP = mod2json(jobj, false, true);
            } else if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    try {
                        jsonP.put(params[i][0], params[i][1]);
                    } catch (Exception e) {

                    }
                }
            }
        } else {
            for (int i = 0; i < params.length; i++) {
                par.add(new BasicNameValuePair(params[i][0], params[i][1]));
            }
        }
        try {
            HttpClient client = new DefaultHttpClient();
            HttpResponse resposta = null;
            if (parcon != null && parcon.length() > 0)
                endpoint = endpoint + "?" + parcon;

            HttpPut wschama = new HttpPut(endpoint);
            if (authbasic != null && authbasic.trim().length() > 0)
                wschama.addHeader("Authorization", "Basic " + authbasic);
            if (authjwt != null && authjwt.trim().length() > 0)
                wschama.addHeader("JWT", authjwt);
            if (jsonpars) {
                wschama.addHeader("Content-Type", "application/json");
                wschama.setEntity(new StringEntity(jsonP.toString(), "UTF-8"));
            } else {
                wschama.setEntity(new UrlEncodedFormEntity(par, "UTF-8"));
            }

            resposta = client.execute(wschama);
            int stt = resposta.getStatusLine().getStatusCode();
            if(resposta != null && resposta.getEntity() != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        resposta.getEntity().getContent()));
                StringBuffer sb = new StringBuffer("");

                if(br != null) {
                    while ((linha = br.readLine()) != null) {
                        sb.append(linha);
                    }

                    br.close();
                }
                linha = sb.toString();
            }


            if (stt > 400d)
                throw new LRWSException(String.valueOf(stt), String.valueOf(stt));
            return linha;

        } catch (Exception e) {
            throw new LRWSException(LRWSException.E_CALLERROR, e.getMessage());
        }
    }

    //##############################################################################################
    // DELETE HTTP CALL
    //##############################################################################################

    private String doDELETE(String endpoint, String[][] params, boolean jsonpars, Object jobj) throws LRWSException {
        String authbasic = null;
        String authjwt = null;
        String parcon = "";
        String linha = "";
        JSONObject jsonP = new JSONObject();
        ArrayList<NameValuePair> par = new ArrayList<NameValuePair>();

        if (auth_user != null && auth_user.trim().length() > 0)
            authbasic = MIGBase64.encodeToString(new String(auth_user.toLowerCase() + ":" + auth_pass).getBytes(), false);
        else if (auth_string != null && auth_string.trim().length() > 0)
            authjwt = auth_string;

        try {
            HttpClient client = new DefaultHttpClient();
            HttpResponse resposta = null;
            if (parcon != null && parcon.length() > 0)
                endpoint = endpoint + "?" + parcon;

            HttpDelete wschama = new HttpDelete(endpoint);
            if (authbasic != null && authbasic.trim().length() > 0)
                wschama.addHeader("Authorization", "Basic " + authbasic);
            if (authjwt != null && authjwt.trim().length() > 0)
                wschama.addHeader("JWT", authjwt);

            resposta = client.execute(wschama);
            int stt = resposta.getStatusLine().getStatusCode();
            if(resposta != null  && resposta.getEntity() != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        resposta.getEntity().getContent()));
                StringBuffer sb = new StringBuffer("");

                if(br != null) {
                    while ((linha = br.readLine()) != null) {
                        sb.append(linha);
                    }

                    br.close();
                }
                linha = sb.toString();
            }


            if (stt > 400d)
                throw new LRWSException(String.valueOf(stt), String.valueOf(stt));
            return linha;

        } catch (Exception e) {
            throw new LRWSException(LRWSException.E_CALLERROR, e.getMessage());
        }
    }

    //##############################################################################################
    // CALL WEBSERVICE EXPECTING OBJECT[] RETURN
    //##############################################################################################

    public Object[] callArray(String classname, String arrayparam, String wshost, String wsname, String[][] params, int method) throws LRWSException {
        return callArray(classname, arrayparam, wshost, wsname, params, method, null);
    }

    public Object[] callArray(String classname, String arrayparam, String wshost, String wsname, String[][] params, int method, Object jobj) throws LRWSException {

        if (classname == null || classname.trim().length() <= 0) {
            throw new LRWSException(LRWSException.E_WRAPPER, "You have to inform a classname.");
        }

        String callres = null;

        try {
            callres = callWSAS(wshost, wsname, params, method, jobj);
        } catch (LRWSException e) {
            throw e;
        }
        if (callres != null && callres.trim().indexOf("[") >= 0) {
            try {
                JSONArray jsa = null;
                if (arrayparam != null && arrayparam.trim().length() > 0) {
                    JSONObject jso = new JSONObject(callres);
                    if (jso.has(arrayparam)) jsa = jso.getJSONArray(arrayparam);
                } else {
                    jsa = new JSONArray(callres);
                }
                if (jsa != null) {
                    Object uout = Array.newInstance(Class.forName(classname), jsa.length());
                    Object[] rout = (Object[]) uout;
                    return json2ObjectA(rout, jsa, true);
                }
                return null;
            } catch (Exception ex) {
                throw new LRWSException(LRWSException.E_CONVERSION, "Error parsing Data");
            }
        } else {
            throw new LRWSException(LRWSException.E_CALLERROR, "Web Service not executed");
        }
    }

    //##############################################################################################
    // CALL WEBSERVICE EXPECTING OBJECT RETURN
    //##############################################################################################

    public Object call(String classname, String wshost, String wsname, String[][] params, int method) throws LRWSException {
        return call(classname, wshost, wsname, params, method, null);
    }

    public Object call(String classname, String wshost, String wsname, String[][] params, int method, Object jobj) throws LRWSException {
        if (classname == null || classname.trim().length() <= 0) {
            throw new LRWSException(LRWSException.E_WRAPPER, "You have to inform a classname.");
        }
        String callres = null;
        try {
            callres = callWSAS(wshost, wsname, params, method, jobj);
        } catch (LRWSException e) {
            throw e;
        }
        if (callres != null) {
            if(callres.trim().length() <= 0)
                return null;
            try {
                Object ref = Class.forName(classname).newInstance();
                JSONObject jso = new JSONObject(callres);
                return json2Object(ref, jso, true);
            } catch (Exception ex) {
                throw new LRWSException(LRWSException.E_CONVERSION, "Error parsing Data");
            }
        } else {
            throw new LRWSException(LRWSException.E_CALLERROR, "Web Service not executed");
        }
    }

    //##############################################################################################
    // CALL WEBSERVICE EXPECTING STRING RETURN
    //##############################################################################################

    public String callSimple(String wshost, String wsname, String[][] params, int method) throws LRWSException {
        return  callSimple(wshost,  wsname, params, method, null);
    }

    public String callSimple(String wshost, String wsname, String[][] params, int method, Object jobj) throws LRWSException {
        return callWSAS(wshost, wsname, params, method,jobj);
    }

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

    // ####################################################################
    // Java Class(model) To JSON OBJECT CONVERTER
    // ####################################################################

    public JSONObject mod2json(Object o, boolean encoded) {
        return mod2json(o, encoded, false);
    }
    public JSONObject mod2json(Object o, boolean encoded, boolean ignorenull) {
        if (o == null)
            return null;
        JSONObject jout = new JSONObject();
        for (Field f : o.getClass().getDeclaredFields()) {
            try {
                if (f.get(o) != null && f.get(o).getClass().isArray()) {
                    JSONArray ao = mod2json((Object[]) f.get(o), encoded);
                    jout.put(f.getName(), ao);
                } else {
                    String val = (String) f.get(o);
                    if (val != null)
                        if (encoded)
                            jout.put(f.getName(), URLEncoder.encode((String) f.get(o), "UTF-8"));
                        else
                            jout.put(f.getName(), f.get(o));
                    else
                        if(!ignorenull)
                            jout.put(f.getName(), "");
                }
            } catch (Exception e) {

            }
        }
        return jout;
    }

    // ####################################################################
    // Java Class(model) To JSON ARRAY CONVERTER
    // ####################################################################

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

    // ####################################################################
    // COMMON LRWS EXCEPTION
    // ####################################################################

    public class LRWSException extends Exception {
        private String e_id = null;
        private String e_msg = null;

        public static final String E_PARAMS = "PAR";
        public static final String E_CONVERSION = "CONV";
        public static final String E_WRAPPER = "WRAP";
        public static final String E_CALLERROR = "CALL";

        public LRWSException(String errid, String emsg) {
            this.e_id = errid;
            this.e_msg = emsg;
        }

        public String getE_id() {
            return e_id;
        }

        public void setE_id(String e_id) {
            this.e_id = e_id;
        }

        public String getE_msg() {
            return e_msg;
        }

        public void setE_msg(String e_msg) {
            this.e_msg = e_msg;
        }


    }*/
}
