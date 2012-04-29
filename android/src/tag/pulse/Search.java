package tag.pulse;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Search extends ListActivity implements OnClickListener  {
	
	 @SuppressWarnings("unused")
	 private ProgressDialog m_ProgressDialog = null;
	 private TweetAdapter m_adapter;
	 private ArrayList<Tweet> results;
	 private Runnable viewOrders;
	 private EditText text;
	 private Button post;
	 private Client c;
	 String response;
	 private static final String PREFERENCE_FILE = "twitter_oauth.prefs";
	 private SharedPreferences myPrefs;
		  
	    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.search);
       this.results = new ArrayList<Tweet>();
       this.m_adapter = new TweetAdapter(this, R.layout.row, results);
       setListAdapter(this.m_adapter);
       c = new Client();
       myPrefs = this.getSharedPreferences(PREFERENCE_FILE, MODE_WORLD_WRITEABLE);
       response = null;
       
       text = (EditText) findViewById(R.id.EditText01);
       post = (Button) findViewById(R.id.Button01);
       post.setOnClickListener(this);
       
       viewOrders = new Runnable(){
           @Override
           public void run() {
               getOrders();
           }
       };
       //m_ProgressDialog = ProgressDialog.show(Search.this, "Please wait..", "Retrieving data ..", true);
   }
    
    
    
    public void onResume() {
		super.onResume();
		if(!myPrefs.contains("MY_NAME")){
			Intent myIntent = new Intent(Search.this, main.class);
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
			Intent pIntent = new Intent(Search.this, MyProfile.class);
			startActivity(pIntent);
			break;
		case R.id.search:
			Intent myIntent = new Intent(Search.this, Search.class);
			startActivity(myIntent);
			break;
		case R.id.timeline:
			Intent intent = new Intent(Search.this, TimelineView.class);
			startActivity(intent);
			break;
		case R.id.update:
			Intent iintent = new Intent(Search.this, Update.class);
			startActivity(iintent);
			break;
		case R.id.logout:
			Intent iiintent = new Intent(Search.this, main.class);
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
        	if(results != null && results .size() > 0){
                m_adapter.notifyDataSetChanged();
                for(int i=0;i<results .size();i++)
                m_adapter.add(results .get(i));
            }
            //m_ProgressDialog.dismiss();
            m_adapter.notifyDataSetChanged();	
        }
    };
    
    //search button click
    public void onClick(View view) {
		    m_adapter.clear();
		    results.clear();
			if (text.getText().length() == 0) {
				Toast.makeText(this,"Please enter a keyword", Toast.LENGTH_LONG).show();
				return;
			}
	        String query = text.getText().toString();
	        response = c.getRequest(query);
	        Thread thread =  new Thread(null, viewOrders, "background");
	        thread.start();
	}

    
    private void getOrders() {
		try {
			JSONObject res = new JSONObject(response);
			JSONObject re = res.getJSONObject("tweets");
			JSONArray arr = re.getJSONArray("tweet");
			
			for(int i= 0;i < arr.length();i++){
				JSONObject n = arr.getJSONObject(i);
				Tweet one = new Tweet(n.getString("username"), n.getString("tweet"), n.getString("tclock"));
				results.add(one); 
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		runOnUiThread(returnRes);
	}

    private class TweetAdapter extends ArrayAdapter<Tweet>{
    	
   	 		private ArrayList<Tweet> items;

	        public TweetAdapter(Context context, int textViewResourceId, ArrayList<Tweet> res) {
	                super(context, textViewResourceId);
	                this.items = res;
	        }
	       
	        public View getView(int position, View convertView, ViewGroup parent) {
	                View v = convertView;
	                if (v == null) {
	                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                    v = vi.inflate(R.layout.row, null);
	                }
	                Tweet o = items.get(position);
	                if (o != null) {
	                        TextView tt = (TextView) v.findViewById(R.id.toptext);
	                        TextView bt = (TextView) v.findViewById(R.id.bottomtext);
	                        ImageView img = (ImageView) v.findViewById(R.id.icon);
	                        
	                        if(img != null){
	                        	String url = myPrefs.getString(o.getUserName()+"_PICTURE","imgsrc");
	                        	InputStream instream;
								try {
									instream = (InputStream) new URL(url).getContent();
									Drawable d = Drawable.createFromStream(instream, "src name");
		                        	img.setImageDrawable(d);
								} catch (MalformedURLException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								} 	
	                        }
	                        if (tt != null) {
	                              tt.setText("Name: "+o.getUserName());                            }
	                        if(bt != null){
	                              bt.setText("Status: "+ o.getStatus());
	                        }
	                }
	                return v;
	        }
   }

}

