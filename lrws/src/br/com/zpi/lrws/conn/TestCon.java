package br.com.zpi.lrws.conn;

public class TestCon extends ConBaseStd {
	
	public TestCon(String confpath) {
		super(confpath);
	}

	public boolean test(){
		resType = "E";
		resMsg = "";
		
		if(readDb("SELECT version()") != null){
			resType = "S";
			resMsg = "Connection OK";
			return true;
		}
		resMsg = "Connection not tested, check connection params, or maybe there is not users on the database yet.";
		return false;
	}
}
