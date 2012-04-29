package common;

import Data.BootStrap;
import Data.GossipThread;
import Data.StopGossip;
import Data.VectorTable;
import Http.HeartBeat;
import Http.HttpClock;
import Http.HttpStartup;
import Http.KillHeartBeat;


public class Server {

	public static void main(String args[]) {
		MultithreadedServer st = null;
		BootStrap bs = null;
		HttpStartup hs = null;
		
		int Httpport = 0;
		int Dataport = 0;
		String Dataip = null;
		String LogFile = null;
		String Pass = null;
		int port = 0;
		
		ConfigReader cd = ConfigReader.getInstance();
		Password cs = Password.getInstance();
		StopServer s = StopServer.getInstance();
		VectorTable v = VectorTable.getInstance();
		HttpClock hc = HttpClock.getInstance();
		StopGossip stop = StopGossip.getInstance();
		KillHeartBeat kill = KillHeartBeat.getInstance();
		
		
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase(Constants.HTTP)) {
				Httpport = Integer.parseInt(args[1]);
				Dataport = Integer.parseInt(args[2]);				
				Dataip = args[3];
				LogFile = args[4];
				Pass = args[5];
				
				cd.setup(Httpport, Dataport, Dataip);
				cs.setup(Pass);
				
				hs = new HttpStartup();
				hs.setupHTTPServer();
				hc.setNewDataServer(Dataip, Dataport);
				
				st = new MultithreadedServer(LogFile, true);
				s.setInstance(st);
				
				
				Thread t = new Thread(st);
				t.start();	
				
				HeartBeat gp = new HeartBeat();
				kill.setInstance(gp);
				
				Thread h = new Thread(gp);
				h.start();
				
			} else if (args[0].equalsIgnoreCase(Constants.DATA)) {
				Dataport = Integer.parseInt(args[1]);
				Dataip =args[2];
				port = Integer.parseInt(args[3]);
				LogFile = args[4];
				Pass = args[5];
				
				cd.setup(Httpport, Dataport, Dataip);
				cs.setup(Pass);
				
				// adding the ip address and the port of the server which we will be connecting to
				v.addNewVectorClock(Dataip+","+port, 0);
				
				//if it is different from our ip address then we will add ours.
				System.out.println(Dataport+"____"+port);
				if(!(cd.getMyip()+","+Dataport).equals(Dataip+","+port)){
					bs = new BootStrap(port);
					bs.setupBEServer();
					
					System.out.println("Adding 2nd vector");
					System.out.println(cd.getMyip()+","+Dataport);
					v.addNewVectorClock(cd.getMyip()+","+Dataport, 0);
				}
				
				
				st = new MultithreadedServer(LogFile, false);
				s.setInstance(st);
				
				Thread t = new Thread(st);
				t.start();
				
				GossipThread gp = new GossipThread(LogFile);
				stop.setInstance(gp);
				
				Thread g = new Thread(gp);
				g.start();
				
			} else {
				System.out.println("Options: HTTP/DATA");
			}
		} else {
			System.out.println("Options: HTTP/DATA");
		}
	}
}
