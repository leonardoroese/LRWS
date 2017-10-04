package br.com.zpi.lrws.model;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import br.com.zpi.lrws.LRWSException;
import br.com.zpi.lrws.linParams;
import br.com.zpi.lrws.conn.ConBase;
import br.com.zpi.lrws.conn.Configurations;
import br.com.zpi.lrws.conn.DBLin;
import br.com.zpi.lrws.conn.DBParVal;

public class SmartModel extends ConBase implements Serializable {
	private static final long serialVersionUID = 1L;
	private String targetDB = null;
	private Configurations conf = null;
	private int DBD = 0;
	private linParams[] metainfodb = null;
	/*
	 * OPTIONS values S -String E - Encoded String X - not used
	 */
	public String rows_total = null;

	/*
	 * #########################################################################
	 * ##### CONSTRUCTOR
	 * #########################################################################
	 * #####
	 */

	public SmartModel(Configurations conf, int DBD, String targetDB) {
		super(conf, DBD);
		this.targetDB = targetDB;
		this.conf = conf;
		this.DBD = DBD;
		if(this.conf != null && this.conf.encoding == null)
			this.conf.encoding = "UTF-8";
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
	 * ##### keys = obligatory composed by linParam.name (name of key)
	 * linParam.value(value of key)
	 */

	public Object[] listOfMe(linParams[] keys, linParams[] ord, int page, int limit, boolean bringtotal)
			throws LRWSException {

		String query = "SELECT * FROM " + targetDB + " ";
		String querywhere = " WHERE ";
		String queryend = "";

		String querycount = "SELECT COUNT(*) AS rowstotal FROM " + targetDB + " ";
		String rows_count = null;

		// ++++++++++++++++++++++++++++++++++++++
		// PREPARE QUERY
		// ++++++++++++++++++++++++++++++++++++++
		if (keys != null && keys.length > 0) {
			boolean qlink = false;
			for (linParams lin : keys) {
				String mityp = getMetaInfoType(lin.name.trim());
				if (!checkFieldExcluded(lin.name.trim()) && lin.name != null
						&& lin.name.trim().toUpperCase().equals(lin.name.trim().toUpperCase())) {
					if (qlink)
						querywhere = querywhere + " AND ";
					if (mityp != null) {
						switch (mityp) {
						case "S":
							if (lin.value != null)
								querywhere = querywhere + " " + lin.name.trim() + " = '" + lin.value.trim() + "' ";
							else
								querywhere = querywhere + " " + lin.name.trim() + " IS NULL ";
							break;
						case "E":
							if (lin.value != null)
								try {
									querywhere = querywhere + " " + lin.name.trim() + " = '"
											+ URLDecoder.decode(lin.value.trim(), conf.encoding) + "' ";
								} catch (Exception e) {
									throw new LRWSException("E", "LRWS", "lrws.e.modencodingpar");
								}
							else
								querywhere = querywhere + " " + lin.name.trim() + " IS NULL ";
							break;
						default:
							querywhere = querywhere + " " + lin.name.trim() + " = " + lin.value.trim() + " ";
							break;
						}
					} else {
						if (lin.value != null)
							querywhere = querywhere + " " + lin.name.trim() + " = " + lin.value.trim() + " ";
						else
							querywhere = querywhere + " " + lin.name.trim() + " IS NULL ";
					}
					qlink = true;
				}
			}
			query = query + querywhere;
			querycount = querycount + querywhere;
		}

		if (ord != null && ord.length > 0) {
			queryend = queryend + " ORDER BY ";

			boolean ordq = false;

			for (linParams lin : ord) {
				if (ordq)
					queryend = queryend + ", ";
				queryend = queryend + lin.name + " " + lin.value;
				ordq = true;
			}
		}

		if (limit > 0) {
			switch (DBD) {
			case DBD_POSTGRESQL:
				queryend = queryend + " LIMIT " + limit + " OFFSET " + (limit * page);
				break;
			case DBD_MYSQL:
				queryend = queryend + " LIMIT " + (limit * page) + " , " + limit;
				break;
			default:
				queryend = queryend + " LIMIT " + (limit * page) + ", " + limit;
				break;
			}
		}
		query = query + queryend;

		// ++++++++++++++++++++++++++++++++++++++
		// EXECUTE QUERY
		// ++++++++++++++++++++++++++++++++++++++
		if (bringtotal) {
			ArrayList<DBLin> altt = readDb(querycount);
			if (altt != null && altt.size() > 0) {
				rows_count = altt.get(0).getVal("rowstotal").toString();
			}
		}
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
					Constructor<?> ctor = clazz.getConstructor(Configurations.class, int.class, String.class);
					out[c] = ctor.newInstance(new Object[] { conf, DBD, targetDB });
					for (DBParVal p : lin.cols) {
						try {
							if (p.value != null) {
								String mityp = getMetaInfoType(p.param);
								if (mityp != null && mityp.trim().equals("E") && p.value != null) {
									out[c].getClass().getDeclaredField(p.param).set(out[c],
											URLDecoder.decode(p.value, conf.encoding));
								} else {
									out[c].getClass().getDeclaredField(p.param).set(out[c], p.value);
								}
							}
						} catch (Exception e) {

						}
					}
				} catch (Exception e) {
					throw new LRWSException("E", "LRWS_NORES", "lrws.e.moderrcreateinst");
				}
				if (out != null && out[c] != null && bringtotal && rows_count != null) {
					try {
						out[c].getClass().getSuperclass().getDeclaredField("rows_total").set(out[c], rows_count);
					} catch (Exception e) {
						String em = e.getMessage();
					}
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
		boolean qlink = false;
		for (linParams lin : keys) {
			String mityp = getMetaInfoType(lin.name);
			if (!checkFieldExcluded(lin.name)) {
				if (lin.value != null && lin.value.trim().length() > 0) {
					if (qlink)
						query = query + " AND ";
					if (mityp != null) {
						switch (mityp) {
						case "S":
							if (lin.value != null)
								query = query + " " + lin.name + " = '" + lin.value.trim() + "' ";
							else
								query = query + " " + lin.name + " IS NULL ";
							break;
						case "E":
							if (lin.value != null)
								try {
									query = query + " " + lin.name + " = '"
											+ URLDecoder.decode(lin.value.trim(), conf.encoding) + "' ";
								} catch (Exception e) {
									throw new LRWSException("E", "LRWS", "lrws.e.modencodingpar");
								}
							else
								query = query + " " + lin.name + " IS NULL ";
							break;
						default:
							query = query + " " + lin.name + " = " + lin.value.trim() + " ";
							break;
						}
					} else {
						query = query + " " + lin.name + " = " + lin.value.trim() + " ";
					}
					qlink = true;
				} else {
					throw new LRWSException("E", "LRWS", "lrws.e.modinvalidkey");
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
						if (p.value != null) {
							String mityp = getMetaInfoType(p.param);
							if (mityp != null && mityp.trim().equals("E") && p.value != null) {
								this.getClass().getDeclaredField(p.param).set(this,
										URLDecoder.decode(p.value, conf.encoding));
							} else {
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
				if (ai == null || !ai.trim().toUpperCase().equals(f.getName().toUpperCase())) {
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
									if (fval != null) {
										if (qset) {
											qfields = qfields + ", ";
											qvalues = qvalues + ", ";
										}
										qfields = qfields + f.getName();
										qvalues = qvalues + "'" + fval + "'";
										qset = true;
									} else {
										if (qset) {
											qfields = qfields + ", ";
											qvalues = qvalues + ", ";
										}
										qfields = qfields + f.getName();
										qvalues = qvalues + "NULL";
										qset = true;
									}
									break;
								case "E":
									if (fval != null)
										try {
											if (qset) {
												qfields = qfields + ", ";
												qvalues = qvalues + ", ";
											}
											qfields = qfields + f.getName();
											qvalues = qvalues + "'" + URLEncoder.encode(fval, conf.encoding) + "'";
											qset = true;
										} catch (Exception e) {

										}
									else {
										if (qset) {
											qfields = qfields + ", ";
											qvalues = qvalues + ", ";
										}
										qfields = qfields + f.getName();
										qvalues = qvalues + "NULL";
										qset = true;
									}
									break;
								case "X":
									break;
								default:
									if (qset) {
										qfields = qfields + ", ";
										qvalues = qvalues + ", ";
									}
									qfields = qfields + f.getName();
									qvalues = qvalues + fval;
									qset = true;
									break;

								}
								metafound = true;
							}
						}
						if (!metafound) {
							if (qset) {
								qfields = qfields + ", ";
								qvalues = qvalues + ", ";
							}
							qfields = qfields + f.getName();
							qvalues = qvalues + fval;
							qset = true;
						}
					} else {
						if (qset) {
							qfields = qfields + ", ";
							qvalues = qvalues + ", ";
						}
						qfields = qfields + f.getName();
						qvalues = qvalues + fval;
						qset = true;
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
			if (ai != null)
				if (lastval != null)
					return lastval;
				else
					throw new LRWSException("E", "LRWS", "lrws.e.modnotcreated");
			else
				return null;

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
			boolean iskey = false;
			for (linParams lin : keys)
				if (lin.name.trim().toUpperCase().equals(f.getName().trim().toUpperCase()))
					iskey = true;

			if (!iskey && !checkFieldExcluded(f.getName())) {
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
									query = query + f.getName() + " IS " + "NULL";
								break;
							case "E":
								if (fval != null)
									try {
										query = query + f.getName() + " = " + "'" + URLEncoder.encode(fval, conf.encoding)
												+ "'";
									} catch (Exception e) {

									}
								else
									query = query + f.getName() + " IS " + "NULL";
								break;
							default:
								query = query + f.getName() + " = " + fval;
								break;

							}
							metafound = true;
						}
					}
					if (!metafound)
						query = query + f.getName() + " = " + fval;
				} else {
					query = query + f.getName() + " = " + fval;
				}
			}
		}

