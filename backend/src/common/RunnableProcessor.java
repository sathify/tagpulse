package common;


import java.io.BufferedReader; 
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import common.Constants;

public abstract class RunnableProcessor implements Runnable {

	private Socket clientSocket;
	private String requestStr;
	private String responseStr;
	private int operation;
	public boolean shutdown;
	private Logger l = null;
	public String LogFile = "default.log";
	public Password cs = Password.getInstance();
	
	public RunnableProcessor(Socket csocket) {
		this.clientSocket = csocket;
		this.requestStr = null;
		this.responseStr = null;
		this.operation = 0;
		this.shutdown = false;
	}

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
			
			getRequest(in, output);
			log("IP: "+ clientSocket.getInetAddress().getHostAddress() + " Request: " + requestStr);
			if(requestStr != null){
				responseStr = processRequest(requestStr, operation);
			}
			
			log("IP: "+getMyip() + " Response: " + responseStr );
			sendResponse(responseStr, output);
			
			in.close();
			output.close();
			clientSocket.close();
						
			if(shutdown){
				StopServer st = StopServer.getInstance();
				st.stopServer();		
			}
			
		} catch (Exception e) {
			System.out.println("Issues reading from and writing to the client socket");
			e.printStackTrace();
		}
	}
	
    
	public void getRequest(BufferedReader in, PrintWriter out){
		try{
			String str = in.readLine();
			String arr[] = str.split("\\s");
			
			if(arr.length !=3){
				responseStr = printBadRequest();
				return;
			}
			String method = arr[0].toUpperCase();
			String path = arr[1];
			String version = arr[arr.length - 1].toUpperCase();
			
			if (!version.equals(Constants.HTTP_VERSION)) {
				responseStr = printVersionNotSupported();
			} else if (method.equals(Constants.PUT)  || method.equals(Constants.OPTIONS)) {
				responseStr = printMethodNotImplemented();
			} else if (method.equals(Constants.GET)) {
				requestStr = path;
				operation = 1;
			} else if (method.equals(Constants.POST)) {
				if (path.startsWith(Constants.POSTS)) {
					String status = path.substring(Constants.POSTS.length());
					if (status.startsWith(Constants.POSTSTATUS)) {
						String post = URLDecoder.decode(status.substring(Constants.POSTSTATUS.length()),"UTF-8");
						requestStr = post;
					} else {
						String length = null;
						while (!(str = in.readLine()).equals(Constants.EMPTY_STRING)) {
							if (str.toLowerCase().startsWith(Constants.CONTENTLENGTH)) {
								length = str;
							}
						}
						int len = Integer.parseInt(length.substring(Constants.CONTENTLENGTH.length()));
						StringBuilder parameter = new StringBuilder();
						for (int i = 0; i < len; i++) {
							parameter.append((char) in.read());
						}
						String post = URLDecoder.decode(parameter.toString().substring(7), "UTF-8");
						requestStr = post;
					}
					operation = 2;
				} else {
					responseStr = printNotFoundError();
				}
			}else if (method.equals(Constants.DELETE)) {
				requestStr = path;
				operation = 3;
			} else{
				responseStr = printBadRequest();
			}	
		} catch (IOException e) {
			System.out.println("Issues reading from and writing to the client socket");
			e.printStackTrace();
		}
	}
	
    public abstract String processRequest(String request, int flag);
    
    public void sendResponse(String response, PrintWriter out){
    	out.println(response);
    }
    
	public String printNoContent() {
		return Constants.HTTP_NOCONTENT + Constants.NEWLINE;
	}
	
	public String printNoContentWithResponce(String s) {
		return Constants.HTTP_NOCONTENT + Constants.NEWLINE + s + Constants.NEWLINE  ;
	}

	public String printVersionNotSupported() {
		return Constants.HTTP_VERSIONNOTSUPPORTED + Constants.NEWLINE;
	}

	public String printMethodNotImplemented() {
		return Constants.HTTP_NOTIMPLEMENTED + Constants.NEWLINE;
	
	}
	
	public String printNotFoundError() {
		return Constants.HTTP_NOTFOUND + Constants.NEWLINE;
	}
	
	public String printOkShutdown() {
		return Constants.HTTP_OK + Constants.NEWLINE + "SERVER IS SHUTDOWN";
	}
	
	public String printUnAuthorized() {
		return Constants.HTTP_UNAUTH + Constants.NEWLINE + "YOU DO NOT HAVE AUTHORIZATION";
	}
	
	public String printBadRequest() {
		return Constants.HTTP_BADREQUEST + Constants.NEWLINE;
	}
	
	public String printInternalServerError() {
		return Constants.HTTP_SERVERERROR + Constants.NEWLINE;
	}
	
	public String printOKandJson(String Json) {
		return Constants.HTTP_OK + Constants.NEWLINE + Json + Constants.NEWLINE;
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
	
	@SuppressWarnings("rawtypes")
	public void getLogger(Class className){
		l = CustomLogger.getCustomLogger(className, LogFile, true);
	}

	public void log(String msg){		
		l.info(msg);
	}
}
