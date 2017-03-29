package br.com.zpi.lrws;

import org.json.JSONObject;

public class LRWSConnector {

    public final static int METHOD_GET = 1;
    public final static int METHOD_POST = 2;
    public final static int METHOD_PUT = 3;
    public final static int METHOD_DELETE = 4;
    
    public String callWSAS(String host, String endpoint, String[][] params, JSONObject jso, boolean encoded, int method) throws LRWSException{
    	
    	switch(method){
    	case METHOD_GET:
    		return new LRWSHttpConn().doGET(host+endpoint,params,encoded);
    	case METHOD_POST:
    		return new LRWSHttpConn().doPOST(host+endpoint,params,jso,encoded);
    	case METHOD_PUT:
    		return new LRWSHttpConn().doPUT(host+endpoint,params,jso,encoded);
    	case METHOD_DELETE:
    		return new LRWSHttpConn().doDELETE(host+endpoint,encoded);
    	}
        return null;
    }

}
