package br.com.zpi.lrws.translate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class Translate {

	private ArrayList<String[]> msgs = new ArrayList<String[]>();

	/*
	 * #########################################################################
	 * ##### CONSTRUCTOR
	 * #########################################################################
	 * #####
	 */
	
	public Translate(String filepath) {
		String[] mtz = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			String line;
			while ((line = br.readLine()) != null) {
				if (line != null && line.indexOf('=') >= 0) {
					mtz = line.split("=");
					if (mtz != null && mtz.length > 1) {
						String[] l = new String[2];
						l[0] = mtz[0];
						l[1] = mtz[1];
						msgs.add(l);
					}
				}
			}
		} catch (Exception ex) {
			msgs = null;
		}
	}

	
	/*
	 * #########################################################################
	 * ##### GET TRANSLATION
	 * #########################################################################
	 * #####
	 */
	public String getTrans(String msgid) {
		if (msgs != null && msgs.size() > 0) {
			for (String[] s : msgs) {
				if (s[0] != null && s[0].trim().toUpperCase().equals(msgid.trim().toUpperCase())) {
					return s[1];
				}
			}
		}
		return null;
	}
}
