package br.com.zpi.lrws.servlet;

import java.util.ArrayList;

import br.com.zpi.lrws.linParams;

public abstract class LRWSEndpointMap {
	
	public ArrayList<linParams> map = null;
	
	public LRWSEndpointMap() {
		
	}
	
	public String getServletReceiver(String endpoint) {
		if(map != null) {
			for(linParams p : map) {
				if(p.name.toUpperCase().trim().startsWith(endpoint))
					return p.value;
			}
		}
		return null;
	}
}
