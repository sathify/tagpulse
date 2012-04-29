package common;

import java.io.IOException;
import org.apache.log4j.Logger;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.PatternLayout;

public class CustomLogger {
	private static CustomLogger myInstance = null;
	private Logger l = null;
	private CustomLogger(){}
	
	@SuppressWarnings("rawtypes")
	private CustomLogger(Class className, String logfileName, boolean outputToConsole){
		l = Logger.getLogger(className);
		String pattern = "%d [%t] %-5p %c %x - %m%n";
		
		PatternLayout layout = new PatternLayout(pattern);
		FileAppender fAppender = null;
		try {
			fAppender = new FileAppender(layout, logfileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (outputToConsole) {
			BasicConfigurator.configure();
		}
		l.addAppender(fAppender);
	}
	
	@SuppressWarnings("rawtypes")
	public static Logger getCustomLogger(Class className, String logfileName, boolean outputToConsole) {
		 if(myInstance == null){
			 myInstance = new CustomLogger(className, logfileName, outputToConsole);
		 }		
		return myInstance.l;
	}
}
