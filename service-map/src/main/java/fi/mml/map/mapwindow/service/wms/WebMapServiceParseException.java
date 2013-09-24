package fi.mml.map.mapwindow.service.wms;

/**
 * Exception should be thrown if xml of web map service cannot be parsed
 *
 */
public class WebMapServiceParseException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public WebMapServiceParseException(Exception e) {
		super(e);
	}
	
	public WebMapServiceParseException(String s) {
		super(s);
	}
}
