package common;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ConfigReader {

	private static final ConfigReader INSTANCE = new ConfigReader();
	private String Dataip;
	private int httpport;
	private int dataport;

	public static ConfigReader getInstance() {
		return INSTANCE;
	}

	private ConfigReader() {
	}

	public void setup(int Httpport, int Dataport, String dataip) {

		Dataip = dataip;
		httpport = Httpport;
		dataport = Dataport;

	}
	
	public void connectBE(int Dataport, String dataip) {
		Dataip = dataip;
		dataport = Dataport;
	}
	

	public int getHttpport() {
		return this.httpport;
	}

	public String getDataip() {
		return this.Dataip;
	}

	public int getDataport() {
		return this.dataport;
	}
	
	public String getMyip(){
		String ip = null;
		try{
		      InetAddress IP = InetAddress.getLocalHost();
		      ip = IP.getHostAddress();
		    }catch (UnknownHostException e){
		      System.out.println("Exception caught while getting my ip ="+e.getMessage());
		    }
		return ip;
	}

}
