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

public class HeartBeat implements Runnable {
	private Socket s;
	private PrintWriter out;
	private BufferedReader instream;
	private String request;
	private String response;
	private ConfigReader cd;
	private HttpClock hc;
	private String Dataip;
	private int dataPort;
	private boolean stopped;

	public HeartBeat() {
		cd = ConfigReader.getInstance();
		hc = HttpClock.getInstance();
		s = null;
		out = null;
		instream = null;
		request = null;
		response = null;
		stopped = false;
		Dataip = cd.getDataip();
		dataPort = cd.getDataport();
	}

	public void run() {
		while (!stopped) {
			getInformation();
			if (response != null) {
				System.out.println(response);
				process();
			}
			try {
				Thread.sleep(20000);
			} catch (Exception e) {
				System.out.println("Exception in Gossip");
			}
		}

	}

	public void getInformation() {
		try {
			openConnection(Dataip, dataPort);
			request = Constants.GET + " " + Constants.HEARTBEAT;
			out.println(request + " HTTP/1.1\r\n");
			out.println("Host: localhost:" + dataPort + "\r\n");
			out.println("");
			out.flush();
			String str = "";
			response = null;
			while ((str = instream.readLine()) != null) {
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

	public void process() {
		try {
			JSONObject req = new JSONObject(response);
			JSONObject clock = req.getJSONObject("clock");
			hc.UpdateClock(clock);
		} catch (JSONException e) {
			System.out.println("JSONException from httpserver");
		}
	}

	public void openConnection(String Dataip, int dataPort) {
		try {
			s = new Socket(Dataip, dataPort);
			out = new PrintWriter(s.getOutputStream());
			instream = new BufferedReader(new InputStreamReader(
					s.getInputStream()));
		} catch (UnknownHostException e) {
			System.out.println("HostException in opening sockets");
		} catch (IOException e) {
			System.out.println("IOException in opening sockets");
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
	
	public void stopHeartbeat(){
		this.stopped = true;
	}

}
