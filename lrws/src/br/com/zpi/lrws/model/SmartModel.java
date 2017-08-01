package br.com.zpi.lrws.model;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.servlet.ServletConfig;
import br.com.zpi.lrws.LRWSException;
import br.com.zpi.lrws.linParams;
import br.com.zpi.lrws.conn.ConBase;
import br.com.zpi.lrws.conn.DBLin;
import br.com.zpi.lrws.conn.DBParVal;

public class SmartModel extends ConBase implements Serializable {
	private static final long serialVersionUID = 1L;
	private String targetDB = null;
	private ServletConfig sconf = null;
	private int DBD = 0;
	/*
	 * #########################################################################
	 * ##### CONSTRUCTOR
	 * #########################################################################
	 * #####
	 */

	public SmartModel(ServletConfig sconf, int DBD, String targetDB) {
		super(sconf, DBD);
		this.targetDB = targetDB;
		this.sconf = sconf;
		this.DBD = DBD;
	}

	/*
	 * #########################################################################
	 * ##### LIST
	 * #########################################################################
	 * #####
	 */

	public Object[] listOfMe(linParams[] keys, linParams[] ord) throws LRWSException {

		String query = "SELECT * FROM " + targetDB + " ";

		// ++++++++++++++++++++++++++++++++++++++
		// PREPARE QUERY
		// ++++++++++++++++++++++++++++++++++++++
		if (keys != null && keys.length > 0) {
			for (Field f : this.getClass().getDeclaredFields()) {
				if (f.getName() != "serialVersionUID" && f.getName() != "targetDB" && f.getName() != "sconf"
						&& f.getName() != "DBD") {
					boolean qlink = false;
					for (linParams lin : keys) {
						if (lin.name != null
								&& lin.name.trim().toUpperCase().equals(f.getName().trim().toUpperCase())) {
							if (lin.value != null && lin.value.trim().length() > 0) {
								if (qlink)
									query = query + " AND ";
								String val = null;
								try {
									val = (String) f.get(this);
								} catch (Exception e) {
									throw new LRWSException("E", "LRWS", "lrws.e.modexkeyvalue");
								}
								if (lin.value.trim().toUpperCase().equals("S")) {
									query = query + " " + f.getName() + " = '" + val + "' ";
								} else if (lin.value.trim().toUpperCase().equals("I")) {
									query = query + " " + f.getName() + " = " + val + " ";
								} else {
									throw new LRWSException("E", "LRWS", "lrws.e.modinvalidkeytype");
								}
								qlink = true;
							} else {
								throw new LRWSException("E", "LRWS", "lrws.e.modinvalidkey");
							}
							break;
						}
					}
				}
			}
		}

		if (ord != null && ord.length > 0) {
			query = query + " ORDER BY ";
			boolean ordq = false;

			for (linParams lin : ord) {
				if (ordq)
					query = query + ", ";
				query = query + lin.name + " " + lin.value;
				ordq = true;
			}

		}

		// ++++++++++++++++++++++++++++++++++++++
		// EXECUTE QUERY
		// ++++++++++++++++++++++++++++++++++++++
		ArrayList<DBLin> al = readDb(query);

		// ++++++++++++++++++++++++++++++++++++++
		// UPDATE OBJECT
		// ++++++++++++++++++++++++++++++++++++++
		if (al != null && al.size() > 0) {
			Object[] out = (Object[]) Array.newInstance(this.getClass(), al.size());
			int c = 0;
			for (DBLin lin : al) {
				try {
					Class<?> clazz = Class.forName(this.getClass().getName());
					Constructor<?> ctor = clazz.getConstructor(ServletConfig.class, int.class, String.class);
					out[c] = ctor.newInstance(new Object[] { sconf, DBD, targetDB });
					for (DBParVal p : lin.cols) {
						try {
							if (p.value != null)
								out[c].getClass().getDeclaredField(p.param).set(out[c], p.value);
						} catch (Exception e) {

						}
					}
				} catch (Exception e) {
					throw new LRWSException("E", "LRWS_NORES", "lrws.e.moderrcreateinst");
				}
				c++;
			}
			return out;
		} else {
			throw new LRWSException("E", "LRWS_NORES", "lrws.e.modnotfound");
		}
	}

	/*
	 * #########################################################################
	 * ##### GET
	 * #########################################################################
	 * #####
	 */

