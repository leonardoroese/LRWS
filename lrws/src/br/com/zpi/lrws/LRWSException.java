package br.com.zpi.lrws;

/**
 * Created by leonardo on 19/12/15.
 */
public class LRWSException extends Exception {
    private String msg = null;
    private int errNum = 0;

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
}
