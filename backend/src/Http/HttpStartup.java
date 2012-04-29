package Http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import common.ConfigReader;
import common.Constants;

public class HttpStartup {
	private ConfigReader cr;
	private HttpClock hc;
	private String Dataip;
	private int dataPort;
	private Socket s;
	private PrintWriter out;
	private BufferedReader instream;
	private String request;
	private String response;
	
	
	public HttpStartup() {
		this.cr = ConfigReader.getInstance();
	    this.Dataip = cr.getDataip();
		this.dataPort = cr.getDataport();
		this.hc = HttpClock.getInstance();
		s = null;
		out = null;
		instream = null;
		request = null;
		response = null;
	}
	
	public void setupHTTPServer() {
		sendRequest();
		if (response != null) {
			setup();
		}
	}


	private void sendRequest() {
		try {
			openConnection();
			request = Constants.GET + " " + Constants.BOOTSTRAPHTTP;
			out.println(request + " HTTP/1.1\r\n");
			out.println("Host: localhost:" + dataPort + "\r\n");
			out.println("");
			out.flush();
			String str = "";
			response = null;
			while ((str = instream.readLine()) != null) {
				System.out.println(str);
				if (str.startsWith("{"))
					response = str;
			}
			closeAll();
		} catch (UnsupportedEncodingException e) {
			System.out.println("EncodingException");
		} catch (IOException e) {
			System.out.println("EncodingException");
		}
		
	}

	private void setup() {
		try {

			JSONObject req = new JSONObject(response);
			JSONObject vclock = req.getJSONObject("clock");
			JSONObject dead = req.getJSONObject("dead");

			hc.setVectorclock(vclock);
			hc.setDeadlist(dead);	

		} catch (JSONException e) {
			System.out.println("JSON EXCEPTION IN PROCESS REQUEST");
			e.printStackTrace();
		}
	}
	
	public void openConnection() {
		try {
			s = new Socket(Dataip, dataPort);
			out = new PrintWriter(s.getOutputStream());
			instream = new BufferedReader(new InputStreamReader(
					s.getInputStream()));
		} catch (UnknownHostException e) {
			System.out.println("HostException in opening sockets");
		} catch (IOException e) {
			System.out.println("IOException in opening sockets");
			e.printStackTrace();
		}
	}

	public void closeAll() {
		try {
			out.close();
			instream.close();
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
