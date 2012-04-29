package common;

public final class Constants {

	public static final String EMPTY_STRING = "";
	public static final String SPACE = " ";
	public static final String NEWLINE = "\r\n";

	public static final String HTTP_VERSION = "HTTP/1.1";
	public static final String HTTP_VERSIONNOTSUPPORTED = "HTTP/1.1 505 HTTP Version Not Supported\r\n";
	public static final String HTTP_NOTIMPLEMENTED = "HTTP/1.1 501 Not Implemented\r\n";
	public static final String HTTP_OK = "HTTP/1.1 200 OK\r\n";
	public static final String HTTP_NOCONTENT = "HTTP/1.1 204 No Content\r\n";
	public static final String HTTP_SERVERERROR = "HTTP/1.1 500 Internal server error\r\n";
	public static final String HTTP_NOTFOUND = "HTTP/1.1 404 Not Found\r\n";
	public static final String HTTP_BADREQUEST = "HTTP/1.1 400 Bad Request\r\n";
	public static final String HTTP_UNAUTH = "HTTP/1.1 401 Unauthorized\r\n";
	
	public static final String GET = "GET";
	public static final String POST = "POST";
	public static final String PUT = "PUT";
	public static final String OPTIONS = "OPTIONS";
	public static final String DELETE = "DELETE";
	public static final String SEARCH = "/search?q=";
	public static final String POSTS = "/status/update";
	public static final String DELETES = "/remove?tweet=";
	public static final String POSTSTATUS = "?status=";
	public static final String POSTDATA = "/status/update?status=";
	public static final String GETVERSION = "/getversion";
	public static final String CONTENTLENGTH = "content-length: ";
	public static final String STOP = "STOP";
	public static final String DATA = "DATA";
	public static final String HTTP = "HTTP";
	
	public static final String GOSSIP = "/gossip?v=";
	public static final String HEARTBEAT = "/heartbeat";
	public static final String BOOTSTRAPDATA = "/bootstrapdata";
	
	public static final String BADGET = "BADGET";
	public static final String INCORRECT = "INCORRECT";
	public static final String PASSWORD = "/shutdown?p=";
	public static final String BOOTSTRAPHTTP ="/bootstraphttp";
	public static final String USERTWEETS = "/gettweets?username=";
	

	private Constants() {
		throw new AssertionError(
				"Final Constants Class. Objects can not be created");
	}
}
