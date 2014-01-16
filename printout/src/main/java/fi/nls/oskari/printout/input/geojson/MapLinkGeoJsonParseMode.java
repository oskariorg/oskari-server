package fi.nls.oskari.printout.input.geojson;

public enum MapLinkGeoJsonParseMode {
	KeyValueMap(true), FeatureCollection(true), Feature(false), ObjectArray(
			true, true);

	public boolean buildMap = true;

	public boolean array = false;

	MapLinkGeoJsonParseMode() {

	}

	MapLinkGeoJsonParseMode(boolean bm) {
		buildMap = bm;
	}

	MapLinkGeoJsonParseMode(boolean bm, boolean array) {
		buildMap = bm;
		this.array = array;
	}
};
