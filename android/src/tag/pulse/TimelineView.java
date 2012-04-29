package tag.pulse;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TimelineView extends Activity {

	@SuppressWarnings("unused")
	private ProgressDialog m_ProgressDialog = null;
	private TweetAdapter m_adapter;
	private Runnable viewOrders;
	private String response;
	private ListView lview = null;
	private ArrayList<Tweet> results;
	private static final String PREFERENCE_FILE = "twitter_oauth.prefs";
	private SharedPreferences myPrefs;
	private Client c;

	/** Called when the activity is first created. */

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline);
		this.response = null;
		this.results = new ArrayList<Tweet>();
		this.c = new Client();
		lview = (ListView) findViewById(R.id.list);
		this.m_adapter = new TweetAdapter(this, R.layout.row, results);
		lview.setAdapter(m_adapter);
		myPrefs = this.getSharedPreferences(PREFERENCE_FILE,
				MODE_WORLD_WRITEABLE);

		// ListView l = (ListView) findViewById(R.id.list);
		registerForContextMenu(lview);

		viewOrders = new Runnable() {
			@Override
			public void run() {
				getOrders();
			}
		};
		Thread thread = new Thread(null, viewOrders, "background");
		thread.start();
		// m_ProgressDialog = ProgressDialog.show(TimelineView.this,
		// "Please wait..", "Retrieving data ..", true);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menucontext, menu);
	}

	public boolean onContextItemSelected(MenuItem item){
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Tweet data = m_adapter.getItem((int) info.id);
        switch(item.getItemId()){
        
        case R.id.repulse:
			
			if (!data.getUserName().equals(myPrefs.getString("MY_NAME", "UserName"))) {
				String inputValue = data.getStatus();
				try {
					JSONObject on = new JSONObject().put("username",
							myPrefs.getString("MY_NAME", "UserName")).put(
							"tweet", inputValue);
					c.postRequest(on.toString());
					Toast.makeText(this, "You replused it!", Toast.LENGTH_LONG).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}else{
				Toast.makeText(this, "This is your Pulse!", Toast.LENGTH_LONG).show();
			}
			break;
       
        case R.id.deleteContext:
           
            if(data.getUserName().equals(myPrefs.getString("MY_NAME", "UserName"))){
            	
            Toast.makeText(this, "Update"+ info.id+" Deleted" , Toast.LENGTH_LONG).show();
            m_adapter.notifyDataSetChanged();
            try {
				c.deleteRequest(new JSONObject().put("tclock",new JSONObject(data.getClock())).put("username", data.getUserName()).put("tweet",data.getStatus()).toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
            m_adapter.remove(data);  
            results.remove(data);
           // m_ProgressDialog.dismiss();
            m_adapter.notifyDataSetChanged();
            } else{
            	Toast.makeText(this, "You can only delete your Pulse!" , Toast.LENGTH_LONG).show();
            	
            }
            break;
        }
        return true;
    }

	public void onResume() {
		super.onResume();
		if (!myPrefs.contains("MY_NAME")) {
			Intent myIntent = new Intent(TimelineView.this, main.class);
			startActivity(myIntent);
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.profile:
			Intent pIntent = new Intent(TimelineView.this, MyProfile.class);
			startActivity(pIntent);
			break;
		case R.id.search:
			Intent myIntent = new Intent(TimelineView.this, Search.class);
			startActivity(myIntent);
			break;
		case R.id.timeline:
			Intent intent = new Intent(TimelineView.this, TimelineView.class);
			startActivity(intent);
			break;
		case R.id.update:
			Intent iintent = new Intent(TimelineView.this, Update.class);
			startActivity(iintent);
			break;
		case R.id.logout:
			Intent iiintent = new Intent(TimelineView.this, main.class);
			startActivity(iiintent);

			Editor ed = myPrefs.edit();
			ed.remove("MY_NAME");
			ed.commit();

			finish();
			break;
		}
		return true;
	}

	private Runnable returnRes = new Runnable() {
		@Override
		public void run() {
			if (results != null && results.size() > 0) {
				m_adapter.notifyDataSetChanged();
				for (int i = 0; i < results.size(); i++)
					m_adapter.add(results.get(i));
			}
			// m_ProgressDialog.dismiss();
			m_adapter.notifyDataSetChanged();
		}
	};

	public void click() {
		m_adapter.clear();
		results.clear();
		String user = myPrefs.getString("MY_NAME", "UserName");
		String friends = myPrefs.getString(user + "_FRIENDS", ",UserName");
		response = c.getUserTimeline(user + friends);
		for (int i = 0; i < 10; i++) {
			System.out.println(user + friends);
		}
		// response = c.getUserTimeline(user+",mattjcline");
	}

	private void getOrders() {
		click();
		try {
			JSONObject res = new JSONObject(response);
			JSONObject re = res.getJSONObject("tweets");
			JSONArray arr = re.getJSONArray("tweets");
			for (int i = 0; i < arr.length(); i++) {
				JSONObject n = arr.getJSONObject(i);
				Tweet one = new Tweet(n.getString("username"),
						n.getString("tweet"), n.getString("tclock"));
				results.add(one);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		runOnUiThread(returnRes);

	}

	private class TweetAdapter extends ArrayAdapter<Tweet> {

		private ArrayList<Tweet> items;

		public TweetAdapter(Context context, int textViewResourceId,
				ArrayList<Tweet> res) {
			super(context, textViewResourceId);
			this.items = res;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.row, null);
			}
			Tweet o = items.get(position);
			if (o != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				ImageView img = (ImageView) v.findViewById(R.id.icon);

				if (img != null) {
					String url = myPrefs.getString(
							o.getUserName() + "_PICTURE", "imgsrc");
					InputStream instream;
					try {
						instream = (InputStream) new URL(url).getContent();
						Drawable d = Drawable.createFromStream(instream,
								"src name");
						img.setImageDrawable(d);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (tt != null) {
					tt.setText("Name: " + o.getUserName());
				}
				if (bt != null) {
					bt.setText("Status: " + o.getStatus());
				}
			}
			return v;
		}
	}

}
