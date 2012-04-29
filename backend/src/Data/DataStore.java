package Data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DataStore {
	private static final DataStore INSTANCE = new DataStore();

	private HashMap<String, ArrayList<Tweets>> tweets;
	private HashMap<String, Integer> vect;
	private HashMap<String,ArrayList<Tweets>> users;
	private int version;
	private VectorTable vt;
	private ReadWriteLock lock;

	private DataStore() {
		this.tweets = new HashMap<String, ArrayList<Tweets>>();
		this.version = 0;
		this.vt = VectorTable.getInstance();
		this.users = new HashMap<String,ArrayList<Tweets>>();
		this.vect = new HashMap<String, Integer>();
		this.lock = new ReentrantReadWriteLock();
	}

	public static DataStore getInstance() {
		return INSTANCE;
	}

	public void updateVersion() {
		version++;
	}

	public void setupDS(JSONObject data) {
		if (data.length() > 0) {
			System.out.println("DATA-->>>>>"+data);
			HashMap<String, ArrayList<Tweets>> temp = new HashMap<String, ArrayList<Tweets>>();
			for (String Tag : JSONObject.getNames(data)) {
				ArrayList<Tweets> l = new ArrayList<Tweets>();
				try {
					JSONArray arr = data.getJSONArray(Tag);
					if (arr != null) {
						for (int i = 0; i < arr.length(); i++) {
							JSONObject j = arr.getJSONObject(i);
							l.add(new Tweets(j.getJSONObject("vc"),j.getString("tweet"),j.getString("user"),j.getBoolean("flag")));
						}
					}
				} catch (JSONException e) {
					System.out.println("JSONException in bootstrap");
					e.printStackTrace();
				}
				temp.put(Tag, l);
			}
			tweets = temp;
		}	
	}
	
	public void setupUserlogs(JSONObject data){
		if (data.length() > 0) {
			System.out.println("DATA-->>>>>"+data);
			HashMap<String, ArrayList<Tweets>> temp = new HashMap<String, ArrayList<Tweets>>();
			for (String Tag : JSONObject.getNames(data)) {
				ArrayList<Tweets> l = new ArrayList<Tweets>();
				try {
					JSONArray arr = data.getJSONArray(Tag);
					if (arr != null) {
						for (int i = 0; i < arr.length(); i++) {
							JSONObject j = arr.getJSONObject(i);
							l.add(new Tweets(j.getJSONObject("vc"),j.getString("tweet"),j.getString("user"), j.getBoolean("flag")));
						}
					}
				} catch (JSONException e) {
					System.out.println("JSONException in bootstrap");
					e.printStackTrace();
				}
				temp.put(Tag, l);
			}
			users = temp;
		}	
	}
	
	public void addClockforEachtag(String key){
		if(vect.containsKey(key)){
			int current = vect.get(key);
			vect.put(key,current+1);
		}else{
			vect.put(key,1);
		}
	}

	private void delete(String tag, Tweets Value) {
		ArrayList<Tweets> list = tweets.get(tag);
		Iterator<Tweets> l = list.iterator();
		while(l.hasNext()){
			Tweets s = l.next();
			if(s.getClock().toString().equals(Value.getClock().toString())){
				l.remove();
				break;
			}
		}	
	}
	
	public void add(String key, Tweets Value) {
		ArrayList<Tweets> list = tweets.get(key);
		if (list != null) {
			list.add(Value);
		} else {
			ArrayList<Tweets> val = new ArrayList<Tweets>();
			val.add(Value);
			tweets.put(key, val);
		}
	}
	

	public void addUserPosts(String uname, Tweets twt){
		ArrayList<Tweets> list = users.get(uname);
		if (list != null) {
			list.add(twt);
		} else {
			ArrayList<Tweets> val = new ArrayList<Tweets>();
			val.add(twt);
			users.put(uname, val);
		}
	}
	
	private void deleteUserPost(String uname, Tweets val) {
		ArrayList<Tweets> list = users.get(uname);
		Iterator<Tweets> l = list.iterator();
		while(l.hasNext()){
			Tweets s = l.next();
			if(s.getClock().toString().equals(val.getClock().toString())){
				l.remove();
				break;
			}
		}	
	}

	public JSONObject addTweet(String ip, JSONObject vec, String tweet, boolean flag, String uname) {
		lock.writeLock().lock();
		String Tweet = " " + tweet;
		Pattern p = Pattern.compile("(?:\\s|\\A)[#](\\w+)");
		Matcher m = p.matcher(Tweet);
		int ver =0;
		if (flag) {
			updateVersion();
			ver = this.version;
			vt.addNewVectorClock(ip, ver);
			vec = vt.getVectorClock();
		}
		
		JSONObject vc = new JSONObject();
		try {
			vc.put("clock", vec).put("version", ver).put("user", uname);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		while (m.find()) {
			String tag = m.group().substring(2).toLowerCase();
			add(tag, new Tweets(vec, tweet, uname, true));
			addClockforEachtag(tag);
		}
		addUserPosts(uname,  new Tweets(vec, tweet, uname, true));
		lock.writeLock().unlock();
		return vc;
	}
	
	public JSONObject deleteTweet(String ip, JSONObject vec, String tweet, boolean flag, String uname, JSONObject tclock) {
		lock.writeLock().lock();
		String Tweet = " " + tweet;
		Pattern p = Pattern.compile("(?:\\s|\\A)[#](\\w+)");
		Matcher m = p.matcher(Tweet);
		int ver =0;
		if (flag) {
			updateVersion();
			ver = this.version;
			vt.addNewVectorClock(ip, ver);
			vec = vt.getVectorClock();
		}
		
		JSONObject vc = new JSONObject();
		try {
			vc.put("clock", vec).put("version", ver).put("user", uname);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		while (m.find()) {
			String tag = m.group().substring(2).toLowerCase();
			delete(tag, new Tweets(tclock, tweet, uname, true));  // check this method for proper removal
			addClockforEachtag(tag);
		}
		deleteUserPost(uname,  new Tweets(tclock, tweet, uname, true));   
		lock.writeLock().unlock();
		return vc;
		
	}

	public JSONObject getVersion() throws JSONException {
		lock.readLock().lock();
		JSONObject ver = new JSONObject().put("version", this.version);
		lock.readLock().unlock();
		return ver;
	}

	public int myversion() {
		lock.readLock().lock();
		int ver = this.version;
		lock.readLock().unlock();
		return ver;
	}
	

	public JSONArray getTweets(String key) throws JSONException {
		lock.readLock().lock();
		ArrayList<Tweets> list = tweets.get(key);
		lock.readLock().unlock();
		if (list != null){
			Collections.reverse(list);
			JSONArray ordered = new JSONArray();
			for(Tweets s : list){
				ordered.put(new JSONObject().put("tweet", s.getTweet()).put("username",s.getUserName()).put("tclock",s.getClock().toString()));
			}
			return ordered;
		} else {
			ArrayList<String> l = new ArrayList<String>();
			l.add("NO TWEETS FOUND FOR THIS QUERY");
			return new JSONArray(l);
		}
	}
	

	@SuppressWarnings("unused")
	public JSONArray getUserTweets(String uname) throws JSONException {
		lock.readLock().lock();
		String user[] = uname.split(",");
		ArrayList<Tweets> list = new ArrayList<Tweets>();
		for(String s:user){
			if(users.containsKey(s)){
				list.addAll(users.get(s));
			}
		}		
		lock.readLock().unlock();
		if (list != null){
			Collections.reverse(list);
			JSONArray ordered = new JSONArray();
			for(Tweets s : list){
				ordered.put(new JSONObject().put("tweet", s.getTweet()).put("username",s.getUserName()).put("tclock",s.getClock().toString()));
			}
			return ordered;
		} else {
			ArrayList<String> l = new ArrayList<String>();
			l.add("NO TWEETS FOUND, PLEASE TRY POSTING FISRT");
			return new JSONArray(l);
		}
	}


	//HashMap<String, ArrayList<Tweets>> tweets
	public JSONObject getDataStore() {
		JSONObject d = new JSONObject();
		lock.readLock().lock();
		for(String s: tweets.keySet()){
			ArrayList<JSONObject> t = new ArrayList<JSONObject>();
			ArrayList<Tweets> h = tweets.get(s);
			for(int i = 0; i< h.size(); i++){
				try {
					t.add(new JSONObject().put("vc",h.get(i).getClock()).put("tweet", h.get(i).getTweet()).put("user", h.get(i).getUserName()).put("flag",h.get(i).getflag()));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			try {
				d.put(s, t);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		lock.readLock().unlock();
		return d;
	}
	
	public JSONObject getUserlogs() {
		JSONObject d = new JSONObject();
		lock.readLock().lock();
		for(String s: users.keySet()){
			ArrayList<JSONObject> t = new ArrayList<JSONObject>();
			ArrayList<Tweets> h = users.get(s);
			for(int i = 0; i< h.size(); i++){
				try {
					t.add(new JSONObject().put("vc",h.get(i).getClock()).put("tweet", h.get(i).getTweet()).put("user", h.get(i).getUserName()).put("flag",h.get(i).getflag()));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			try {
				d.put(s, t);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		lock.readLock().unlock();
		return d;
	}

	public int queryVersion(String tag) {
		int tagver = -1;
		lock.readLock().lock();
		if(vect.containsKey(tag)){
			tagver = vect.get(tag);
		}
		lock.readLock().unlock();
		return tagver;
	}

	public JSONObject getTagversion() {
		lock.readLock().lock();
		JSONObject d = new JSONObject(vect);
		lock.readLock().unlock();
		return d;
	}
	
	public void setTagversion(JSONObject vect2) {
		Gson gson = new Gson();
		Type list = new TypeToken<HashMap<String, Integer>>() {}.getType();
		HashMap<String, Integer> c = new HashMap<String, Integer>();
		c = gson.fromJson(vect2.toString(), list);
		vect = c;
	}
}
