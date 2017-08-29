package br.com.zpi.lrws;

import java.io.Serializable;

/**
 * Created by leonardo on 10/6/15.
 */
public class linParams implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public String name = null;
    public String value = null;

    public linParams(String name, String value){
        this.name = name;
        this.value = value;
    }

    public linParams(){
    	
    }

}
