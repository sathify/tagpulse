package Http;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;


import common.Constants;
import common.RunnableProcessor;

public class RunnableHttpProcessor extends RunnableProcessor {

	private HttpHandler hp;

	public RunnableHttpProcessor(String LogFile, Socket csocket) {
		super(csocket);
		hp = new HttpHandler();
		this.LogFile = LogFile;
		getLogger(RunnableHttpProcessor.class);
	}
	
	public String processRequest(String request, int flag){
		String result = null;
		if(flag == 1){
			if (request.startsWith(Constants.SEARCH)) {
				String query;
				try {
					query = URLDecoder.decode(request.substring(Constants.SEARCH.length()), "UTF-8");
					String Json = hp.queryDataServer(query);
					if (Json != null) {
						result = printOKandJson(Json);
					} else {
						result = printInternalServerError();
					}
				} catch (UnsupportedEncodingException e) {
					System.out.println("URL EXCEPTION");
				}	
			}else if (request.startsWith(Constants.USERTWEETS)) {
				String query;
				try {
					query = URLDecoder.decode(request.substring(Constants.USERTWEETS.length()), "UTF-8");
					String Json = hp.getFromDataServer(query);
					if (Json != null) {
						result = printOKandJson(Json);
					} else {
						result = printInternalServerError();
					}
				} catch (UnsupportedEncodingException e) {
					System.out.println("URL EXCEPTION");
				}	
			}else if (request.startsWith(Constants.PASSWORD)) { 
				String check = request.substring(Constants.PASSWORD.length());
				if(check.equals(cs.isPassword())){
					result = printOkShutdown();
					super.shutdown = true;
					KillHeartBeat kill = KillHeartBeat.getInstance();
					kill.stop();
				} else {
					result = printUnAuthorized();
				}
			} else {
				result = printNotFoundError();
			}
		}else if(flag ==2){
			result = printPostdata(request);
		} else {
			if (request.startsWith(Constants.DELETES)) {
				try {
					String req = URLDecoder.decode(request.substring(Constants.DELETES.length()),"UTF-8");
					result = printDeletedata(req);
				} catch (UnsupportedEncodingException e) {
					System.out.println("Encoding exception");
				}
			}
		}
		return result;
	}
	public String printDeletedata(String post){
		System.out.println();
		if (hp.deleteTweet(post)) {
			return printNoContent();
		} else {
			return printInternalServerError();
		}		
	}
	
	public String printPostdata(String post){
		System.out.println();
		if (hp.postTweet(post)) {
			return printNoContent();
		} else {
			return printInternalServerError();
		}		
	}
	
}
