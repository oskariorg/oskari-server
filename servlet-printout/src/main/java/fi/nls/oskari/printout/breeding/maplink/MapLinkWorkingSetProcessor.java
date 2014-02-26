package fi.nls.oskari.printout.breeding.maplink;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.geowebcache.GeoWebCacheException;
import org.geowebcache.grid.BoundingBox;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.layer.TileLayer;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import fi.nls.oskari.printout.breeding.ChangeSet;
import fi.nls.oskari.printout.breeding.WorkingSet;
import fi.nls.oskari.printout.breeding.WorkingSetProcessor;
import fi.nls.oskari.printout.input.layers.LayerDefinition;

public class MapLinkWorkingSetProcessor extends WorkingSetProcessor {

	TileLayer tileLayer;
	GridSubset gridSubset;
	String templateLayer;
	GeometricShapeFactory gsf = new GeometricShapeFactory();
	GeometryFactory gf = new GeometryFactory();
	int zoomLevel = -1;
	int adjustedZoomLevel = -1;
	String gridSubsetName = null;

	public MapLinkWorkingSetProcessor() {

	}

	
	public ChangeSet createChangeSet(WorkingSet ws, Polygon polygon,
			TileLayer tileLayer, String gridSubsetName, String entryId)
			throws GeoWebCacheException {

		Envelope envelope = polygon.getEnvelopeInternal();

		GridSubset gridSubset = tileLayer.getGridSubset(gridSubsetName);

		double[] resArray = gridSubset.getResolutions();
		int zoomStart = zoomLevel;
		int zoomStop = zoomLevel;

		Vector<Double> resolutions = new Vector<Double>();
		int n = 0;
		for (double d : resArray) {
			if (!isInRange(n, zoomStart, zoomStop)) {
				n++;
				continue;
			}
			resolutions.add(d);
			n++;
		}

		BoundingBox reqBounds = new BoundingBox(envelope.getMinX(),
				envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());

		ChangeSet changeSet = ws.addEntry(tileLayer.getName(), gridSubsetName,
				polygon, reqBounds, resolutions, zoomStart, zoomStop,
				gridSubset.getTileWidth(), gridSubset.getTileHeight());

		changeSet.setIdentifier(entryId);

		return changeSet;

	}

	public Envelope getEnvFromPointZoomAndExtent(Point centre, int zoom,
			int width, int height) {
		double[] resArray = gridSubset.getResolutions();
		double resolution = resArray[zoom];
		Envelope env = new Envelope(centre.getX() - (resolution * width / 2),
				centre.getX() + (resolution * width / 2), centre.getY()
						- (resolution * height / 2), centre.getY()
						+ (resolution * height / 2));

		return env;
	}

	public GridSubset getGridSubset() {
		return gridSubset;
	}

	public String getGridSubsetName() {
		return gridSubsetName;
	}

	public String getTemplateLayer() {
		return templateLayer;
	}

	public TileLayer getTileLayer() {
		return tileLayer;
	}

	public List<WorkingSet> getWorkingSetsForMapLink(Envelope env, int zoom,
			List<LayerDefinition> layers) throws ParseException, IOException,
			GeoWebCacheException, XMLStreamException, FactoryConfigurationError {
		this.zoomLevel = zoom;

		ArrayList<WorkingSet> list = new ArrayList<WorkingSet>();

		gsf.setEnvelope(env);
		Polygon polygon = gsf.createRectangle();

		for (LayerDefinition layerDefinition : layers) {
			String identifier = layerDefinition.getWmsname();
			WorkingSet ws = new WorkingSet(templateLayer);

			createWorkingSet(ws, templateLayer, gridSubsetName, polygon,
					identifier);

			createChangeSets(ws, templateLayer);

			ws.setLayer(layerDefinition.getWmsname());

			list.add(ws);
		}

		return list;
	}

	public int getZoomLevel() {
		return zoomLevel;
	}

	public void setGridSubset(GridSubset gridSubset) {
		this.gridSubset = gridSubset;
	}

	public void setGridSubsetName(String gridSubsetName) {

		this.gridSubsetName = gridSubsetName;
	}

	public void setTemplateLayer(String templateLayer) {
		this.templateLayer = templateLayer;
	}

	public void setTileLayer(TileLayer tileLayer) {
		this.tileLayer = tileLayer;
	}

	public void setZoomLevel(int zoomLevel) {
		this.zoomLevel = zoomLevel;
	}

}
