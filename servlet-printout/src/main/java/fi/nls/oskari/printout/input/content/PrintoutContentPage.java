package fi.nls.oskari.printout.input.content;

import java.util.ArrayList;
import java.util.List;

public class PrintoutContentPage {

	final List<PrintoutContentPart> parts;

	public PrintoutContentPage() {
		parts = new ArrayList<PrintoutContentPart>();
	}

	public List<PrintoutContentPart> getParts() {
		return parts;
	}
}
