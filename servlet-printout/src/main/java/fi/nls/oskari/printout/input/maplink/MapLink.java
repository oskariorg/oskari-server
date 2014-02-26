package fi.nls.oskari.printout.input.maplink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

import fi.nls.oskari.printout.input.content.PrintoutContent;
import fi.nls.oskari.printout.input.layers.LayerDefinition;
import fi.nls.oskari.printout.printing.PDFProducer.Options;

/**
 * 
 * This class contains any information read from JSON or URL request.
 * 
 */
public class MapLink {

	final List<LayerDefinition> mapLinkLayers = new ArrayList<LayerDefinition>();
	Point centre;
	Envelope env;
	int zoom;
	Double scale;
	int width;
	int height;
	final Map<String, String> values = new HashMap<String, String>();

	PrintoutContent printoutContent;

	public Point getCentre() {
		return centre;
	}

	public Envelope getEnv() {
		return env;
	}

	public int getHeight() {
		return height;
	}

	public List<LayerDefinition> getMapLinkLayers() {
		return mapLinkLayers;
	}

	public Double getScale() {
		return scale;
	}

	public Map<String, String> getValues() {
		return values;
	}

	public int getWidth() {
		return width;
	}

	public int getZoom() {
		return zoom;
	}

	public LayerDefinition selectLayerDefinitionForScale(LayerDefinition self) {

		if (self.getSubLayers().isEmpty()) {

			Double minScale = self.getMinScale();
			Double maxScale = self.getMaxScale();

			if (minScale == null && maxScale == null) {
				return self;
			} else if (minScale == null && scale >= maxScale) {
				return self;
			} else if (minScale >= scale && maxScale == null) {
				return self;
			} else if (minScale >= scale && scale >= maxScale) {
				return self;
			}

		} else {

			for (LayerDefinition subLayerDef : self.getSubLayers()) {

				LayerDefinition inScale = selectLayerDefinitionForScale(subLayerDef);
				if (inScale != null) {

					return inScale;
				}
			}

		}

		return null;
	}

	public void setCentre(Point centre) {
		this.centre = centre;
	}

	public void setEnv(Envelope env) {
		this.env = env;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setScale(Double scale) {
		this.scale = scale;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
	}



	public PrintoutContent getPrintoutContent() {
		return printoutContent;
	}

	public void setPrintoutContent(PrintoutContent printoutContent) {
		this.printoutContent = printoutContent;
	}

	

}
