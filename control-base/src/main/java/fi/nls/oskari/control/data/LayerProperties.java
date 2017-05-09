package fi.nls.oskari.control.data;

import fi.nls.oskari.util.ConversionHelper;

public class LayerProperties {

	public static final int NULL = Integer.MIN_VALUE;

	private final String id;
	private final int opacity;
	private final String style;

	public LayerProperties(String id, int opacity, String style) {
		this.id = id;
		this.opacity = opacity;
		this.style = style;
	}


	public static LayerProperties parse(String[] layerParam) throws IllegalArgumentException {
		if (layerParam == null || layerParam.length == 0) {
			throw new IllegalArgumentException("layerParam must be non-null with positive length!");
		}
		
		String id = layerParam[0];
		int opacity = layerParam.length > 1 ?
				ConversionHelper.getInt(layerParam[1], NULL) : NULL;
		String style = layerParam.length > 2 ? layerParam[2] : null;
		
		return new LayerProperties(id, opacity, style);
	}
	
	
	public static LayerProperties[] parse(String mapLayers) throws IllegalArgumentException {
		final String[] layers = mapLayers.split(",");
		final int n = layers.length;
		
		final LayerProperties[] layerProperties = new LayerProperties[n];
		for (int i = 0; i < n; i++) {
			String[] layerParam = layers[i].split(" ");
			layerProperties[i] = parse(layerParam);
		}
		
		return layerProperties;
	}
	
	
	public static int getDefaultOpacity() {
		return NULL;
	}

	public String getId() {
		return id;
	}

	public int getOpacity() {
		return opacity;
	}

	public String getStyle() {
		return style;
	}

}
