package fi.nls.oskari.domain.map.wmts;

import fi.nls.oskari.domain.map.Layer;

public class WMTSLayer extends Layer {
	public WMTSLayer() {
		super.setType(Layer.WMTS_LAYER);
	}

}
