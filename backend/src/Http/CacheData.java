package Http;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CacheData {
	private static final CacheData INSTANCE = new CacheData();

	private HashMap<String, Tweet> tweets;
	private ReadWriteLock lock;

	public static CacheData getInstance() {
		return INSTANCE;
	}

	private CacheData() {
		this.tweets = new HashMap<String, Tweet>();
		this.lock = new ReentrantReadWriteLock();
	}

	public void addLocal(String key, int version, String value) {

		lock.writeLock().lock();
		tweets.put(key, new Tweet(version, value));
		lock.writeLock().unlock();
	}

	public String getTweets(String Key) {
		lock.readLock().lock();
		Tweet tw = tweets.get(Key);
		String tweet = tw.getJson();
		lock.readLock().unlock();
		return tweet;
	}

	public boolean hasKey(String key) {
		lock.readLock().lock();
		boolean b = tweets.containsKey(key);
		lock.readLock().unlock();
		return b;
	}

	public int getVersion(String key) {
		int v = 0;
		lock.readLock().lock();
		Tweet tw = tweets.get(key);
		if (tw != null)
			v = tw.getVersion();
		lock.readLock().unlock();
		return v;
	}
}
