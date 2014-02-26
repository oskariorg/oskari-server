package fi.nls.oskari.printout.input.content;


public interface PrintoutContentPart {
	
	public void setStyle(final PrintoutContentStyle style);
	public PrintoutContentStyle getStyle();

	public PrintoutContentPartType getType() ;
	
}
