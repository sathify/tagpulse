package tag.pulse;

public class Tweet {
	public String user;
	public String tweet;
	public String clock;
	public String image;
	
	public Tweet(String uname, String twt, String ck){
		user = uname;
		clock = ck;
	    tweet = twt;
	}
	
	 	public String getUserName() {
	        return user;
	    }
	 	
	    public void setUserName(String orderName) {
	        this.user = orderName;
	    }
	    
	    public String getStatus() {
	        return tweet;
	    }
	    
	    public void setStatus(String Status) {
	        this.tweet = Status;
	    }
	    
	    public String getClock() {
	        return clock;
	    }
	    
	    public void setClock(String clk) {
	        this.clock = clk;
	    }
}
