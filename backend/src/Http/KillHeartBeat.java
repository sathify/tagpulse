package Http;


public class KillHeartBeat {
	private  HeartBeat TempServer;
	
	private KillHeartBeat(){}
	
    private static final KillHeartBeat myInstance = new KillHeartBeat();
    
	public void setInstance(HeartBeat srvr) {
		TempServer = srvr;
	}
	
	public static KillHeartBeat getInstance(){
		if(myInstance!= null){
			return myInstance;
		}
		return new KillHeartBeat();
	}
	
	public void stop(){
		TempServer.stopHeartbeat();
	}
}
