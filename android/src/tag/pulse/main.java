
package tag.pulse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class main extends Activity {

	private static final String CONSUMER_KEY = "zSt0L3TKUhhVzO7s1Q6DzA";
	private static final String CONSUMER_SECRET = "enW71aIL9WzxRisQAJM5mcABnxjAaM0W1wFvf1EW0";

	private static String ACCESS_KEY = null;
	private static String ACCESS_SECRET = null;

	private static final String REQUEST_URL = "http://twitter.com/oauth/request_token";
	private static final String ACCESS_TOKEN_URL = "http://twitter.com/oauth/access_token";
	private static final String AUTH_URL = "http://twitter.com/oauth/authorize";
	private static final String CALLBACK_URL = "OauthTwitter://twitt";
	private static final String PREFERENCE_FILE = "twitter_oauth.prefs";

	private static CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
	private static CommonsHttpOAuthProvider provider = new CommonsHttpOAuthProvider(REQUEST_URL, ACCESS_TOKEN_URL, AUTH_URL);
	private Button btnLogin;
	public SharedPreferences myPrefs;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		myPrefs = this.getSharedPreferences(PREFERENCE_FILE, MODE_WORLD_WRITEABLE);
		btnLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					String authURL = provider.retrieveRequestToken(consumer,CALLBACK_URL);
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authURL)));
				} catch (OAuthMessageSignerException e) {
					e.printStackTrace();
				} catch (OAuthNotAuthorizedException e) {
					e.printStackTrace();
				} catch (OAuthExpectationFailedException e) {
					e.printStackTrace();
				} catch (OAuthCommunicationException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void onResume() {
		super.onResume();
		Uri uri = this.getIntent().getData();

		if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {

			String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
			try {

				provider.retrieveAccessToken(consumer, verifier);
				ACCESS_KEY = consumer.getToken();
				ACCESS_SECRET = consumer.getTokenSecret();
				
				System.out.println(ACCESS_KEY );
				System.out.println(ACCESS_SECRET);
				
				setupUserData();
			} catch (OAuthMessageSignerException e) {
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				e.printStackTrace();
			}
			Intent iiintent = new Intent(main.this,  MyProfile.class);
			startActivity(iiintent);
		}
	}
	
	public void setupUserData(){

		OAuthConsumer consumer = new DefaultOAuthConsumer(CONSUMER_KEY,
	             CONSUMER_SECRET);
	    consumer.setTokenWithSecret(ACCESS_KEY, ACCESS_SECRET);
	     try{
	  
	    	 URL url = new URL("http://api.twitter.com/1/account/verify_credentials.json");
	    	 HttpURLConnection request = (HttpURLConnection) url.openConnection();
	     
	    	 request.setRequestMethod("GET");
	    	 consumer.sign(request);

	    	 System.out.println("Sending post to Twitter...");
	    	 request.connect();

	    	 System.out.println("Response: " + request.getResponseCode());
	    	 BufferedReader instream = new BufferedReader(new InputStreamReader(request.getInputStream()));
	    	 String line = "";
	    	 String json = "";
	    	 while ((line = instream.readLine()) != null) {
	    			System.out.println(line);
	    			if(line.startsWith("{")){
	    				json = line;
	    			}
	    	 }
	    	instream.close();
	      JSONObject details = new JSONObject(json);  
	      System.out.println(json);
	      SharedPreferences.Editor editor = myPrefs.edit();
	      editor.putString("MY_NAME", details.getString("screen_name"));
	      editor.putString(details.getString("screen_name")+"_PICTURE", details.getString("profile_image_url"));
	      editor.putString(details.getString("screen_name")+"_DETAILS", details.getString("description"));
	      //editor.putString(details.getString("screen_name")+"_FRIENDS", "UserName");
	   
	      editor.commit(); 
	      
	     }catch(IOException e){
	    	 System.out.println("Input/output issues");
	     }catch( OAuthMessageSignerException e){
	    	 System.out.println("Authentication signature Failed");
	     }catch(OAuthExpectationFailedException e){
	    	 System.out.println("Authentication to Twitter Failed");
	     }
	     catch(OAuthCommunicationException e){
	    	 System.out.println("Communication to Twitter Failed");
	     }
	     catch(Exception e){
	    	 e.printStackTrace();
	     }
	 }
	

}