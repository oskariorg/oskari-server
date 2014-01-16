package fi.nls.oskari.printout.input.geojson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MaplinkGeoJsonParser {

	boolean isDebug = false;

	public MaplinkGeoJsonParser() {

	}

	public boolean isDebug() {
		return isDebug;
	}

	public Map<String, ?> parse(InputStream inp) throws IOException,
			ParseException {

		Map<String, Object> root = null;

		BufferedReader reader = new BufferedReader(new InputStreamReader(inp));

		JSONParser parser = new JSONParser();

		MaplinkGeoJsonHandler handler = new MaplinkGeoJsonHandler();
		handler.setDebug(isDebug);

		parser.parse(reader, handler, true);

		root = handler.getValue();

		return root;

	}

	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
	}

}
