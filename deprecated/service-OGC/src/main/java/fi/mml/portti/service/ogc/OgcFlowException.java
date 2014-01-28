package fi.mml.portti.service.ogc;

public class OgcFlowException  extends Exception {


	private static final long serialVersionUID = -2427597919179478878L;
	private String mistake;

	public OgcFlowException() {
		super();
		mistake = "unknown";
	}
  
	public OgcFlowException(String err) {
		super(err); 
		mistake = err; 
	}
	
	public OgcFlowException(String err, Exception e) {
		super(err, e);
		mistake = err;
	}

	public String getError() {
		return mistake;
	}



}