	public void getMe(linParams[] keys) throws LRWSException {

		if (keys == null || keys.length <= 0)
			throw new LRWSException("E", "LRWS", "lrws.e.modinformkeys");

		String query = "SELECT * FROM " + targetDB + " WHERE ";

		// ++++++++++++++++++++++++++++++++++++++
		// PREPARE QUERY
		// ++++++++++++++++++++++++++++++++++++++
		for (Field f : this.getClass().getDeclaredFields()) {
			if (f.getName() != "serialVersionUID" && f.getName() != "targetDB" && f.getName() != "sconf"
					&& f.getName() != "DBD") {
				boolean qlink = false;
				for (linParams lin : keys) {
					if (lin.name != null && lin.name.trim().toUpperCase().equals(f.getName().trim().toUpperCase())) {
						if (lin.value != null && lin.value.trim().length() > 0) {
							if (qlink)
								query = query + " AND ";
							String val = null;
							try {
								val = (String) f.get(this);
							} catch (Exception e) {
								throw new LRWSException("E", "LRWS", "lrws.e.modexkeyvalue");
							}
							if (lin.value.trim().toUpperCase().equals("S")) {
								query = query + " " + f.getName() + " = '" + val + "' ";
							} else if (lin.value.trim().toUpperCase().equals("I")) {
								query = query + " " + f.getName() + " = " + val + " ";
							} else {
								throw new LRWSException("E", "LRWS", "lrws.e.modinvalidkeytype");
							}
							qlink = true;
						} else {
							throw new LRWSException("E", "LRWS", "lrws.e.modinvalidkey");
						}
						break;
					}
				}
			}
		}
		// ++++++++++++++++++++++++++++++++++++++
		// EXECUTE QUERY
		// ++++++++++++++++++++++++++++++++++++++
		ArrayList<DBLin> al = readDb(query);

		// ++++++++++++++++++++++++++++++++++++++
		// UPDATE OBJECT
		// ++++++++++++++++++++++++++++++++++++++
		if (al != null && al.size() == 1) {
			boolean upderr = false;
			for (DBLin lin : al) {
				for (DBParVal p : lin.cols) {
					try {
						if (p.value != null)
							this.getClass().getDeclaredField(p.param).set(this, p.value);
					} catch (Exception e) {
						upderr = true;
					}
				}
			}
			if (upderr)
				throw new LRWSException("E", "LRWS_NORES", "lrws.e.modmissingvalues");

		} else {
			throw new LRWSException("E", "LRWS_NORES", "lrws.e.modnotfound");
		}

	}

	/*
	 * #########################################################################
	 * ##### CREATE
	 * #########################################################################
	 * #####
	 */

	public String createMe(String ai) throws LRWSException {

		String query = "INSERT " + targetDB + " ";
		String qfields = "";
		String qvalues = "";
		boolean qset = false;
		for (Field f : this.getClass().getDeclaredFields()) {
			if (f.getName() != "serialVersionUID" && f.getName() != "targetDB" && f.getName() != "sconf"
					&& f.getName() != "DBD") {
				if (ai == null || ai.trim().toUpperCase().equals(f.getName().toUpperCase())) {
					if (qset){
						qfields = qfields + ", ";
						qvalues = qvalues + ", ";
					}
						
					qfields = qfields + f.getName();
					
					String fval = null;
					try {
						fval = (String) f.get(this);
					} catch (Exception e) {
						throw new LRWSException("E", "LRWS", "lrws.e.modexkeyvalue");
					}
					
					qvalues = qvalues + fval;
				}
			}
		}
		query = query + "(" + qfields + ") VALUES (" + qvalues + ")";

		// ++++++++++++++++++++++++++++++++++++++
		// EXECUTE QUERY
		// ++++++++++++++++++++++++++++++++++++++
		if(ai != null)
			lastval_par = ai;
		
		boolean res = updateDB(query);

		// ++++++++++++++++++++++++++++++++++++++
		// UPDATE OBJECT
		// ++++++++++++++++++++++++++++++++++++++
		if (res) {
			if(ai != null && lastval != null)
				return lastval;
			else
				throw new LRWSException("E", "LRWS", "lrws.e.modnotcreated");
			
		}else
			throw new LRWSException("E", "LRWS", "lrws.e.modnotcreated");

	}

	/*
	 * #########################################################################
	 * ##### SAVE
	 * #########################################################################
	 * #####
	 */

