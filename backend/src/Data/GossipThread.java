package Data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import common.ConfigReader;
import common.Constants;
import common.CustomLogger;

public class GossipThread implements Runnable {

	private DataStore ds;
	private VectorTable vt;
	private boolean stopped;
	private boolean dead;
	private HashMap<String, Integer> checkmap;
	private int gossip;
	private Socket s;
	private PrintWriter out;
	private BufferedReader instream;
	private String request;
	private String response;
	private ConfigReader cd;
	private Logger l;
	public String LogFile;

	public GossipThread(String filename) {
		ds = DataStore.getInstance();
		vt = VectorTable.getInstance();
		cd = ConfigReader.getInstance();
		checkmap = new HashMap<String, Integer>();
		gossip = 0;
		stopped = false;
		dead = false;
		s = null;
		out = null;
		instream = null;
		request = null;
		response = null;
		LogFile = filename;
		getLogger(GossipThread.class);
	}
	
	@SuppressWarnings("rawtypes")
	public void getLogger(Class className){
		l = CustomLogger.getCustomLogger(className, LogFile, true);
	}

	public void log(String msg){		
		l.info(msg);
	}

	public void run() {
		while (!stopped) {
			if (gossip == 0) {
				checkmap = vt.getclockforPruning();
			}
			Set<String> rms = vt.getOtherRMips();
			for (String s : rms) {
				String[] item = s.split(",");
				if (!s.equals(cd.getMyip() + ',' + cd.getDataport()) && !vt.isDead(s)) {
					gossip(item[0], Integer.parseInt(item[1]));
					log("IP: "+ this.s.getInetAddress().getHostAddress() + " Request: " + request);
					if (dead) {
						vt.addDeadlist(s);
						dead = false;
					}
					System.out.println("SENDING GOSSIP to : " + s);
					if (response != null) {
						System.out.println();
						System.out.println("Incomming Response:" + response);
						log("IP: "+ this.s.getInetAddress().getHostAddress() + " Request: " + response);
						processRequest();
					}
				}
			}
			gossip++;
			if (gossip == 4) {
				System.out.println("BEFORE PRUNING" + vt.getLogTable());
				pruneData();
				System.out.println("AFTER PRUNING" + vt.getLogTable());
				gossip = 0;
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				System.out.println("Exception in Gossip");
			}
		}
	}

	public void pruneData() {
		for (String s : checkmap.keySet()) {
			System.out.println("Pruning this ---->"+s+"=>"+checkmap.get(s));
			vt.removeLogs(s, checkmap.get(s));
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
			dead = true;
			System.out.println("IOException in opening sockets");
		}
	}

	public void gossip(String Dataip, int dataPort) {
		try {
			openConnection(Dataip, dataPort);
			request = Constants.GET
					+ " "
					+ Constants.GOSSIP
					+ URLEncoder
							.encode(vt.getVectorClock().toString(), "UTF-8");
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

	public void processRequest() {
		try {

			JSONObject req = new JSONObject(response);
			System.out.println("INCOMING CLOCK"
					+ req.getJSONObject("clock").toString());
			JSONObject clock = req.getJSONObject("clock");
			if (gossip == 4) {
				for (String ip : JSONObject.getNames(clock)) {
					setMinforCheckPoint(ip, clock.getInt(ip));
				}
			}
			JSONObject updates = req.getJSONObject("updates");
			for (String ip : JSONObject.getNames(updates)) {
				JSONArray l = updates.getJSONArray(ip);
				if (l != null) {
					for (int i = 0; i < l.length(); i++) {
						JSONObject n = l.getJSONObject(i);
						vt.logUpdate(ip, n.getJSONObject("clock"), n.getInt("number"),n.getString("tweet"), true,n.getString("user"), n.getBoolean("flag"));
						if(n.getBoolean("flag")){
							ds.addTweet(ip,n.getJSONObject("clock"),n.getString("tweet"), false,n.getString("user"));
						} else{
							ds.deleteTweet(ip, null, n.getString("tweet"), false, n.getString("user"), n.getJSONObject("clock"));
						}
					}
				}
			}
		} catch (JSONException e){
			System.out.println("JSON EXCEPTION IN PROCESS REQUEST");
			e.printStackTrace();
		}
	}

	public void setMinforCheckPoint(String ip, int no) {
		if (checkmap.containsKey(ip)) {
			int present = checkmap.get(ip);
			if (no < present) {
				checkmap.put(ip, no);
			}
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
	
	public void stopGossiping(){
		this.stopped = true;
	}
}
