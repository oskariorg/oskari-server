package fi.nls.oskari.printout.input.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrintoutContent {

	final Map<String, PrintoutContentStyle> styles;

	final List<PrintoutContentPart> parts;

	public PrintoutContent() {
		styles = new HashMap<String,PrintoutContentStyle>();
		parts = new ArrayList<PrintoutContentPart>();
	}

	public Map<String, PrintoutContentStyle> getStyles() {
		return styles;
	}

	public List<PrintoutContentPart> getParts() {
		return parts;
	}

}
