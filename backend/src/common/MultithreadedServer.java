package common;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import Data.RunnableDataProcessor;
import Http.RunnableHttpProcessor;

public class MultithreadedServer implements Runnable {
	private int serverport;
	private ServerSocket serverSocket;
	private ExecutorService threadPool;
	private boolean stopped;
	private boolean Flag;
	private ConfigReader cr;
	private String LogFile;

	public MultithreadedServer(String LogFile, boolean flag) {
		this.cr = ConfigReader.getInstance();
		this.Flag = flag;
		this.serverport = port(Flag);
		this.serverSocket = null;
		this.threadPool = Executors.newCachedThreadPool();
		this.stopped = false;
		this.LogFile = LogFile;
	}


	private int port(boolean flag) {
		if (flag) {
			return cr.getHttpport();
		} else {
			return cr.getDataport();
		}
	}

	private void openServerSocket() {
		try {
			serverSocket = new ServerSocket(serverport);
		} catch (IOException e) {
			System.out.println("Issues Creating new Server socket");
			e.printStackTrace();
		}
	}

	public void run() {

		openServerSocket();
		while (!stopped) {
			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				System.out.println("Exception caused when accepting a client");
			}
			if (this.Flag) {
				threadPool.execute(new RunnableHttpProcessor(LogFile,
						clientSocket));
			} else {
				threadPool.execute(new RunnableDataProcessor(LogFile,
						clientSocket));
			}
		}
		this.threadPool.shutdown();
		try {
			this.threadPool.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			System.out.println("Executor Shutdown timedout!");
		}
		System.out.println("Shuttingdown executor complete.");
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			System.out.println("Issues closing ServerSocket");
		}
		System.out.println("Closing Server Socket...");
	}

	public void stopServer() {
		stopped = true;
		try {
			Socket s = new Socket("localhost", this.serverport);
			PrintWriter out = new PrintWriter(new OutputStreamWriter(
					s.getOutputStream()));
			out.println("Shut down server");
			out.close();
		} catch (UnknownHostException e) {
			System.out.println("Exception Shutting down");
		} catch (IOException e) {
			System.out.println("Exception Shutting down");
		}
		
	}

}
