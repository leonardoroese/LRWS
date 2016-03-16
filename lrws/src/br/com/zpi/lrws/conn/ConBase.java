package br.com.zpi.lrws.conn;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.Statement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.*;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.codecs.MySQLCodec;
import java.net.*;
import java.io.*;

public abstract class ConBase {

	public String resType = null;
	public String resMsg = null;
	private ServletConfig scontext = null;

	public ConBase(ServletConfig sconf) {
		this.scontext = sconf;
	}

	// ####################################################################
	// INSERT DB (MYSQL)
	// ####################################################################
	public boolean updateDB(String query) {
		return updateDB(query, null);
	}

	public boolean updateDB(String query, String dbname) {
		Connection connection = null;
		ServletContext ctx = this.scontext.getServletContext();
		if (dbname == null)
			dbname = ctx.getInitParameter("dbName");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = (Connection) DriverManager.getConnection("jdbc:mysql://" + ctx.getInitParameter("dbHost") + ":"
					+ ctx.getInitParameter("dbPort") + "/" + dbname, ctx.getInitParameter("dbUser"),
					ctx.getInitParameter("dbPass"));
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
		ServletContext ctx = this.scontext.getServletContext();
		ResultSet rs = null;
		ArrayList<DBLin> outres = null;
		if (dbname == null)
			dbname = ctx.getInitParameter("dbName");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = (Connection) DriverManager.getConnection("jdbc:mysql://" + ctx.getInitParameter("dbHost") + ":"
					+ ctx.getInitParameter("dbPort") + "/" + dbname, ctx.getInitParameter("dbUser"),
					ctx.getInitParameter("dbPass"));
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
			this.resMsg = "Operação realizada";
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
	
}