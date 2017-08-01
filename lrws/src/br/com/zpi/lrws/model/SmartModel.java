package br.com.zpi.lrws.model;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
	private linParams[] metainfodb = null; // S -String, E - Encoded String, X -
											// not used

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
	 * ##### GETTERS SETTERS
	 * #########################################################################
	 * #####
	 */

	public linParams[] getMetainfodb() {
		return metainfodb;
	}

	public void setMetainfodb(linParams[] metainfodb) {
		this.metainfodb = metainfodb;
	}

	/*
	 * #########################################################################
	 * ##### LIST
	 * #########################################################################
	 * #####
	 * keys = obligatory composed by linParam.name (name of key) linParam.value(value of key)
	 */

	public Object[] listOfMe(linParams[] keys, linParams[] ord, int page, int limit) throws LRWSException {

		String query = "SELECT * FROM " + targetDB + " ";

		// ++++++++++++++++++++++++++++++++++++++
		// PREPARE QUERY
		// ++++++++++++++++++++++++++++++++++++++
		if (keys != null && keys.length > 0) {
			for (Field f : this.getClass().getDeclaredFields()) {
				if (!checkFieldExcluded(f.getName())) {
					boolean qlink = false;
					for (linParams lin : keys) {
						String mityp = getMetaInfoType(f.getName());
						if (lin.name != null
								&& lin.name.trim().toUpperCase().equals(f.getName().trim().toUpperCase())) {
							if (lin.value != null && lin.value.trim().length() > 0) {
								if (qlink)
									query = query + " AND ";
								if (mityp != null) {
									switch (mityp) {
									case "S":
										if (lin.value != null)
											query = query + " " + f.getName() + " = '" + lin.value.trim() + "' ";
										else
											query = query + " " + f.getName() + " = NULL ";
										break;
									case "E":
										if (lin.value != null)
											try {
												query = query + " " + f.getName() + " = '"
														+ URLDecoder.decode(lin.value.trim(), "UTF-8") + "' ";
											} catch (Exception e) {
												throw new LRWSException("E", "LRWS", "lrws.e.modencodingpar");
											}
										else
											query = query + " " + f.getName() + " = NULL ";
										break;
									default:
										query = query + " " + f.getName() + " = " + lin.value.trim() + " ";
										break;
									}
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

		if (limit > 0) {
			switch (DBD) {
			case DBD_POSTGRESQL:
				query = query + " LIMIT " + limit + " OFFSET " + (limit * page);
				break;
			case DBD_MYSQL:
				query = query + " LIMIT " + page + " , " + limit;
				break;
			default:
				query = query + " LIMIT " + page + ", " + limit;
				break;
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
							if (p.value != null){
								String mityp = getMetaInfoType(p.param);
								if(mityp != null && mityp.trim().equals("E") && p.value != null){
									out[c].getClass().getDeclaredField(p.param).set(out[c], URLDecoder.decode(p.value,"UTF-8"));
								}else{
									out[c].getClass().getDeclaredField(p.param).set(out[c], p.value);
								}
							}
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
			String mityp = getMetaInfoType(f.getName());
			if (!checkFieldExcluded(f.getName())) {
				boolean qlink = false;
				for (linParams lin : keys) {
					if (lin.name != null && lin.name.trim().toUpperCase().equals(f.getName().trim().toUpperCase())) {
						if (lin.value != null && lin.value.trim().length() > 0) {
							if (qlink)
								query = query + " AND ";
							if (mityp != null) {
								switch (mityp) {
								case "S":
									if (lin.value != null)
										query = query + " " + f.getName() + " = '" + lin.value.trim() + "' ";
									else
										query = query + " " + f.getName() + " = NULL ";
									break;
								case "E":
									if (lin.value != null)
										try {
											query = query + " " + f.getName() + " = '"
													+ URLDecoder.decode(lin.value.trim(), "UTF-8") + "' ";
										} catch (Exception e) {
											throw new LRWSException("E", "LRWS", "lrws.e.modencodingpar");
										}
									else
										query = query + " " + f.getName() + " = NULL ";
									break;
								default:
									query = query + " " + f.getName() + " = " + lin.value.trim() + " ";
									break;
								}
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
						if (p.value != null){
							String mityp = getMetaInfoType(p.param);
							if(mityp != null && mityp.trim().equals("E") && p.value != null){
								this.getClass().getDeclaredField(p.param).set(this, URLDecoder.decode(p.value,"UTF-8"));
							}else{
								this.getClass().getDeclaredField(p.param).set(this, p.value);
							}
						}
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
			if (!checkFieldExcluded(f.getName())) {
				if (ai == null || ai.trim().toUpperCase().equals(f.getName().toUpperCase())) {
					if (qset) {
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
					if (metainfodb != null && metainfodb.length > 0) {
						boolean metafound = false;
						for (linParams lm : metainfodb) {
							if (lm.name.trim().equals(f.getName().trim())) {
								switch (lm.value) {
								case "S":
									if (fval != null)
										qvalues = qvalues + "'" + fval + "'";
									else
										qvalues = qvalues + "NULL";
									break;
								case "E":
									if (fval != null)
										try {
											qvalues = qvalues + "'" + URLEncoder.encode(fval, "UTF-8") + "'";
										} catch (Exception e) {

										}
									else
										qvalues = qvalues + "NULL";
									break;
								default:
									qvalues = qvalues + fval;
									break;

								}
								metafound = true;
							}
							if (!metafound)
								qvalues = qvalues + fval;
						}
					} else {
						qvalues = qvalues + fval;
					}
				}
			}
		}
		query = query + "(" + qfields + ") VALUES (" + qvalues + ")";

		// ++++++++++++++++++++++++++++++++++++++
		// EXECUTE QUERY
		// ++++++++++++++++++++++++++++++++++++++
		if (ai != null)
			lastval_par = ai;

		boolean res = updateDB(query);

		// ++++++++++++++++++++++++++++++++++++++
		// UPDATE OBJECT
		// ++++++++++++++++++++++++++++++++++++++
		if (res) {
			if (ai != null && lastval != null)
				return lastval;
			else
				throw new LRWSException("E", "LRWS", "lrws.e.modnotcreated");

		} else
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
			if (!checkFieldExcluded(f.getName())) {
				String fval = null;
				try {
					fval = (String) f.get(this);
				} catch (Exception e) {
					throw new LRWSException("E", "LRWS", "lrws.e.modexkeyvalue");
				}
				if (qset)
					query = query + ", ";
				qset = true;

				if (metainfodb != null && metainfodb.length > 0) {
					boolean metafound = false;
					for (linParams lm : metainfodb) {
						if (lm.name.trim().equals(f.getName().trim())) {
							switch (lm.value) {
							case "S":
								if (fval != null)
									query = query + f.getName() + " = " + "'" + fval + "'";
								else
									query = query + f.getName() + " = " + "NULL";
								break;
							case "E":
								if (fval != null)
									try {
										query = query + f.getName() + " = " + "'" + URLEncoder.encode(fval, "UTF-8")
												+ "'";
									} catch (Exception e) {

									}
								else
									query = query + f.getName() + " = " + "NULL";
								break;
							default:
								query = query + f.getName() + " = " + fval;
								break;

							}
							metafound = true;
						}
						if (!metafound)
							query = query + f.getName() + " = " + fval;
					}
				} else {
					query = query + f.getName() + " = " + fval;
				}
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
			String mityp = getMetaInfoType(f.getName());
			if (!checkFieldExcluded(f.getName())) {
				boolean qlink = false;
				for (linParams lin : keys) {
					if (lin.name != null && lin.name.trim().toUpperCase().equals(f.getName().trim().toUpperCase())) {
						if (lin.value != null && lin.value.trim().length() > 0) {
							if (qlink)
								query = query + " AND ";
							if (mityp != null) {
								switch (mityp) {
								case "S":
									if (lin.value != null)
										query = query + " " + f.getName() + " = '" + lin.value.trim() + "' ";
									else
										query = query + " " + f.getName() + " = NULL ";
									break;
								case "E":
									if (lin.value != null)
										try {
											query = query + " " + f.getName() + " = '"
													+ URLDecoder.decode(lin.value.trim(), "UTF-8") + "' ";
										} catch (Exception e) {
											throw new LRWSException("E", "LRWS", "lrws.e.modencodingpar");
										}
									else
										query = query + " " + f.getName() + " = NULL ";
									break;
								default:
									query = query + " " + f.getName() + " = " + lin.value.trim() + " ";
									break;
								}
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

	/*
	 * #########################################################################
	 * ##### CHECK FIELD EXCLUDED(true)
	 * #########################################################################
	 * #####
	 */

	private boolean checkFieldExcluded(String fname) {
		if (fname == null)
			return false;
		String[] fexcl = new String[] { "serialVersionUID", "targetDB", "sconf", "DBD", "metainfodb" };
		for (String s : fexcl)
			if (s.trim().equals(fname.trim()))
				return true;
		if (metainfodb != null && metainfodb.length > 0)
			for (linParams ml : metainfodb)
				if (ml != null && ml.name.trim().equals(fname.trim()) && ml.value != null
						&& ml.value.trim().toUpperCase().equals("X"))
					return true;

		return false;

	}

	/*
	 * #########################################################################
	 * ##### GET METINFOTYPE
	 * #########################################################################
	 * #####
	 */

	private String getMetaInfoType(String fname) {
		if (fname == null)
			return null;
		for (linParams mi : metainfodb)
			if (mi.name.trim().equals(fname.trim()))
				return mi.value;
		return null;

	}

}
