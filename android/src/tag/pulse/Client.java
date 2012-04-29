package tag.pulse;
import java.net.*;
import java.io.*;
import java.net.URLEncoder;
import java.lang.Math;

public class Client {
	Socket cSocket = null;
	BufferedReader in = null;
	PrintWriter out = null;
	BufferedReader consoleIn = null;
	ConfigReader cr = ConfigReader.getInstance();
	public String request; 
	
	public Client(){
		
	}
	public Client(String request){		
		this.request = request;
	}	
	public void PerformTests() {
		try {		
			
			consoleIn = new BufferedReader(new InputStreamReader(System.in));
			String consoleStr = "";
			while (!(consoleStr = consoleIn.readLine()).toLowerCase().equals(
					"stop")) {
				System.out.println("Runnning Case : " + consoleStr);
				switch (Integer.parseInt(consoleStr)) {
				// Correctly formatted POST Requests
				case 3:
					request = "GET  /search/q=Test  " + GlobalConstants.HTTPVersion + GlobalConstants.CRLF;
					executeRequest(request);
					request = "POST /status/update ?status =wrong+Format+POST " + GlobalConstants.HTTPVersion + GlobalConstants.CRLF;
					executeRequest(request);
					break;
				case 13:
					System.out.println("Please enter the username clock and tweet to delete: ");
					String tweetStr = consoleIn.readLine();
					request = "DELETE /remove?tweet=" + URLEncoder.encode(tweetStr, "UTF-8") + " "+ GlobalConstants.HTTPVersion + GlobalConstants.CRLF;
					executeRequest(request);					
					break;
					
				case 14:					
					System.out.println("Please enter the password to shutdown: ");
					String pass = consoleIn.readLine();
					request = "GET /shutdown?p=" + URLEncoder.encode(pass, "UTF-8") + " "+ GlobalConstants.HTTPVersion+GlobalConstants.CRLF;
					executeRequest(request);					
					break;
					
				case 15 :
					System.out.println("Please enter the name of the user: ");
					String user = consoleIn.readLine();
					request = "GET /gettweets?username=" + URLEncoder.encode(user, "UTF-8") + " "+ GlobalConstants.HTTPVersion+GlobalConstants.CRLF;
					executeRequest(request);					
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void sendMessage(String msg) {
		System.out.println("Requesting Server:-");
		System.out.println(msg);
		out.println(msg);
		out.flush();
	}

	public void initialize() {
		try {
			cSocket = new Socket("138.202.171.146", 7002); 
/*			System.out.println("Connected to " + cr.getHTTPServerIP()
					+ " in port " + cr.getHTTPServerPort());*/
			out = new PrintWriter(cSocket.getOutputStream());
			out.flush();
			in = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeAll() {
		try {
			out.close();
			in.close();
			cSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String receiveResponse() {
		String response = "";
		try {
			String temp;
			while ((temp = in.readLine()) != null) {
				if(temp.startsWith("{")){
					response = temp;
				}
			}
		} catch (IOException e) {
			response = "Error!\n" + e.getMessage();
		}
		System.out.println("Response from Server:-");
		return response;
	}


	public String deleteRequest(String tweetStr) {
		try {
			request = "DELETE /remove?tweet=" + URLEncoder.encode(tweetStr, "UTF-8") + " "
						+ GlobalConstants.HTTPVersion + GlobalConstants.CRLF;
			
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}
		return executeRequest(request);
	}
	

	public String postRequest(String tweetStr) {		
		try {
			String encodedTweet = URLEncoder.encode(tweetStr, "UTF-8");
			request = "POST /status/update?status=" + encodedTweet + " "
						+ GlobalConstants.HTTPVersion + GlobalConstants.CRLF;
	
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return executeRequest(request);
	}

	public String getRequest(String tag) {		
		try {
			request = "GET /search?q=" + URLEncoder.encode(tag, "UTF-8") + " "
					+ GlobalConstants.HTTPVersion + GlobalConstants.CRLF;			
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}
		return executeRequest(request);
	}
	
	public String getUserTimeline(String user) {		
		try {
			request = "GET /gettweets?username=" + URLEncoder.encode(user, "UTF-8") + " "+ GlobalConstants.HTTPVersion+GlobalConstants.CRLF;		
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}
		return executeRequest(request);
	}


	public String executeRequest(String request) {
		initialize();
		sendMessage(request);
		String result  = receiveResponse();
		System.out.println(result);
		closeAll();
		return result;
	}

}
