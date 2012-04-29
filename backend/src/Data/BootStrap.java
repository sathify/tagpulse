  package Data;

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

public class BootStrap {
	private DataStore ds;
	private VectorTable vt;
	private Socket s;
	private PrintWriter out;
	private BufferedReader instream;
	private String request;
	private String response;
	private ConfigReader cd;
	private String dataip;
	private int dataport;

	public BootStrap(int port) {
		ds = DataStore.getInstance();
		vt = VectorTable.getInstance();
		cd = ConfigReader.getInstance();
		s = null;
		out = null;
		instream = null;
		dataip = cd.getDataip();
		dataport = port;
		request = null;
		response = null;
	}

	public void setupBEServer() {
		sendRequest();
		if (response != null) {
			setupDatabase();
		}
	}

	public void sendRequest() {
		try {
			openConnection();
			request = Constants.GET + " " + Constants.BOOTSTRAPDATA;
			out.println(request + " HTTP/1.1\r\n");
			out.println("Host: localhost:" + dataport + "\r\n");
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

	public void setupDatabase() {
		try {

			JSONObject req = new JSONObject(response);
			JSONObject vclock = req.getJSONObject("clock");
			JSONObject updates = req.getJSONObject("updates");
			JSONObject data = req.getJSONObject("data");
			JSONObject dead = req.getJSONObject("dead");
			JSONObject vect= req.getJSONObject("tagversion");
			JSONObject user= req.getJSONObject("name");

			vt.setVectorclock(vclock);
			vt.setVectortable(updates);
			vt.setDeadlist(dead);
			ds.setupDS(data);
			ds.setTagversion(vect);
			ds.setupUserlogs(user);

		} catch (JSONException e) {
			System.out.println("JSON EXCEPTION IN PROCESS REQUEST");
			e.printStackTrace();
		}
	}

	public void openConnection() {
		try {
			s = new Socket(dataip, dataport);
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
