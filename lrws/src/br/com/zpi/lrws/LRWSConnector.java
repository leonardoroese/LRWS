package br.com.zpi.lrws;
import java.net.URLEncoder;
import java.util.ArrayList;

public class LRWSConnector {

    public final static int METHOD_GET = 1;
    public final static int METHOD_POST = 2;

    public String callWSAS(String host, String name, ArrayList<linParams> params, int method) throws LRWSException{
        String[] sendpar = null;
        LRWSException e = new LRWSException();

        if (host == null || host.trim().length() <= 0) {
            e.setMessage("Forgot to inform host endpoint?");
            throw e;
        }

        if (name == null || name.trim().length() <= 0) {
            e.setMessage("Forgot to inform endpoint name?");
            throw e;
        }

        if (method != METHOD_GET && method != METHOD_POST) {
            e.setMessage("Invalid Method");
            throw e;
        }
        try {

            // Fill url parameters
            if(method == METHOD_GET){
                sendpar = new String[1];
                sendpar[0] = "g"+host + "/" + name;
                if (params != null && params.size() > 0) {
                    int cntp = 0;
                    for (linParams l : params) {
                        if(cntp == 0)
                            sendpar[0] = sendpar[0] + "?";
                                    else
                            sendpar[0] = sendpar[0] + "&";
                        sendpar[0] = sendpar[0] + "&" +  URLEncoder.encode(l.name, "UTF-8") + "=" + URLEncoder.encode(l.value, "UTF-8");
                        cntp++;
                    }
                }
            }else {
                if (params == null || params.size() <= 0)
                    sendpar = new String[1];
                    else
                    sendpar = new String[(params.size() * 2) + 1];
                sendpar[0] = "p"+host + "/" + name;
                if (params != null && params.size() > 0){
                    int cntpar = 0;
                    for (linParams l : params) {
                        cntpar++;
                        sendpar[cntpar] = URLEncoder.encode(l.name, "UTF-8");
                        cntpar++;
                        sendpar[cntpar] = URLEncoder.encode(l.value, "UTF-8");
                    }
                }
            }
            // Call WEBSERVICE
            String hres = null;
            hres = new LRWSHttpConn().doGET(sendpar);
            if (hres != null && hres.trim().length() > 0) {
                return hres;
            }
        } catch (Exception ex) {
            e.setMessage(ex.getMessage());
            throw e;
        }
        return null;
    }

}
