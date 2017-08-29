package br.com.zpi.lrws.conn;

import java.io.Serializable;

public class DBLin implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public int lin = 0;
	public DBParVal[] cols = null;

	public String getVal(String parname) {
		if (this.cols != null) {
			if (this.cols.length > 0) {
				for (DBParVal lin : this.cols) {
					if (lin.param.equals(parname)) {
						return lin.value;
					}
				}
			}
		}
		return null;
	}
}
