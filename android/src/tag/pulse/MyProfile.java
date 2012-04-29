package tag.pulse;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import tag.pulse.R;
import tag.pulse.Search;
import tag.pulse.TimelineView;
import tag.pulse.Update;
import tag.pulse.main;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MyProfile extends Activity implements OnClickListener{
	private static final String PREFERENCE_FILE = "twitter_oauth.prefs";
	private SharedPreferences myPrefs;
	private TextView name;
	private TextView about;
	private TextView following;
	private EditText text;
	private Button add;
	private ImageView img ;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        name = (TextView)findViewById(R.id.TextView05);
        about = (TextView)findViewById(R.id.TextView03);
        following = (TextView)findViewById(R.id.TextView07);
        myPrefs = this.getSharedPreferences(PREFERENCE_FILE, MODE_WORLD_WRITEABLE);
        text = (EditText) findViewById(R.id.friend);
        add = (Button) findViewById(R.id.add);
        img = (ImageView)findViewById(R.id.proImg);
        add.setOnClickListener(this);
        create();
    }
    
    public void create(){  
         if(img != null){
         	String url = myPrefs.getString(myPrefs.getString("MY_NAME", "UserName")+"_PICTURE","imgsrc");
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
    	name.setText(myPrefs.getString("MY_NAME", "UserName"));
    	about.setText(myPrefs.getString(myPrefs.getString("MY_NAME", "UserName")+"_DETAILS", "Please write something about you so that other can know about you."));
    	following.setText(myPrefs.getString(myPrefs.getString("MY_NAME", "UserName")+"_FRIENDS", ""));
    }
    
    public void onResume() {
		super.onResume();
		if(!myPrefs.contains("MY_NAME")){
			Intent myIntent = new Intent(MyProfile.this, main.class);
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
			Intent pIntent = new Intent(MyProfile.this, MyProfile.class);
			startActivity(pIntent);
			break;
		case R.id.search:
			Intent myIntent = new Intent(MyProfile.this, Search.class);
			startActivity(myIntent);
			break;
		case R.id.timeline:
			Intent intent = new Intent(MyProfile.this, TimelineView.class);
			startActivity(intent);
			break;
		case R.id.update:
			Intent iintent = new Intent(MyProfile.this, Update.class);
			startActivity(iintent);
			break;
		case R.id.logout:
			Intent iiintent = new Intent(MyProfile.this, main.class);
			startActivity(iiintent);
			
			Editor ed = myPrefs.edit();
			ed.remove("MY_NAME");
			ed.commit();
			
			finish();
			break;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		if (text.getText().length() == 0) {
			Toast.makeText(this,"Please enter an update status", Toast.LENGTH_LONG).show();
			return;
		}
		String inputValue = text.getText().toString();
		Editor ed = myPrefs.edit();
		String name = myPrefs.getString("MY_NAME", "UserName");
		ed.putString(name+"_FRIENDS",","+inputValue);
		ed.commit();
		following.append(" "+inputValue);
		Toast.makeText(this,"You are following  "+inputValue, Toast.LENGTH_LONG).show();
		text.setText("");
	}

}
