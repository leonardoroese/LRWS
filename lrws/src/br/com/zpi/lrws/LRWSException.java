package br.com.zpi.lrws;

/**
 * Created by leonardo on 19/12/15.
 */
public class LRWSException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String msg = null;
    private int errNum = 0;
    private String errCode = null;
    private String errType = null;
    
	public LRWSException(){
    	this.errCode = null;
    	this.errType = null;
    	this.msg = null;
    }
	
	public LRWSException(String type, String code, String message){
    	this.errCode = code;
    	this.errType = type;
    	this.msg = message;
    }
    
    
    public int getErrNum() {
		return errNum;
	}

	public void setErrNum(int errNum) {
		this.errNum = errNum;
	}

	@Override
    public String getMessage() {
        return msg;
    }

    public void setMessage(String message){
        msg = message;
    }
    
    public String getErrCode() {
		return errCode;
	}


	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}


	public String getErrType() {
		return errType;
	}


	public void setErrType(String errType) {
		this.errType = errType;
	}
}
