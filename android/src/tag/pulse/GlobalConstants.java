package tag.pulse;
public class GlobalConstants {
	
	public static final String CRLF = "\r\n";
	public static final String HTTPVersion = "HTTP/1.1";
	
	// HTTP Status codes
	public static final String OK = "200 OK";
	public static final String NoContent = "204 No Content";
	public static final String BadRequest = "400 Bad Request";
	public static final String NotFound = "404 Not Found";
	public static final String NotImplemented = "501 Not Implemented";
	public static final String HTTPVersionNotSupported = "505 HTTP Version Not Supported";
	public static final String InternalServerError = "500 Internal Server Error";
	
	// Builds Complete response header
	public static final String BuildHeader(String headerStr){
		return HTTPVersion+ " " + headerStr + CRLF;		
	}
}
