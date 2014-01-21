package fi.nls.oskari.domain.map.wmts;

import fi.nls.oskari.domain.map.Layer;

@Deprecated
public class WMTSLayer extends Layer {
	public WMTSLayer() {
		super.setType(Layer.TYPE_WMTS);
	}

}
