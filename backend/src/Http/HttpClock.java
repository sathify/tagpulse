package Http;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.ConfigReader;

public class HttpClock {

	private static final HttpClock INSTANCE = new HttpClock();

	private HashMap<String, Integer> clock;
	private HashMap<String, Boolean> deadlist;
	private ReadWriteLock lock;
	private ConfigReader cd;

	private HttpClock() {
		clock = new HashMap<String, Integer>();
		deadlist = new HashMap<String, Boolean>();
		cd = ConfigReader.getInstance();
		lock = new ReentrantReadWriteLock();
	}

	public static HttpClock getInstance() {
		return INSTANCE;
	}

	public JSONObject getClock() {
		lock.readLock().lock();
		JSONObject o = new JSONObject(clock);
		lock.readLock().unlock();
		return o;
	}

	public void UpdateClock(JSONObject vclock) {
		lock.writeLock().lock();
		Gson gson = new Gson();
		Type list = new TypeToken<HashMap<String, Integer>>() {}.getType();
		HashMap<String, Integer> c = new HashMap<String, Integer>();
		c = gson.fromJson(vclock.toString(), list);
		clock = c;
		lock.writeLock().unlock();
	}

	// setup dead list when starting the server
	public void setDeadlist(JSONObject dead) {
		Gson gson = new Gson();
		Type list = new TypeToken<HashMap<String, Boolean>>() {
		}.getType();
		HashMap<String, Boolean> l = new HashMap<String, Boolean>();
		l = gson.fromJson(dead.toString(), list);
		deadlist = l;
	}

	// setup dead list when starting the server
	public void setVectorclock(JSONObject vclock) {
		Gson gson = new Gson();
		Type list = new TypeToken<HashMap<String, Integer>>() {
		}.getType();
		HashMap<String, Integer> c = new HashMap<String, Integer>();
		c = gson.fromJson(vclock.toString(), list);
		clock = c;
	}

	public void addDeadServer(String s) {
		lock.writeLock().lock();
		deadlist.put(s, true);
		lock.writeLock().unlock();
	}

	public void setNewDataServer(String ip, int port) {
		ArrayList<String> availableServers = new ArrayList<String>();
		lock.readLock().lock();
		String newIP = cd.getDataip();
		int newPort = cd.getDataport();
		if (ip.equals(newIP) && port == newPort) {
			for (String in : clock.keySet()) {
				if (!deadlist.containsKey(in)) {
					availableServers.add(in);
				}
			}
			Random r = new Random();
			int randint = 0;
			if (availableServers.size() > 1) {
				randint = r.nextInt(availableServers.size() - 1);
			}
			if (!availableServers.isEmpty()) {
				String server = availableServers.get(randint);
				String[] item = server.split(",");
				cd.connectBE(Integer.parseInt(item[1]), item[0]);
			}
		}
		lock.readLock().unlock();
	}

}
