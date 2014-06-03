package fi.nls.oskari.fe.input;

import org.codehaus.staxmate.in.SMInputCursor;

public interface XMLInputProcessor extends InputProcessor {

	public SMInputCursor root();

}
