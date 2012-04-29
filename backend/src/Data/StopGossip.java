package Data;

public class StopGossip {

		private  GossipThread temp;
		
		private StopGossip(){}
		
	    private static final StopGossip myInstance = new StopGossip();
	    
		public void setInstance(GossipThread srvr) {
			temp = srvr;
		}
		
		public static StopGossip getInstance(){
			if(myInstance!= null){
				return myInstance;
			}
			return new StopGossip();
		}
		
		public void stop(){
			temp.stopGossiping();
		}	
}