	public void saveMe(linParams[] keys) throws LRWSException {
		if (keys == null || keys.length <= 0)
			throw new LRWSException("E", "LRWS", "lrws.e.informkeys");

		String query = "UPDATE " + targetDB + " SET ";
		String condition = " WHERE ";
		boolean qset = false;

		for (Field f : this.getClass().getDeclaredFields()) {
			if (f.getName() != "serialVersionUID" && f.getName() != "targetDB" && f.getName() != "sconf"
					&& f.getName() != "DBD") {
				String fval = null;
				try {
					fval = (String) f.get(this);
				} catch (Exception e) {
					throw new LRWSException("E", "LRWS", "lrws.e.modexkeyvalue");
				}
				if (qset)
					query = query + ", ";
				qset = true;

				query = query + f.getName() + " = " + fval;

				boolean qlink = false;
				for (linParams lin : keys) {
					if (lin.value != null && lin.value.trim().length() > 0) {
						if (qlink)
							condition = condition + " AND ";
						String val = null;
						try {
							val = (String) f.get(this);
						} catch (Exception e) {
							throw new LRWSException("E", "LRWS", "lrws.e.modexkeyvalue");
						}
						if (lin.value.trim().toUpperCase().equals("S")) {
							condition = condition + " " + f.getName() + " = '" + val + "' ";
						} else if (lin.value.trim().toUpperCase().equals("I")) {
							condition = condition + " " + f.getName() + " = " + val + " ";
						} else {
							throw new LRWSException("E", "LRWS", "lrws.e.modinvalidkeytype");
						}
						qlink = true;
					} else {
						throw new LRWSException("E", "LRWS", "lrws.e.modinvalidkey");
					}
				}
			}
		}

		// ++++++++++++++++++++++++++++++++++++++
		// EXECUTE QUERY
		// ++++++++++++++++++++++++++++++++++++++
		boolean res = updateDB(query + condition);

		// ++++++++++++++++++++++++++++++++++++++
		// UPDATE OBJECT
		// ++++++++++++++++++++++++++++++++++++++
		if (!res) {
			throw new LRWSException("E", "LRWS", "lrws.e.modnotupdated");
		}

	}

	/*
	 * #########################################################################
	 * ##### DELETE
	 * #########################################################################
	 * #####
	 */

	public boolean deleteMe(linParams[] keys) throws LRWSException {
		if (keys == null || keys.length <= 0)
			throw new LRWSException("E", "LRWS", "lrws.e.informkeys");

		String query = "DELETE FROM " + targetDB + " WHERE ";

		// ++++++++++++++++++++++++++++++++++++++
		// PREPARE QUERY
		// ++++++++++++++++++++++++++++++++++++++
		for (Field f : this.getClass().getDeclaredFields()) {
			if (f.getName() != "serialVersionUID" && f.getName() != "targetDB" && f.getName() != "sconf"
					&& f.getName() != "DBD") {
				boolean qlink = false;
				for (linParams lin : keys) {
					if (lin.name != null && lin.name.trim().toUpperCase().equals(f.getName().trim().toUpperCase())) {
						if (lin.value != null && lin.value.trim().length() > 0) {
							if (qlink)
								query = query + " AND ";
							String val = null;
							try {
								val = (String) f.get(this);
							} catch (Exception e) {
								throw new LRWSException("E", "LRWS", "lrws.e.modexkeyvalue");
							}
							if (lin.value.trim().toUpperCase().equals("S")) {
								query = query + " " + f.getName() + " = '" + val + "' ";
							} else if (lin.value.trim().toUpperCase().equals("I")) {
								query = query + " " + f.getName() + " = " + val + " ";
							} else {
								throw new LRWSException("E", "LRWS", "lrws.e.modinvalidkeytype");
							}
							qlink = true;
						} else {
							throw new LRWSException("E", "LRWS", "lrws.e.modinvalidkey");
						}
						break;
					}
				}
			}
		}
		// ++++++++++++++++++++++++++++++++++++++
		// EXECUTE QUERY
		// ++++++++++++++++++++++++++++++++++++++
		ArrayList<DBLin> al = readDb(query);

		// ++++++++++++++++++++++++++++++++++++++
		// UPDATE OBJECT
		// ++++++++++++++++++++++++++++++++++++++
		if (al != null && al.size() == 1) {
			return true;
		} else {
			return false;
		}
	}

}
