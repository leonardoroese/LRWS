package br.com.zpi.lrws;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class LRWSHttpConn {

	
    // ####################################################################
    // GET CALL
    // ####################################################################
	
	public String doGET(String endpoint, String[][] params, boolean encoded) {
		CloseableHttpClient httpclient = null;
		String outp = null;
		
		if(endpoint == null || endpoint.trim().length() <= 0)
			return null;

		try {
			httpclient = HttpClients.createDefault();
			HttpResponse resposta = null;
			String epparams = "";

			if (params != null && params.length > 0) {
				epparams = "";
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				for (String[] s : params) {
					if (encoded)
						epparams = epparams + "&" + s[0] + "=" + URLEncoder.encode(s[1], "UTF-8");
					else
						epparams = epparams + "&" + s[0] + "=" + s[1];
				}
			}
			if (endpoint.indexOf("?") < 0)
				epparams = epparams.replaceFirst("&", "?");

			HttpGet wschama = new HttpGet(endpoint + epparams);
			resposta = httpclient.execute(wschama);
			outp = getResult(resposta);

		} catch (Exception e) {
			String msg = e.getMessage();
		} finally {
			if (httpclient != null)
				try {
					httpclient.close();
				} catch (Exception e) {

				}
		}

		return outp;
	}

    // ####################################################################
    // POST CALL
    // ####################################################################

	
	public String doPOST(String endpoint, String[][] params, JSONObject jso, boolean encoded) {
		CloseableHttpClient httpclient = null;
		String outp = null;
		
		if(endpoint == null || endpoint.trim().length() <= 0)
			return null;
		
		try {
			httpclient = HttpClients.createDefault();
			HttpResponse resposta = null;
			HttpPost wschama = new HttpPost(endpoint);

			if (params != null && params.length > 0) {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				for (String[] s : params) {
					if (encoded)
						nvps.add(new BasicNameValuePair(s[0], URLEncoder.encode(s[1], "UTF-8")));
					else
						nvps.add(new BasicNameValuePair(s[0], s[1]));
				}
				wschama.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			} else if (jso != null) {
                wschama.addHeader("Content-Type", "application/json");
                wschama.setEntity(new StringEntity(jso.toString(), "UTF-8"));
			}
			resposta = httpclient.execute(wschama);
			outp = getResult(resposta);
		} catch (Exception e) {
			String msg = e.getMessage();
		} finally {
			if (httpclient != null)
				try {
					httpclient.close();
				} catch (Exception e) {

				}
		}

		return outp;
	}

	
    // ####################################################################
    // PUT CALL
    // ####################################################################

	
	public String doPUT(String endpoint, String[][] params, JSONObject jso, boolean encoded) {
		CloseableHttpClient httpclient = null;
		String outp = null;
		
		if(endpoint == null || endpoint.trim().length() <= 0)
			return null;

		try {
			httpclient = HttpClients.createDefault();
			HttpResponse resposta = null;
			HttpPut wschama = new HttpPut(endpoint);

			if (params != null && params.length > 0) {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				for (String[] s : params) {
					if (encoded)
						nvps.add(new BasicNameValuePair(s[0], URLEncoder.encode(s[1], "UTF-8")));
					else
						nvps.add(new BasicNameValuePair(s[0], s[1]));
				}
				wschama.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			} else if (jso != null) {
                wschama.addHeader("Content-Type", "application/json");
                wschama.setEntity(new StringEntity(jso.toString(), "UTF-8"));
			}
			resposta = httpclient.execute(wschama);
			outp = getResult(resposta);
		} catch (Exception e) {
			String msg = e.getMessage();
		} finally {
			if (httpclient != null)
				try {
					httpclient.close();
				} catch (Exception e) {

				}
		}

		return outp;
	}

	
    // ####################################################################
    // DELETE CALL
    // ####################################################################
	
	public String doDELETE(String endpoint, boolean encoded) {
		CloseableHttpClient httpclient = null;
		String outp = null;
		
		if(endpoint == null || endpoint.trim().length() <= 0)
			return null;

		try {
			httpclient = HttpClients.createDefault();
			HttpResponse resposta = null;
			HttpDelete wschama = new HttpDelete(endpoint);
			resposta = httpclient.execute(wschama);
			outp = getResult(resposta);
		} catch (Exception e) {
			String msg = e.getMessage();
		} finally {
			if (httpclient != null)
				try {
					httpclient.close();
				} catch (Exception e) {

				}
		}

		return outp;
	}

	
    // ####################################################################
    // GET RESULT FROM CALLS
    // ####################################################################

	
	private String getResult(HttpResponse resposta) {

		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(resposta.getEntity().getContent()));
			StringBuffer sb = new StringBuffer("");
			String linha = "";
			while ((linha = br.readLine()) != null) {
				sb.append(linha);
			}

			br.close();
			linha = sb.toString();
			return linha;

		} catch (Exception e) {

		}
		return null;
	}

}
