package Data;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import common.ConfigReader;

public class DataHandler {
	private DataStore ds;
	private VectorTable vt;
	private ConfigReader cd;

	public DataHandler() {
		ds = DataStore.getInstance();
		vt = VectorTable.getInstance();
		cd = ConfigReader.getInstance();
	}

	public String getVersion() {
		try {
			return ds.getVersion().toString();
		} catch (JSONException e) {
			return "Exception when getting version";
		}
	}

	public JSONObject post(String query, String ip) {
		JSONObject myclock = null;
		try {
			JSONObject req = new JSONObject(query);
			JSONObject clock = req.getJSONObject("clock");
			String tweet = req.getString("tweet");
			String userName = req.getString("name");
			if(!vt.isLesser(clock)){
				Thread.sleep(4000);
			}
			JSONObject vc = ds.addTweet(ip + "," + cd.getDataport(),null,tweet, true, userName);
			vt.logUpdate(ip + "," + cd.getDataport(), vc.getJSONObject("clock"), vc.getInt("version"), tweet, false, userName, true);
			myclock = vt.getVectorClock();
		} catch (JSONException e) {
			System.out.println("JSON EXCEPTION");
			e.printStackTrace();
		}catch (InterruptedException e) {
			System.out.println("Interrupted EXCEPTION");
		}
		return myclock;
	}
	
	public JSONObject delete(String query, String ip) {
		JSONObject myclock = null;
		try {
			JSONObject req = new JSONObject(query);
			JSONObject clock = req.getJSONObject("clock");
			JSONObject tclock = req.getJSONObject("tclock");
			String tweet = req.getString("tweet");   
			String userName = req.getString("name");
			if(!vt.isLesser(clock)){
				Thread.sleep(4000);
			}
			JSONObject vc = ds.deleteTweet(ip + "," + cd.getDataport(), null,tweet, true, userName, tclock);
			vt.logUpdate(ip + "," + cd.getDataport(), tclock, vc.getInt("version"), tweet, false, userName, false);
			myclock = vt.getVectorClock();
		} catch (JSONException e) {
			System.out.println("JSON EXCEPTION");
			e.printStackTrace();
		}catch (InterruptedException e) {
			System.out.println("Interrupted EXCEPTION");
		}
		return myclock;
	}

	public String getTweets(String query) {
		String response = null;
		try {
			JSONObject req = new JSONObject(query);
			JSONObject clock = req.getJSONObject("clock");
			String tag = req.getString("tag");
			if(!vt.isLesser(clock)){
				Thread.sleep(4000);
			}
			int ver = req.getInt("version");
			int myversion = ds.queryVersion(tag);
			if(ver == myversion ){
				response = new JSONObject().put("clock", vt.getVectorClock()).put("version",ver).put("array","[]").toString();
			}else {
				response = new JSONObject().put("clock", vt.getVectorClock()).put("version",myversion).put("array",ds.getTweets(tag)).toString();
			}
		} catch (JSONException e) {
			System.out.println("JSON EXCEPTION in getTweets");
		}catch (InterruptedException e) {
			System.out.println("Interrupted EXCEPTION");
		}
		return response; 
	}
	
	public String getUserTweets(String query) {
		String response = null;
		try {
			JSONObject req = new JSONObject(query);
			JSONObject clock = req.getJSONObject("clock");
			String user = req.getString("user");
			if(!vt.isLesser(clock)){
				Thread.sleep(4000);
			}
			response = new JSONObject().put("clock", vt.getVectorClock()).put("array",ds.getUserTweets(user)).toString();
		} catch (JSONException e) {
			System.out.println("JSON EXCEPTION in getUserTweets");
		}catch (InterruptedException e) {
			System.out.println("Interrupted EXCEPTION");
		}
		return response; 
	}

	// Match with vector clock and send missing updates. Add new IPs in vector clock if found.
	public String getUpdates(String input) {
		String result = null;
		try {
			JSONObject update = new JSONObject();
			JSONObject in = new JSONObject(input);
			JSONObject my = vt.getVectorClock();

			for (@SuppressWarnings("unchecked")
			Iterator<String> it = in.keys(); it.hasNext();) {
				String ip = it.next();
				int no = in.getInt(ip);
				if (my.has(ip)) {
					int myno = my.getInt(ip);
					ArrayList<JSONObject> l = new ArrayList<JSONObject>();
					for (int i = no + 1; i <= myno; i++) {
						l.add(vt.getUpdate(ip, i).put("number", i));
					}
					update.put(ip, l);
				} else if(!vt.isDead(ip)) {
					vt.addNewVectorClock(ip, 0);
				}
			}
			result = new JSONObject().put("clock", my).put("updates", update).toString();
		} catch (JSONException e) {
			System.out.println("JSON EXCEPTION");
		}
		return result;
	}

	// Send the entire data for a new Back end.
	public String bootStrapme() {
		String result = null;
		try {
			JSONObject myclock = vt.getVectorClock();
			JSONObject mylog = vt.getLogTable();
			JSONObject data = ds.getDataStore();
			JSONObject dead = vt.getdeadlist();
			JSONObject tagversion = ds.getTagversion();
			JSONObject userlogs = ds.getUserlogs();

			result = new JSONObject().put("clock", myclock).put("updates", mylog).put("data", data).put("dead", dead).put("tagversion", tagversion).put("name", userlogs).toString();
		} catch (JSONException e) {
			System.out.println("JSON EXCEPTION");
		}
		return result;
	}

	public String bootStraphttp() {
		String result = null;
		try {
			JSONObject myclock = vt.getVectorClock();
			JSONObject dead = vt.getdeadlist();

			result = new JSONObject().put("clock", myclock).put("dead", dead).toString();
		} catch (JSONException e) {
			System.out.println("JSON EXCEPTION");
		}
		return result;
	}
	
	public String getClockforHttp(){
		String result = null;
		try {
			JSONObject myclock = vt.getVectorClock();
			result = new JSONObject().put("clock", myclock).toString();
		} catch (JSONException e) {
			System.out.println("JSON EXCEPTION");
		}
		return result;
	}
	
}
