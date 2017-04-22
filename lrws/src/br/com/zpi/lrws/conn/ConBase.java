package br.com.zpi.lrws.conn;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.jsoup.Jsoup;

public abstract class ConBase {

	public String resType = null;
	public String resMsg = null;
	private int DBD = 1;

	private ServletConfig scontext = null;

	private static final String MYSQLDRIVER = "com.mysql.jdbc.Driver";
	private static final String POSTGRESQLDRIVER = "org.postgresql.Driver";

	public static final int DBD_MYSQL = 1;
	public static final int DBD_POSTGRESQL = 2;

	public String lastval = null;
	public String lastval_par = null;
	
	public ConBase(ServletConfig sconf) {
		this.scontext = sconf;
		this.DBD = 1;
	}

	public ConBase(ServletConfig sconf, int DBD) {
		this.scontext = sconf;
		this.DBD = DBD;
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
		ServletContext ctx = this.scontext.getServletContext();
		lastval = null;
		boolean done = false;
		
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
					query = query.replaceFirst("\\?", s);
				}
			}
		}

		if (dbname == null)
			dbname = ctx.getInitParameter("dbName");
		try {
			Statement stmt = null;
			switch (DBD) {
			case 1:
				Class.forName(MYSQLDRIVER);
				connection = (Connection) DriverManager.getConnection("jdbc:mysql://" + ctx.getInitParameter("dbHost")
						+ ":" + ctx.getInitParameter("dbPort") + "/" + dbname, ctx.getInitParameter("dbUser"),
						ctx.getInitParameter("dbPass"));
				stmt = connection.createStatement();
				stmt.execute(query);
				break;

			case 2:
				Class.forName(POSTGRESQLDRIVER);
				connection = (Connection) DriverManager
						.getConnection(
								"jdbc:postgresql://" + ctx.getInitParameter("dbHost") + ":"
										+ ctx.getInitParameter("dbPort") + "/" + dbname,
								ctx.getInitParameter("dbUser"), ctx.getInitParameter("dbPass"));
				connection.setAutoCommit(true);
				stmt = connection.createStatement();
				try{
					stmt.executeQuery(query);
					done = true;
				}catch(SQLException e){
					
				}
				break;

			default:
				Class.forName(MYSQLDRIVER);
				connection = (Connection) DriverManager.getConnection("jdbc:mysql://" + ctx.getInitParameter("dbHost")
						+ ":" + ctx.getInitParameter("dbPort") + "/" + dbname, ctx.getInitParameter("dbUser"),
						ctx.getInitParameter("dbPass"));
				stmt = connection.createStatement();
				done = stmt.execute(query);
				break;

			}


			
			ResultSet rs = stmt.getResultSet();
			if (rs != null){
				while(rs.next()){
					if(lastval_par != null){
						if(rs.getString(lastval_par) != null)
							lastval = rs.getString(lastval_par);
					}else{
						if(rs.getString("id") != null)
							lastval = rs.getString("id");
					}
				}
				done = true;
			}
			
			if(stmt.getUpdateCount() > 0)
				done  = true;
			
			
			connection.close();

			if (done) {
				this.resType = "S";
				this.resMsg = "Success";
				return true;
			} else {
				this.resType = "E";
				this.resMsg = "Not executed";
				return false;
			}

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
		return readDb(query, null, null, false);
	}

	public ArrayList<DBLin> readDb(String query, String[] params, boolean encode) {
		return readDb(query, null, params, encode);
	}

	public ArrayList<DBLin> readDb(String query, String dbname, String[] params, boolean encode) {
		Connection connection = null;
		ServletContext ctx = this.scontext.getServletContext();
		ResultSet rs = null;
		ArrayList<DBLin> outres = null;

		if (query == null || query.trim().length() <= 0) {
			this.resType = "E";
			this.resMsg = "Inform valid query";
			return null;
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
					query = query.replaceFirst("\\?", s);
				}
			}
		}

		if (dbname == null)
			dbname = ctx.getInitParameter("dbName");
		try {

			switch (DBD) {
			case 1:
				Class.forName(MYSQLDRIVER);
				connection = (Connection) DriverManager.getConnection("jdbc:mysql://" + ctx.getInitParameter("dbHost")
						+ ":" + ctx.getInitParameter("dbPort") + "/" + dbname, ctx.getInitParameter("dbUser"),
						ctx.getInitParameter("dbPass"));
				break;

			case 2:
				Class.forName(POSTGRESQLDRIVER);
				connection = (Connection) DriverManager
						.getConnection(
								"jdbc:postgresql://" + ctx.getInitParameter("dbHost") + ":"
										+ ctx.getInitParameter("dbPort") + "/" + dbname,
								ctx.getInitParameter("dbUser"), ctx.getInitParameter("dbPass"));
				break;

			default:
				Class.forName(MYSQLDRIVER);
				connection = (Connection) DriverManager.getConnection("jdbc:mysql://" + ctx.getInitParameter("dbHost")
						+ ":" + ctx.getInitParameter("dbPort") + "/" + dbname, ctx.getInitParameter("dbUser"),
						ctx.getInitParameter("dbPass"));
				break;

			}

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
