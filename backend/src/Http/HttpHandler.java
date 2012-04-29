package Http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import common.ConfigReader;

public class HttpHandler {
	private ConfigReader cr;
	private HttpClock hc;
	private CacheData cd;
	private String Dataip;
	private int dataPort;
	private Socket s;
	private PrintWriter out;
	private BufferedReader instream;
	private String response;

	public HttpHandler() {
		this.cd = CacheData.getInstance();
		this.cr = ConfigReader.getInstance();
		this.Dataip = cr.getDataip();
		this.dataPort = cr.getDataport();
		this.hc = HttpClock.getInstance();
		s = null;
		out = null;
		instream = null;
		response = null;
	}
	
	public boolean deleteTweet(String post) {
		boolean success = false;
		int count = 0;
		while (!(success = deleteTweetDataServer(post)) && count < hc.getClock().length()-1) {
			System.out.println(Dataip + "," + dataPort);
			hc.addDeadServer(Dataip + "," + dataPort);
			Dataip = cr.getDataip();
			dataPort = cr.getDataport();
			hc.setNewDataServer(Dataip, dataPort);
			count++;
		}
		return success;
	}
	
	public boolean deleteTweetDataServer(String query) {
		boolean success = false;
		try {
			JSONObject req = new JSONObject(query);
			JSONObject tclock = req.getJSONObject("tclock");
			String tweet = req.getString("tweet");
			String user = req.getString("username");
			s = new Socket(Dataip, dataPort);
			out = new PrintWriter(s.getOutputStream());
			instream = new BufferedReader(new InputStreamReader(
					s.getInputStream()));
			String req1 = "remove?tweet="
					+ URLEncoder.encode(new JSONObject().put("tclock", tclock).put("tweet", tweet).put("name",user)
							.put("clock", hc.getClock()).toString(), "UTF-8");
			out.println("DELETE /" + req1 + " HTTP/1.1");
			out.println("Host: localhost:" + dataPort + " \r\n");
			out.println("");
			out.flush();
			String OK = "204";
			String str = instream.readLine();
			if (str.contains(OK)) {
				success = true;
			}
			response = null;
			while ((instream.readLine()) != null) {
				if (str.startsWith("{")) {
					response = str;
				}
			}
			closeAll();
			if (response != null) {
				processPost();
			}
		} catch (UnknownHostException e) {
			System.out.println("Unknown host exception caused from httpserver");
		} catch (IOException e) {
			System.out.println("IOException caused when creating sockets from httpserver");
		} catch (JSONException e) {
			e.printStackTrace();
			System.out.println("JSONException caused when creating sockets from httpserver");
		}
		return success;
	}

	public boolean postTweet(String tweet) {
		boolean success = false;
		int count = 0;
		while (!(success = postTweetDataServer(tweet)) && count < hc.getClock().length()-1) {
			System.out.println(Dataip + "," + dataPort);
			hc.addDeadServer(Dataip + "," + dataPort);
			Dataip = cr.getDataip();
			dataPort = cr.getDataport();
			hc.setNewDataServer(Dataip, dataPort);
			count++;
		}
		return success;
	}

	public boolean postTweetDataServer(String query) {
		boolean success = false;
		try {
			JSONObject req = new JSONObject(query);
			String tweet = req.getString("tweet");
			String user = req.getString("username");
		
			s = new Socket(Dataip, dataPort);
			out = new PrintWriter(s.getOutputStream());
			instream = new BufferedReader(new InputStreamReader(
					s.getInputStream()));
			String reqs = "status/update?status="
					+ URLEncoder.encode(new JSONObject().put("tweet", tweet).put("name",user)
							.put("clock", hc.getClock()).toString(), "UTF-8");
			out.println("POST /" + reqs + " HTTP/1.1");
			out.println("Host: localhost:" + dataPort + " \r\n");
			out.println("");
			out.flush();
			String OK = "204";
			String str = instream.readLine();
			if (str.contains(OK)) {
				success = true;
			}
			response = null;
			while ((instream.readLine()) != null) {
				if (str.startsWith("{")) {
					response = str;
				}
			}
			closeAll();
			if (response != null) {
				processPost();
			}
		} catch (UnknownHostException e) {
			System.out.println("Unknown host exception caused from httpserver");
		} catch (IOException e) {
			System.out.println("IOException caused when creating sockets from httpserver");
		} catch (JSONException e) {
			System.out.println("JSONException caused when creating sockets from httpserver");
		}
		return success;
	}

	public void processPost() {
		try {
			JSONObject req = new JSONObject(response);
			JSONObject clock = req.getJSONObject("clock");
			hc.UpdateClock(clock);
		} catch (JSONException e) {
			System.out.println("JSONException from httpserver");
		}
	}

