package Data;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;

import org.json.JSONObject;
import common.Constants;
import common.RunnableProcessor;

public class RunnableDataProcessor extends RunnableProcessor {

	private DataHandler dh;

	
	public RunnableDataProcessor(String LogFile, Socket csocket) {
		super(csocket);
		dh = new DataHandler();
		this.LogFile = LogFile;
		getLogger(RunnableDataProcessor.class);
		
	}

	public String processRequest(String request, int flag) {
		String result = null;
		if (flag == 1) {
			if (request.startsWith(Constants.SEARCH)) {
				try {
					String json = URLDecoder.decode(request.substring(Constants.SEARCH.length()),"UTF-8");
					result = printOKandJson(dh.getTweets(json));
				} catch (UnsupportedEncodingException e) {
					System.out.println("Encoding exception");
				}
			}else if (request.startsWith(Constants.USERTWEETS)) {
				try {
					String json = URLDecoder.decode(request.substring(Constants.USERTWEETS.length()),"UTF-8");
					result = printOKandJson(dh.getUserTweets(json));
				} catch (UnsupportedEncodingException e) {
					System.out.println("Encoding exception");
				}
			}else if (request.startsWith(Constants.BOOTSTRAPDATA)) {
				result = printOKandJson(dh.bootStrapme());
			} else if (request.startsWith(Constants.HEARTBEAT)) {
				result = printOKandJson(dh.getClockforHttp());
			}else if (request.startsWith(Constants.BOOTSTRAPHTTP)) {
				result = printOKandJson(dh.bootStraphttp());
			}else if (request.startsWith(Constants.GOSSIP)) {
				try {
					String clock = URLDecoder.decode(request.substring(Constants.GOSSIP.length()),"UTF-8");
					result = printOKandJson(dh.getUpdates(clock));
				} catch (UnsupportedEncodingException e) {
					System.out.println("URL EXCEPTION");
				}
			} else if (request.startsWith(Constants.PASSWORD)) { 
				String check = request.substring(Constants.PASSWORD.length());
				if(check.equals(cs.isPassword())){
					result = printOkShutdown();
					super.shutdown = true;
					StopGossip s = StopGossip.getInstance();
					s.stop();
				} else {
					result = printUnAuthorized();
				}
			} else {
				result = printNotFoundError();
			}
		} else if(flag == 2) {
			JSONObject c = dh.post(request,getMyip());
			result = printNoContentWithResponce(c.toString());
		} else{
			if (request.startsWith(Constants.DELETES)) {
				try {
					String req = URLDecoder.decode(request.substring(Constants.DELETES.length()),"UTF-8");
					JSONObject c = dh.delete(req,getMyip());
					result = printNoContentWithResponce(c.toString());
				} catch (UnsupportedEncodingException e) {
					System.out.println("Encoding exception");
				}
			}
		}
		return result;
	}
	
}
