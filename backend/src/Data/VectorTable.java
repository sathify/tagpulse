package Data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class VectorTable {

	private static final VectorTable INSTANCE = new VectorTable();

	private HashMap<String, Integer> clock;
	private HashMap<String, HashMap<Integer, Tweets>> table;
	private HashMap<String, Boolean> deadlist;
	private ReadWriteLock lock;

	private VectorTable() {
		clock = new HashMap<String, Integer>();
		table = new HashMap<String, HashMap<Integer, Tweets>>();
		deadlist = new HashMap<String, Boolean>();
		lock = new ReentrantReadWriteLock();
	}

	public static VectorTable getInstance() {
		return INSTANCE;
	}

	public void addDeadlist(String k) {
		lock.writeLock().lock();
		deadlist.put(k, true);
		lock.writeLock().unlock();
	}

	public HashMap<String, Integer> getclockforPruning() {
		lock.readLock().lock();
		HashMap<String, Integer> s = new HashMap<String, Integer>();
		for (String each : clock.keySet()) {
			s.put(each, clock.get(each));
		}
		lock.readLock().unlock();
		return s;
	}

	public boolean isDead(String k) {
		lock.readLock().lock();
		boolean s = deadlist.containsKey(k);
		lock.readLock().unlock();
		return s;
	}

	// update vector is done after adding the data update from a specific ip for
	// that ip
	public void updateVectorClock(String ip, int no) {
		clock.put(ip, no);
	}

	// setup clocks when starting the sever
	public void setVectorclock(JSONObject vclock) {
		Gson gson = new Gson();
		Type list = new TypeToken<HashMap<String, Integer>>() {
		}.getType();
		HashMap<String, Integer> c = new HashMap<String, Integer>();
		c = gson.fromJson(vclock.toString(), list);
		clock = c;
	}

	// setup log table when starting the server
	public void setVectortable(JSONObject updates) {
		HashMap<String, HashMap<Integer, Tweets>> temp = new HashMap<String, HashMap<Integer, Tweets>>();
		if(updates.length() > 0){
			for (String Tag : JSONObject.getNames(updates)) {
				HashMap<Integer, Tweets> obj = new HashMap<Integer, Tweets>();
				try {
					JSONArray arr = updates.getJSONArray(Tag);
					for (int i = 0; i < arr.length(); i++) {
						JSONObject j = arr.getJSONObject(i);
						obj.put(j.getInt("no"), new Tweets(j.getJSONObject("vc"),j.getString("tweet"),j.getString("user"),j.getBoolean("flag")));
					}
				} catch (JSONException e) {
					System.out.println("JSONException in bootstrap");
					e.printStackTrace();
				}
				temp.put(Tag, obj);
			}
		}
		table = temp;
	}

	// setup deadlist when starting the server
	public void setDeadlist(JSONObject dead) {
		Gson gson = new Gson();
		Type list = new TypeToken<HashMap<String, Boolean>>() {
		}.getType();
		HashMap<String, Boolean> l = new HashMap<String, Boolean>();
		l = gson.fromJson(dead.toString(), list);
		deadlist = l;
	}

	// get other replica managers ips
	public Set<String> getOtherRMips() {
		lock.readLock().lock();
		Set<String> s = clock.keySet();
		lock.readLock().unlock();
		return s;
	}

	// add vector is done when adding a new BE
	public void addNewVectorClock(String ip, int number) {
		lock.writeLock().lock();
		clock.put(ip, number);
		lock.writeLock().unlock();
	}

	public JSONObject getVectorClock() {
		lock.readLock().lock();
		JSONObject o = new JSONObject(clock);
		lock.readLock().unlock();
		return o;
	}
	
	
	//HashMap<String, HashMap<Integer, Tweets>> table
	public JSONObject getLogTable() {
		lock.readLock().lock();
		JSONObject o = new JSONObject();
		ArrayList<JSONObject> ip = new ArrayList<JSONObject>();
		for(String s: table.keySet()){
			for(Integer i: table.get(s).keySet()){
				try {
					JSONObject item = new JSONObject().put("no", i).put("vc",table.get(s).get(i).getClock() ).put("tweet", table.get(s).get(i).getTweet());
					ip.add(item);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			try {
				o.put(s, ip);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		lock.readLock().unlock();
		return o;
	}

	// gets only the update of specified ip and update number. Null if not
	// there.
	public JSONObject getUpdate(String ip, int upno) {
		lock.readLock().lock();
		Tweets l = null;
		JSONObject n = new JSONObject();
		HashMap<Integer, Tweets> updates = table.get(ip);
		if (updates != null) {
			l = updates.get(upno);
		}
		lock.readLock().unlock();
		try {
			n.put("clock", l.getClock()).put("tweet", l.getTweet()).put("user", l.getUserName()).put("flag", l.getflag());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return n;
	}

	// log update for the specified ip, update number and update if its not
	// already there.
	public void logUpdate(String ip, JSONObject vc, int upnumber, String update, boolean flag, String uname, boolean deleteFlag) {
		if(flag){
			addNewVectorClock(ip, upnumber);
		}
		lock.writeLock().lock();
		HashMap<Integer, Tweets> updates = table.get(ip);
		if (updates != null) {
			Tweets up = updates.get(upnumber);
			if (up == null) {
				updates.put(upnumber,new Tweets(vc,update,uname, deleteFlag));
			}
		} else {
			HashMap<Integer, Tweets> val = new HashMap<Integer, Tweets>();
			val.put(upnumber,new Tweets(vc,update,uname, deleteFlag));
			table.put(ip, val);
		}
		lock.writeLock().unlock();
	}

	public JSONObject getdeadlist() {
		lock.readLock().lock();
		JSONObject d = new JSONObject(deadlist);
		lock.readLock().unlock();
		return d;
	}

	// removing the logs
	public void removeLogs(String s, Integer integer) {
		lock.writeLock().lock();
		if (table.containsKey(s)) {
			HashMap<Integer, Tweets> n = table.get(s);
			for (int i = integer; i >= 1; i--) {
				if (n.containsKey(i)) {
					n.remove(i);
				} else {
					break;
				}
			}
		}
		lock.writeLock().unlock();
	}

	public boolean isLesser(JSONObject clock2) {
		boolean f = true;
		lock.readLock().lock();
		if (clock2.length() > clock.size()) {
			f = false;
		} else {
			for (String s : JSONObject.getNames(clock2)) {
				try {
					if (clock2.getInt(s) > clock.get(s)) {
						f = false;
						break;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		lock.readLock().unlock();
		return f;
	}

}
