package br.com.zpi.lrws.conn;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.Statement;

public abstract class ConBaseStd {

	public String resType = null;
	public String resMsg = null;
	private String dbHost = null;
	private String dbPort = null;
	private String dbName = null;
	private String dbUser = null;
	private String dbPass = null;
	
	
	public ConBaseStd(String configurationsfilepath) {
		Configurations conf = loadConfig(configurationsfilepath);
		if(conf != null){
			dbHost = conf.dbHost;
			dbPort = conf.dbPort;
			dbName = conf.dbName;
			dbUser = conf.dbUser;
			dbPass = conf.dbPass;
		}

	}

	// ####################################################################
	// INSERT DB (MYSQL)
	// ####################################################################
	public boolean updateDB(String query) {
		return updateDB(query, null);
	}

	public boolean updateDB(String query, String dbname) {
		return updateDB(query, dbname, null, false);
	}

	public boolean updateDB(String query, String[] params, boolean encode) {
		return updateDB(query, null, params, encode);
	}

	public boolean updateDB(String query, String dbname, String[] params, boolean encode) {
		Connection connection = null;
		
		if (query == null || query.trim().length() <= 0) {
			this.resType = "E";
			this.resMsg = "Inform valid query";
			return false;
		}
		if (params != null && params.length > 0) {
			if (query.indexOf('?') > 0) {
				for (String s : params) {
					if (encode) {
						try {
							s = URLEncoder.encode(s, "UTF-8");
						} catch (Exception e) {

						}
					}
					query = query.replace("?", s);
				}
			}
		}

		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = (Connection) DriverManager.getConnection("jdbc:mysql://" +dbHost + ":"
					+ dbPort + "/" + dbName, dbUser,dbPass);
			Statement stmt = (Statement) connection.createStatement();
			stmt.execute(query);
			connection.close();

			this.resType = "S";
			this.resMsg = "Operação realizada";
			return true;

		} catch (Exception e) {
			this.resType = "E";
			this.resMsg = e.getMessage();
			return false;
		}
	}

	// ####################################################################
	// READ DB (MYSQL)
	// ####################################################################
	public ArrayList<DBLin> readDb(String query) {
		return readDb(query, null);
	}

	public ArrayList<DBLin> readDb(String query, String dbname) {
		Connection connection = null;
		ResultSet rs = null;
		ArrayList<DBLin> outres = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = (Connection) DriverManager.getConnection("jdbc:mysql://" +dbHost + ":"
					+ dbPort + "/" + dbName, dbUser,dbPass);
			Statement stmt = (Statement) connection.createStatement();
			rs = stmt.executeQuery(query);
			if (rs != null) {
				ResultSetMetaData metaData = (ResultSetMetaData) rs.getMetaData();
				while (rs.next()) {
					if (outres == null)
						outres = new ArrayList<DBLin>();
					DBLin lin = new DBLin();
					DBParVal[] par = new DBParVal[metaData.getColumnCount()];
					for (int i = 0; i < metaData.getColumnCount(); i++) {
						par[i] = new DBParVal();
						par[i].param = metaData.getColumnLabel(i + 1);
						switch (metaData.getColumnTypeName(i + 1).toUpperCase()) {
						case "VARCHAR":
							par[i].value = rs.getString(i + 1);
							break;
						case "DATE":
							if (rs.getDate(i + 1) != null)
								par[i].value = rs.getDate(i + 1).toString();
							break;
						case "DATETIME":
							par[i].value = "";
							try {
								java.sql.Timestamp timestamp = rs.getTimestamp(i + 1);
								if (timestamp != null)
									par[i].value = timestamp.toString();
							} catch (Exception ex2) {
								InputStream bin = rs.getBinaryStream(i + 1);
								if (bin != null)
									par[i].value = bin.toString();
							}

							break;
						case "INT":
							par[i].value = Integer.valueOf(rs.getInt(i + 1)).toString();
							break;
						case "BIGINT":
							par[i].value = Integer.valueOf(rs.getInt(i + 1)).toString();
							break;
						default:
							par[i].value = rs.getString(i + 1);
							break;
						}

					}
					lin.cols = par;
					outres.add(lin);
				}
			}
			connection.close();
			this.resType = "S";
			this.resMsg = "Operacao realizada";
			return outres;
		} catch (Exception e) {
			this.resType = "E";
			this.resMsg = e.getMessage();
			return null;
		}
	}

	// ####################################################################
	// E-MAIL VALIDATION
	// ####################################################################
	public boolean validEmail(String email) {
		if (email == null || email.length() <= 0)
			return false;
		String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
		return email.matches(EMAIL_REGEX);
	}

	// ####################################################################
	// FORMAT DATE MYSQL>>FORMAT
	// ####################################################################
	public String dateMYSQL2FORMAT(String dt) {
		String res = "";
		if (dt == null || dt.length() < 10)
			return null;
		res = dt.substring(8, 10) + "/" + dt.substring(5, 7) + "/" + dt.substring(0, 4);
		return res;
	}

	// ####################################################################
	// FORMAT DATE FORMAT>>MYSQL
	// ####################################################################
	public String dateFORMAT2MYSQL(String dt) {
		String res = "";
		if (dt == null || dt.length() < 10)
			return null;
		res = dt.substring(6, 10) + "-" + dt.substring(3, 5) + "-" + dt.substring(0, 2);
		return res;
	}

	// ####################################################################
	// CRIPTO MD5
	// ####################################################################
	public String toMD5(String val) {
		MessageDigest m;
		String outmd5 = null;
		if (val == null) {
			this.resType = "E";
			this.resMsg = "Ocorreu um problema com criptografia.";
			return null;
		}
		try {
			m = MessageDigest.getInstance("MD5");
			m.update(val.getBytes(), 0, val.length());
			outmd5 = new BigInteger(1, m.digest()).toString(16);
			return outmd5;
		} catch (Exception e) {
			this.resType = "E";
			this.resMsg = "Ocorreu um problema com criptografia.";
			return null;
		}
	}

	// ####################################################################
	// SQL VALIDATE
	// ####################################################################
	public String sqlValidate(String sqlquery, boolean nohtml) {
		String outs = "";

		if (nohtml)
			outs = Jsoup.parse(sqlquery).text();
		/*
		 * try{ outs = ESAPI.encoder().encodeForSQL(new
		 * MySQLCodec(MySQLCodec.Mode.STANDARD), sqlquery); }catch(Exception
		 * ex){ return null; }
		 */
		return outs;
	}

	// ####################################################################
	// HTTP-GET
	// ####################################################################
	public String httpget(String url) {
		URL dest;
		try {
			dest = new URL(url);
			URLConnection yc = dest.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
			String inputLine;
			String resp = "";
			while ((inputLine = in.readLine()) != null)
				resp = resp + inputLine;
			in.close();
			return resp;

		} catch (Exception e) {
			return null;
		}
	}

	// ####################################################################
	// Object Lin to JSON
	// ####################################################################
	public JSONObject lin2json(Object o, boolean encoded) {
		if (o == null)
			return null;
		JSONObject jout = new JSONObject();
		for (Field f : o.getClass().getDeclaredFields()) {
			try {
				if (f.get(o) != null && f.get(o).getClass().isArray()) {
					JSONArray ao = lin2json((Object[]) f.get(o), true);
					jout.put(f.getName(), ao);
				} else {
					String val = (String) f.get(o);
					if (val != null)
						if (encoded)
							jout.put(f.getName(), URLEncoder.encode((String) f.get(o), "UTF-8"));
						else
							jout.put(f.getName(), (String) f.get(o));
					else
						jout.put(f.getName(), "");
				}
			} catch (Exception e) {
				jout.put(f.getName(), "");
			}
		}
		return jout;
	}

	// ####################################################################
	// Object Lin to JSON Array
	// ####################################################################
	public JSONArray lin2json(Object[] ao, boolean encoded) {
		if (ao == null)
			return null;
		JSONArray jout = new JSONArray();
		for (Object o : ao) {
			JSONObject jso = new JSONObject();
			for (Field f : o.getClass().getDeclaredFields()) {
				try {
					if (f.get(o) != null && f.get(o).getClass().isArray()) {
						JSONArray aao = lin2json((Object[]) f.get(o), true);
						jso.put(f.getName(), aao);
					} else {
						String val = (String) f.get(o);
						if (val != null)
							if (encoded)
								jso.put(f.getName(), URLEncoder.encode((String) f.get(o), "UTF-8"));
							else
								jso.put(f.getName(), (String) f.get(o));
						else
							jso.put(f.getName(), "");
					}
				} catch (Exception e) {
					jso.put(f.getName(), "");
				}
			}
			jout.put(jso);
		}
		return jout;
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
                    String clnameredux = clname.substring(2, clname.length() - 1);
                    Object[] ao = (Object[]) Array.newInstance(Class.forName(clnameredux),((JSONArray)jso.get(key)).length());
                    int cnob = 0;
                    for(Object xo : ao){
                        ao[cnob] = Class.forName(clnameredux).newInstance();
                        cnob++;
                    }
                    ao = json2ObjectA(ao, (JSONArray) jso.get(key), decode);
                    if(ao != null){
                        o.getClass().getDeclaredField(key).set(o, ao);
                    }

                }else{
                    if (o.getClass().getDeclaredField(key) != null) {
                        if(decode)
                            o.getClass().getDeclaredField(key).set(o, URLDecoder.decode((String) jso.get(key), "UTF-8"));
                        else
                            o.getClass().getDeclaredField(key).set(o, (String) jso.get(key));
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
	// LOAD CONFIGURATIONS
	// ####################################################################
	public Configurations loadConfig(String pathfile) {
		if (pathfile == null || pathfile.trim().length() <= 0) {
			System.out.println("Inform the configuration file path");
			return null;
		}
		Path proppath = Paths.get(pathfile, "lrws.properties");
		Configurations conf = new Configurations();
		Charset charset = Charset.forName("UTF-8");
		try {
			List<String> confLines = Files.readAllLines(proppath, charset);
			for (String s : confLines) {
				if (s.indexOf("=") > 0) {
					String[] mtzProp = s.split("=");
					if (mtzProp != null && mtzProp.length > 1) {
						if (mtzProp[0].trim().equals("dbHost"))
							conf.dbHost = mtzProp[1].trim();
						if (mtzProp[0].trim().equals("dbPort"))
							conf.dbPort = mtzProp[1].trim();
						if (mtzProp[0].trim().equals("dbName"))
							conf.dbName = mtzProp[1].trim();
						if (mtzProp[0].trim().equals("dbUser"))
							conf.dbUser = mtzProp[1].trim();
						if (mtzProp[0].trim().equals("dbPass"))
							conf.dbPass = mtzProp[1].trim();
					}
				}
			}
			return conf;
		} catch (Exception ex) {
			System.out.println("Error reading properties file: " + ex.getMessage());
			return null;
		}
	}
	
}
