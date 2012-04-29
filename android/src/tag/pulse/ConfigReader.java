package tag.pulse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream; /*
 * Singleton pattern to read project1.config file for IPs of HTTP and Data Servers 
 */

import java.util.Properties;

public class ConfigReader {
	private static ConfigReader instance = null;
	private String HTTPServerIP = null;
	private String DataServerIP = null;
	private int HTTPServerPort = 8080;
	private int DataServerPort = 8080;
	private Properties prop = null;
	private ConfigReader() {
		readConfigFromFile();
	}

	public static ConfigReader getInstance() {
		if (instance == null) {
			instance = new ConfigReader();
		}
		return instance;
	}

	public void readConfigFromFile() {
		prop = new Properties();
		try {
			String fileName = "client.config";
			InputStream is;
			is = new FileInputStream(fileName);
			prop.load(is);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		
		HTTPServerIP = prop.getProperty("HTTPServerIP");
		DataServerIP = prop.getProperty("DataServerIP");
		try{
		HTTPServerPort = Integer.parseInt(prop.getProperty("HTTPServerPort"));
		}catch(NumberFormatException nfe){
			
		}
		try{
		DataServerPort = Integer.parseInt(prop.getProperty("DataServerPort"));
		}catch(NumberFormatException nfe){}
	}
	
	// returns the Http server's IP address
	public String getHTTPServerIP() {
		return HTTPServerIP;
	}

	// returns the port at which HttpServer is listening
	public int getHTTPServerPort(){
		return HTTPServerPort;
	}
	
	// returns the Http server's IP address
	public String getDataServerIP() {
		return DataServerIP;
	}
	
	// returns the port at which HttpServer is listening
	public int getDataServerPort(){
		return DataServerPort;
	}
	

}
