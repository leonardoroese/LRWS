package br.com.zpi.lrws;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by leonardo on 19/12/15.
 */
public class LRWSHttpConn {
    public String doGET(String... params){
        ArrayList<NameValuePair> par = new ArrayList<NameValuePair>();
        if(params == null || params.length <= 0){
            return null;
        }
        if(params.length <= 0 || (params.length > 1 && (params.length % 2) == 0)){
            return null;
        }else if(params.length > 2){
            for (int i = 1; i < params.length; i++) {
                par.add(new BasicNameValuePair(params[i], params[i+1]));
                i++;
            }
        }
        try {
            HttpClient client = new DefaultHttpClient();
            HttpResponse resposta = null;
            if(params[0].substring(0,1).equals("g")){
                HttpGet wschama = new HttpGet(params[0].substring(1));
                resposta = client.execute(wschama);

            }else if(params[0].substring(0,1).equals("p")){
                HttpPost wschama = new HttpPost(params[0].substring(1));
                wschama.setEntity(new UrlEncodedFormEntity(par,"UTF-8"));
                resposta = client.execute(wschama);
            }else{
                HttpPost wschama = new HttpPost(params[0]);
                wschama.setEntity(new UrlEncodedFormEntity(par, "UTF-8"));
                resposta = client.execute(wschama);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    resposta.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String linha = "";
            while ((linha = br.readLine()) != null) {
                sb.append(linha);
            }

            br.close();
            linha = sb.toString();
            return linha;

        } catch (Exception e)  {
            String msg = e.getMessage();
        }

        return null;
    }

}
