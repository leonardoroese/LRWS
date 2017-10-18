package br.com.zpi.lrws.model;

public class WSReturn {
	public String errorType = null;
	public String errorCode = null;
	public String errorMessage = null;
	
	public WSReturn() {
		
	}
	
	public WSReturn(String type, String code, String message) {
		errorType = type;
		errorCode = code;
		errorMessage = message;
	}
}
