package common;

public class Password {
	
	private static final Password INSTANCE = new Password();
	private String pass;
	
	public static Password getInstance() {
		return INSTANCE;
	}
	
	private Password(){
	}
	
	public void setup(String password){
		this.pass = password;
	}
	
	public String isPassword(){
		return pass;
	}
}