		boolean qlink = false;
		for (linParams lin : keys) {
			if (qlink)
				condition = condition + " AND ";
			String mityp = getMetaInfoType(lin.name);
			if (mityp != null) {
				switch (mityp) {
				case "S":
					if (lin.value != null)
						condition = condition + " " + lin.name + " = '" + lin.value.trim() + "' ";
					else
						condition = condition + " " + lin.name + " IS NULL ";
					break;
				case "E":
					if (lin.value != null)
						try {
							condition = condition + " " + lin.name + " = '"
									+ URLDecoder.decode(lin.value.trim(), conf.encoding) + "' ";
						} catch (Exception e) {
							throw new LRWSException("E", "LRWS", "lrws.e.modencodingpar");
						}
					else
						condition = condition + " " + lin.name + " IS NULL ";
					break;
				default:
					condition = condition + " " + lin.name + " = " + lin.value.trim() + " ";
					break;
				}
			} else {
				condition = condition + " " + lin.name + " = " + lin.value.trim() + " ";
			}
			qlink = true;
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
										query = query + " " + f.getName() + " IS NULL ";
									break;
								case "E":
									if (lin.value != null)
										try {
											query = query + " " + f.getName() + " = '"
													+ URLDecoder.decode(lin.value.trim(), conf.encoding) + "' ";
										} catch (Exception e) {
											throw new LRWSException("E", "LRWS", "lrws.e.modencodingpar");
										}
									else
										query = query + " " + f.getName() + " IS NULL ";
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
