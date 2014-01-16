package fi.nls.oskari.printout.input.geojson;

/*
 * This parses JSON with embedded GEOJSON using simple parser which was
 * used in Geotools geojson module. Would have used Jackson.
 * 
 * ParseMode and ParseContext change parsing from default key value
 * to GeoTools Feature and FeatureCollection parsing.
 * 
 * Mixed key value - geojson to featurecollection parsing allows for
 * embedded geojson to be processed as geotools features.
 * 
 * Parsing is sort of context aware. Context is checked against
 * parse context declarations and method of parsing is changed when 
 * needed from key-value to array or geojson parsing.
 * 
 * Note#1: Design choice
 * was based on attempt to reuse Geotools geojson parsing.
 * 
 * Note#2: Parsing has not been tested for efficiency. 
 * 
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geojson.DelegatingHandler;
import org.geotools.geojson.feature.AttributeIO;
import org.geotools.geojson.feature.DefaultAttributeIO;
import org.geotools.geojson.feature.FeatureHandler;
import org.geotools.geojson.feature.FeatureTypeAttributeIO;
import org.json.simple.parser.ParseException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class MaplinkGeoJsonHandler extends
		DelegatingHandler<Map<String, Object>> {

	boolean isDebug = false;

	Map<String, Object> root = null;
	Map<String, Object> current = null;
	Stack<Map<String, Object>> currentLists = new Stack<Map<String, Object>>();

	List<Map<String, Object>> currentArrayList;
	Stack<List<Map<String, Object>>> currentArrayLists = new Stack<List<Map<String, Object>>>();
	Stack<Object> currentValueList = new Stack<Object>();
	Object currentValue;

	String currentKey;
	Stack<String> currentKeyList = new Stack<String>();

	DefaultFeatureCollection currentFeatureCollection;

	SimpleFeatureType featureType;
	AttributeIO attio;

	Stack<String> pcStrStack = new Stack<String>();
	Stack<MapLinkGeoJsonParseContext> pcCtxStack = new Stack<MapLinkGeoJsonParseContext>();

	MapLinkGeoJsonParseContext pcSel = MapLinkGeoJsonParseContext.Default;

	long objid = 0;

	private ArrayList<Object> currentArrayValue;

	int level = 0;

	public MaplinkGeoJsonHandler() {
		this(null, null);
	}

	public MaplinkGeoJsonHandler(SimpleFeatureType featureType,
			AttributeIO attio) {

		if (attio == null) {
			if (featureType != null) {
				attio = new FeatureTypeAttributeIO(featureType);
			} else {
				attio = new DefaultAttributeIO();
			}
		}

		this.attio = attio;
	}

	void _lookupContext() {
		MapLinkGeoJsonParseContext pcPrev = pcSel;
		pcSel = MapLinkGeoJsonParseContext.Default;
		for (MapLinkGeoJsonParseContext pc : MapLinkGeoJsonParseContext
				.values()) {
			if (pc.partialMatch(pcStrStack)) {
				pcSel = pc;
				break;
			}
		}
		if (pcPrev != pcSel) {
			debugOutput("CHANGE CONTEXT " + pcPrev + " -> " + pcSel);
		}
	}

	private void debugOutput(String context) {
		if (!isDebug) {
			return;
		}

		StringBuffer buf = new StringBuffer();
		buf.append(__debugLevelStr.substring(0, currentLists.size() * 2));
		for (String s : pcStrStack) {
			buf.append("/" + s);
		}

		buf.append(" @" + context);
		System.out.println(buf);

	}

	
	public boolean endArray() throws ParseException, IOException {

		popContext("array");

		return super.endArray();
	}

	
	public void endJSON() throws ParseException, IOException {

	}

	
	public boolean endObject() throws ParseException, IOException {

		if (getContext() == MapLinkGeoJsonParseContext.Data_Feature
				&& getContext().match(pcStrStack)) {
			debugOutput("EXITING GeoJSON....");

			super.endObject();

			SimpleFeature f = ((FeatureHandler) delegate).getValue();
			if (f != null) {
				currentFeatureCollection.add(f);
				debugOutput("+ [" + f + "]");
			}

			delegate = UNINITIALIZED;

		} else {
			super.endObject();

		}

		currentValue = current;
		current = currentLists.peek();
		currentLists.pop();

		popContext("object");

		return true;

	}

	
	public boolean endObjectEntry() throws ParseException, IOException {

		if (getContext().getPm().buildMap) {
			if (currentArrayValue != null) {
				current.put(currentKey, currentArrayValue);
			} else if (currentArrayList != null) {

				debugOutput("<== (" + currentKey + ") ["
						+ currentArrayList.size() + "]");

				current.put(currentKey, currentArrayList);

			} else if (currentValue != null) {

				if (current == currentValue) {
					throw new IOException("CONFUSED");
				}

				debugOutput("<== (" + currentKey + ") " + currentValue);
				current.put(currentKey, currentValue);
			}
			currentArrayList = null;
			currentArrayValue = null;
			currentValue = null;
		}

		if (getContext() == MapLinkGeoJsonParseContext.Data_FeatureCollection
				&& getContext().match(pcStrStack)) {

			debugOutput(" CLOSING FEATURECOLLECTION at " + currentKey);
			if (currentFeatureCollection != null) {
				current.put("." + currentKey, currentFeatureCollection);
			}

			if (currentFeatureCollection != null
					&& currentFeatureCollection.getSchema() != null) {
				debugOutput(currentFeatureCollection.getSchema().toString());
			}

			currentFeatureCollection = null;

		}

		currentValue = currentValueList.peek();
		currentValueList.pop();

		currentArrayList = currentArrayLists.peek();
		currentArrayLists.pop();

		currentKey = currentKeyList.peek();
		currentKeyList.pop();

		popContext("objectEntry");

		return super.endObjectEntry();
	}

	MapLinkGeoJsonParseContext getContext() {
		return pcSel;
	}

	
	public Map<String, Object> getValue() {
		return root;
	}

	public boolean isDebug() {
		return isDebug;
	}

	void popContext(String ctx) {

		pcStrStack.pop();
		pcSel = pcCtxStack.peek();
		pcCtxStack.pop();

		level--;
	}

	
	public boolean primitive(Object value) throws ParseException, IOException {

		currentValue = value;

		if (currentArrayValue != null) {
			currentArrayValue.add(value);
			debugOutput("VALUE[" + currentArrayValue.size() + "] ==== "
					+ currentValue);
		} else {
			debugOutput("VALUE ==== " + currentValue);
		}
		return super.primitive(value);
	}

	void pushContext(String s, String ctx) {
		level++;

		pcStrStack.push(s);
		pcCtxStack.push(pcSel);
		_lookupContext();
	}

	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
	}

	
	public boolean startArray() throws ParseException, IOException {
		pushContext("[]", "array");

		currentArrayList = new ArrayList<Map<String, Object>>();

		return super.startArray();
	}

	
	public boolean startObject() throws ParseException, IOException {
		objid++;

		currentLists.push(current);
		current = null;

		String objname = "";
		if (currentArrayList != null) {
			objname += "#";
		}

		pushContext("{" + objname + "}", "object");

		if (getContext() == MapLinkGeoJsonParseContext.Data_Feature
				&& getContext().match(pcStrStack)) {
			debugOutput("ENTERING GeoJSON....");
			delegate = new FeatureHandler();
		} else {

			if (getContext().getPm().buildMap) {
				current = new HashMap<String, Object>();

				if (root == null) {
					debugOutput("Setting ROOT");
					root = current;
				}
			} else {
				debugOutput("NOT BUILDING MAP");
			}

			if (currentArrayList != null) {
				currentArrayList.add(current);
			}

		}

		return super.startObject();

	}

	
	public boolean startObjectEntry(String key) throws ParseException,
			IOException {

		String contextKey = "." + key;
		pushContext(contextKey, "objectEntry");

		currentKeyList.push(currentKey);
		currentKey = key;

		currentValueList.push(currentValue);
		currentValue = null;

		currentArrayLists.push(currentArrayList);
		currentArrayList = null;

		if (getContext() == MapLinkGeoJsonParseContext.Data_FeatureCollection
				&& getContext().match(pcStrStack)) {
			debugOutput("OPENING FeatureCollection for...." + getContext());

			DefaultFeatureCollection features = new DefaultFeatureCollection(
					null, null);
			currentFeatureCollection = features;
		} else if (getContext().getPm().array) {
			currentArrayValue = new ArrayList<Object>();
		}

		return super.startObjectEntry(key);
	}

	private static final String __debugLevelStr = "                                         "
			+ "                                         "
			+ "                                         "
			+ "                                         "
			+ "                                         "
			+ "                                         "
			+ "                                         "
			+ "                                         "
			+ "                                         "
			+ "                                         ";

}