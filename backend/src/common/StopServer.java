package common;

public class StopServer {
	private  MultithreadedServer TempServer;
	
	private StopServer(){}
	
    private static final StopServer myInstance = new StopServer();
    
	public void setInstance(MultithreadedServer srvr) {
		TempServer = srvr;
	}
	
	public static StopServer getInstance(){
		if(myInstance!= null){
			return myInstance;
		}
		return new StopServer();
	}
	
	public void stopServer(){
		TempServer.stopServer();
	}
}
