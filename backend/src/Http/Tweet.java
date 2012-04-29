package Http;

public class Tweet {
	private int version;
	private String json;

	public Tweet(int ver, String son) {
		this.version = ver;
		this.json = son;
	}

	public void setVersion(int ver) {
		this.version = ver;
	}
	
	public void setJson(String son){
		this.json = son;
	}

	public String getJson() {
		return this.json;
	}
	
	public int getVersion(){
		return this.version;
	}
}