	public String queryData(String Query) {
		String success = null;
		int count = 0;
		while ((success = queryDataServer(Query)) != null
				&& count < hc.getClock().length()-1) {
			System.out.println(Dataip + "," + dataPort);
			hc.addDeadServer(Dataip + "," + dataPort);
			Dataip = cr.getDataip();
			dataPort = cr.getDataport();
			hc.setNewDataServer(Dataip, dataPort);
			count++;
		}
		return success;
	}

	public String queryDataServer(String Query) {
		String query = Query.toLowerCase();
		String tweets = null;
		int count = 0;
		while (true) {
			try {
				s = new Socket(Dataip, dataPort);
				out = new PrintWriter(s.getOutputStream());
				instream = new BufferedReader(new InputStreamReader(
						s.getInputStream()));
				String req = "search?q="
						+ URLEncoder.encode(
								new JSONObject().put("tag", query)
										.put("clock", hc.getClock())
										.put("version", cd.getVersion(query))
										.toString(), "UTF-8");

				out.println("GET /" + req + " HTTP/1.1");
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
				if (response != null) {
					tweets = processGet(query);
				}
				break;
			} catch (UnknownHostException e) {
				System.out.println("Unknown host exception caused from httpserver");
			} catch (IOException e) {
				System.out.println("IOException caused when creating sockets from httpserver");
			} catch (JSONException e) {
				System.out.println("JSONException from httpserver");
			}
			hc.addDeadServer(Dataip + "," + dataPort);
			Dataip = cr.getDataip();
			dataPort = cr.getDataport();
			hc.setNewDataServer(Dataip, dataPort);
			count++;
			if(count > hc.getClock().length()){
				break;
			}
		}
		return tweets;
	}

	public String processGet(String query) {
		String tweets = null;
		try {
			JSONObject req = new JSONObject(response);
			JSONObject clock = req.getJSONObject("clock");
			hc.UpdateClock(clock);
			int ver = req.getInt("version");
			if (ver == cd.getVersion(query)) {
				tweets = new JSONObject().put(
						"tweets",
						new JSONObject().put("query", query)
								.put("cached", "yes")
								.put("tweet", new JSONArray(cd.getTweets(query)))).toString();
			} else {
				String value = req.getJSONArray("array").toString();
				cd.addLocal(query, ver, value);
				tweets = new JSONObject().put(
						"tweets",
						new JSONObject().put("query", query)
								.put("cached", "no").put("tweet", new JSONArray(value)))
						.toString();
			}
		} catch (JSONException e) {
			System.out
					.println("JSONException caused when creating sockets from httpserver");
			e.printStackTrace();
		}
		return tweets;
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

	public String getFromDataServer(String query) {
		String success = null;
		int count = 0;
		while ((success = getUserTweetsDataServer(query)) != null
				&& count < hc.getClock().length()-1) {
			System.out.println(Dataip + "," + dataPort);
			hc.addDeadServer(Dataip + "," + dataPort);
			Dataip = cr.getDataip();
			dataPort = cr.getDataport();
			hc.setNewDataServer(Dataip, dataPort);
			count++;
		}
		return success;
	}

	private String getUserTweetsDataServer(String query) {
		String tweets = null;
		int count = 0;
		while (true) {
			try {
				s = new Socket(Dataip, dataPort);
				out = new PrintWriter(s.getOutputStream());
				instream = new BufferedReader(new InputStreamReader(
						s.getInputStream()));
				String req = "gettweets?username="
						+ URLEncoder.encode(
								new JSONObject().put("user", query)
										.put("clock", hc.getClock())
										.toString(), "UTF-8");

				out.println("GET /" + req + " HTTP/1.1");
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
				if (response != null) {
					tweets = processGettweets(query);
				}
				break;
			} catch (UnknownHostException e) {
				System.out.println("Unknown host exception caused from httpserver");
			} catch (IOException e) {
				System.out.println("IOException caused when creating sockets from httpserver");
			} catch (JSONException e) {
				System.out.println("JSONException from httpserver");
			}
			hc.addDeadServer(Dataip + "," + dataPort);
			Dataip = cr.getDataip();
			dataPort = cr.getDataport();
			hc.setNewDataServer(Dataip, dataPort);
			count++;
			if(count > hc.getClock().length()){
				break;
			}
		}
		return tweets;
	}

	private String processGettweets(String query) {
		String tweets = null;
		try {
			JSONObject req = new JSONObject(response);
			JSONObject clock = req.getJSONObject("clock");
			hc.UpdateClock(clock);	
			tweets = new JSONObject().put("tweets", new JSONObject().put("user", query)
								.put("cached", "no").put("tweets", req.getJSONArray("array"))).toString();	
		} catch (JSONException e) {
			System.out.println("JSONException caused when creating sockets from httpserver");
			e.printStackTrace();
		}
		return tweets;
	}

}
