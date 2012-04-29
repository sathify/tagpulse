package tag.pulse;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Update extends Activity implements OnClickListener {
	private EditText text;
	private Button post;
	private Client c;
	private static final String PREFERENCE_FILE = "twitter_oauth.prefs";
	private SharedPreferences myPrefs;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post);
        text = (EditText) findViewById(R.id.textStatus);
        post = (Button) findViewById(R.id.buttonUpdate);
        myPrefs = this.getSharedPreferences(PREFERENCE_FILE, MODE_WORLD_WRITEABLE);
        post.setOnClickListener(this);
        c= new Client();
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
    

    public void onResume() {
		super.onResume();
		if(!myPrefs.contains("MY_NAME")){
			Intent myIntent = new Intent(Update.this, main.class);
			startActivity(myIntent);
		}
    }
    
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.profile:
			Intent pIntent = new Intent(Update.this, MyProfile.class);
			startActivity(pIntent);
			break;
		case R.id.search:
			Intent myIntent = new Intent(Update.this, Search.class);
			startActivity(myIntent);
			break;
		case R.id.timeline:
			Intent intent = new Intent(Update.this, TimelineView.class);
			startActivity(intent);
			break;
		case R.id.update:
			Intent iintent = new Intent(Update.this, Update.class);
			startActivity(iintent);
			break;
		case R.id.logout:
			Intent iiintent = new Intent(Update.this, main.class);
			startActivity(iiintent);
			
			Editor ed = myPrefs.edit();
			ed.remove("MY_NAME");
			ed.commit();
			
			finish();
			break;
		}
		return true;
	}
    
    //search button click
    public void onClick(View view) {
			if (text.getText().length() == 0) {
				Toast.makeText(this,"Please enter an update status", Toast.LENGTH_LONG).show();
				return;
			}
			String inputValue = text.getText().toString();
			try {
				JSONObject on = new JSONObject().put("username",myPrefs.getString("MY_NAME","UserName")).put("tweet",inputValue);
		    c.postRequest(on.toString());
		    Toast.makeText(this,"Your update was posted!", Toast.LENGTH_LONG).show();
		    text.setText("");
			} catch (JSONException e) {
				e.printStackTrace();
			}
	}

}