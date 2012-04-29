package Data;

import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class Tweets implements Comparable<Tweets> {
	private JSONObject clock;
	private String tweet;
	private String user;
	private boolean post;

	public Tweets(JSONObject c, String s, String uname, boolean val) {
		this.clock = c;
		this.tweet = s;
		this.user = uname;
		this.post = val;
		
	}

	public int compareTo(Tweets arg) {
		try {
		if (isLesser(arg.clock)) {
			return 1;
		}
		if (isGreater(arg.clock)) {
			return -1;
		}
		
		} catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	
	public JSONObject getClock() {
		return this.clock;
	}
	
	public String getUserName() {
		return this.user;
	}
	
	public boolean getflag() {
		return this.post;
	}

	public String getTweet() {
		return this.tweet;
	}


	public boolean isLesser(JSONObject clock2) throws JSONException {
		boolean isLesser = true;
		ArrayList<String> common = new ArrayList<String>();
		if (this.clock.length() > 0) {
			for (String s : JSONObject.getNames(this.clock)) {
					common.add(s);
			}
		}
		if (clock2.length() > 0) {
			for (String s : JSONObject.getNames(clock2)) {
				common.add(s);
			}
		}
		for (String server : common) {
			int current = 0;
			int incoming = 0;
			if (clock.has(server)) {
				current = clock.getInt(server);
			}
			if (clock2.has(server)) {
				incoming = clock2.getInt(server);
		    }
			if (!(current <= incoming)) {
				isLesser = false;
				break;
			}
		}
		return isLesser;
	}
	
	public boolean isGreater(JSONObject clock2) throws JSONException {
		boolean isGreater = true;
		ArrayList<String> common = new ArrayList<String>();
		if (this.clock.length() > 0) {
			for (String s : JSONObject.getNames(this.clock)) {
					common.add(s);
			}
		}
		if (clock2.length() > 0) {
			for (String s : JSONObject.getNames(clock2)) {
				common.add(s);
			}
		}
		for (String server : common) {
			int current = 0;
			int incoming = 0;
			if (clock.has(server)) {
				current = clock.getInt(server);
			}
			if (clock2.has(server)) {
				incoming = clock2.getInt(server);
		    }
			if (!(current >= incoming)) {
				isGreater = false;
				break;
			}
		}
		return isGreater;
	}

/*	public static void main(String args[]){
		ArrayList<Tweets> n = new ArrayList<Tweets>();
		try {
			n.add(new Tweets(new JSONObject().put("ip1",6).put("ip2",6).put("ip3",6), "Tweet1 for this clock", "user", true));
			n.add(new Tweets(new JSONObject().put("ip1",3).put("ip2",6).put("ip3",6), "Tweet1 for this clock", "user", true));
			n.add(new Tweets(new JSONObject().put("ip1",6).put("ip2",4).put("ip3",6), "Tweet1 for this clock", "user", true));
			n.add(new Tweets(new JSONObject().put("ip1",6).put("ip2",6).put("ip3",1), "Tweet1 for this clock", "user", true));
			
		Collections.reverse(n);
		for(Tweets s : n){
			System.out.println(s.getClock());
		}
		System.out.println(new JSONArray(n).get(0).toString());
		System.out.println(new JSONObject(new JSONArray(n).get(0).toString()).get("clock"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/
}
