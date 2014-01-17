package fi.nls.oskari.printout.input.geojson;

import java.util.Stack;

/**
 * 
 * This class is used to specify parse mode for mixed json/geojson
 * parsing.   
 * 
 * NOTE! Deeper specs first. Specs are checked in declared order assuming
 * Java emits enums in order. If deeper spec is after shallower the shallow
 * one will never match.
 *
 */
public enum MapLinkGeoJsonParseContext {

	/**
	 * 
	 */
	Data_Feature(MapLinkGeoJsonParseMode.Feature, "{}", ".layers", "[]", "{#}",
			".data", "{}", ".features", "[]", "{#}"),

	/**
	 * 
	 */
	Data_FeatureCollection(MapLinkGeoJsonParseMode.FeatureCollection, "{}",
			".layers", "[]", "{#}", ".data"),

	/**
	 * 
	 */
	Tiles_Tile_Bbox(MapLinkGeoJsonParseMode.ObjectArray, "{}", ".layers", "[]",
			"{#}", ".tiles", "[]", "{#}", ".bbox"),
	/**
	 * 
	 */
	Tiles(MapLinkGeoJsonParseMode.KeyValueMap, "{}", ".layers", "[]", "{#}",
			".tiles", "[]", "{#}"),

	/**
	 * 
	 */
	Style("{}", ".layers", "[]", "{#}", ".styles", "[]", "{#}"),

	/**
	 * 
	 */
	LayerSelection("{}", ".state", "{}", ".selectedLayers", "[]", "{#}"),

	/**
	 * 
	 */
	Layer("{}", ".layers", "[]"),

	/**
	 * 
	 */
	Maplink("maplink"),

	Default("{}");

	private Stack<String> path = new Stack<String>();
	MapLinkGeoJsonParseMode pm = MapLinkGeoJsonParseMode.KeyValueMap;

	MapLinkGeoJsonParseContext(MapLinkGeoJsonParseMode pm, String... pathEls) {
		this.pm = pm;
		for (String s : pathEls) {
			path.add(s);
		}
	}

	MapLinkGeoJsonParseContext(String... pathEls) {
		for (String s : pathEls) {
			path.add(s);
		}
	}

	public Stack<String> getPath() {
		return path;
	}

	public MapLinkGeoJsonParseMode getPm() {
		return pm;
	}

	public boolean match(Stack<String> context) {
		return path.equals(context);
	}

	public boolean partialMatch(Stack<String> context) {
		if (path.size() > context.size()) {
			return false;
		}
		return path.equals(context.subList(0, path.size()));
	}

}