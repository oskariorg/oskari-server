package fi.nls.oskari.printout.input.maplink;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import fi.nls.oskari.printout.input.content.PrintoutContent;
import fi.nls.oskari.printout.input.content.PrintoutContentParser;
import fi.nls.oskari.printout.input.layers.LayerDefinition;
import fi.nls.oskari.printout.input.layers.MapLayerJSON;
import fi.nls.oskari.printout.output.map.MetricScaleResolutionUtils.ScaleResolution;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class parses MapLink from URL or map of URL parameter values.
 * 
 */

public class MapLinkParser {
	private static Log log = LogFactory.getLog(MapLinkParser.class);
	private ScaleResolution sr;
	private Integer zoomOffset = 0;
	PrintoutContentParser contentParser = new PrintoutContentParser();

	public MapLinkParser(ScaleResolution sr, Integer zoomOffset) {
		this.sr = sr;
		this.zoomOffset = zoomOffset;
	}

	@SuppressWarnings("unchecked")
	public MapLink parseJSONMapLink(Map<String, ?> obj, MapLayerJSON layers,
			GeometryFactory gf, double[] resolutions)
			throws UnsupportedEncodingException {

		MapLink mapLink = null;

		Map<String, ?> mapLinkInfo = (Map<String, ?>) obj.get("maplink");

		if (mapLinkInfo.get("args") != null) {
			String mapLinkQueryArgs = URLDecoder.decode(
					(String) mapLinkInfo.get("args"), "UTF-8");

			mapLink = parseURLMapLink(mapLinkQueryArgs, layers, gf, resolutions);
		} else {
			Map<String, String> values = new HashMap<String, String>();

			for (String mapLinkArg : mapLinkInfo.keySet()) {
				String upper = new String(mapLinkArg).toUpperCase();
				if (mapLinkInfo.get(mapLinkArg) == null) {
					continue;
				}
				values.put(upper, mapLinkInfo.get(mapLinkArg).toString());
			}

			mapLink = parseValueMapLink(values, layers, gf, resolutions);

		}

		Map<String, ?> stateInfo = (Map<String, ?>) obj.get("state");

		if (stateInfo != null) {

			Integer zoomLevel = ((Number) stateInfo.get("zoom")).intValue();
			zoomLevel += zoomOffset;
            if(zoomLevel >= resolutions.length) {
                zoomLevel = resolutions.length - 1;
            }

			mapLink.setZoom(zoomLevel);
			mapLink.setScale(sr.getScaleFromResolution(resolutions[zoomLevel]));

			double x = ((Number) stateInfo.get("east")).doubleValue();
			double y = ((Number) stateInfo.get("north")).doubleValue();
			Point pt = gf.createPoint(new Coordinate(x, y));
			mapLink.setCentre(pt);

			List<Map<String, ?>> linkLayerObjs = (List<Map<String, ?>>) stateInfo
					.get("selectedLayers");

			if (!linkLayerObjs.isEmpty()) {

				mapLink.getMapLinkLayers().clear();

			}
			for (Map<String, ?> linkLayerObj : linkLayerObjs) {

				String layerId = linkLayerObj.get("id").toString();
				Integer opacity = null;

				if (linkLayerObj.get("opacity") instanceof Number) {
					opacity = ((Number) linkLayerObj.get("opacity")).intValue();
				} else {
					opacity = Integer.valueOf(
							(String) linkLayerObj.get("opacity"), 10);
				}

				String style = (String) linkLayerObj.get("style");

				LayerDefinition layerDef = layers.getLayerDefs().get(layerId);
				if (layerDef == null) {
					log.info("LAYER not found layers " + layerId);
					continue;
				}

				LayerDefinition layerSelection = new LayerDefinition();

				layerDef.copyTo(layerSelection);

				if (layerSelection.getFormat() == null) {
					layerSelection.setFormat("image/png");
				}

				layerSelection.setOpacity(opacity);
				layerSelection.setScale(mapLink.getScale());
				layerSelection.setStyle(style);
				for (LayerDefinition subdef : layerSelection.getSubLayers()) {
					subdef.setScale(mapLink.getScale());
					subdef.setOpacity(layerSelection.getOpacity());
					subdef.setStyle(style);
				}

				mapLink.getMapLinkLayers().add(layerSelection);

			}
		}

		if (mapLink != null) {
			Map<String, ?> printoutInfo = (Map<String, ?>) obj.get("printout");

			if (printoutInfo != null) {
				PrintoutContent content = contentParser.parse(printoutInfo);

				mapLink.setPrintoutContent(content);
			}

		}

		return mapLink;
	}

	void parseMapLinkMapLinkPart(MapLink mapLink, Map<String, ?> values,
			MapLayerJSON layers, GeometryFactory gf, double[] resolutions) {

		Integer zoomLevel = values.get("ZOOMLEVEL") instanceof Integer ? (Integer) values
				.get("ZOOMLEVEL") : Integer.valueOf(
				(String) values.get("ZOOMLEVEL"), 10);
		zoomLevel += zoomOffset;
        if(zoomLevel >= resolutions.length) {
            zoomLevel = resolutions.length - 1;
        }
		mapLink.setZoom(zoomLevel);
		mapLink.setScale(sr.getScaleFromResolution(resolutions[zoomLevel]));

		String[] xy = ((String) values.get("COORD")).split("_");
		double x = Double.valueOf(xy[0]);
		double y = Double.valueOf(xy[1]);
		Point pt = gf.createPoint(new Coordinate(x, y));
		mapLink.setCentre(pt);

		String[] linkLayers = ((String) values.get("MAPLAYERS")).split(",");
		if (linkLayers == null) {
			return;
		}
		for (String linkLayer : linkLayers) {
			if ("".equals(linkLayer)) {
				continue;
			}
			String[] linkParts = linkLayer.split(" ");
			String layerId = linkParts[0];
			String opacity = linkParts[1];
			String style = null;
			if (linkParts.length > 2) {
				style = linkParts[2];
			}

			log.info("LAYERID " + layerId);
			LayerDefinition layerDef = layers.getLayerDefs().get(layerId);

			if (layerDef == null) {
				log.info("NOT FOUND in layers JSON " + layerId);
				continue;
			}

			LayerDefinition layerSelection = new LayerDefinition();

			layerDef.copyTo(layerSelection);

			if (layerSelection.getFormat() == null) {
				layerSelection.setFormat("image/png");
			}

			layerSelection.setOpacity(Integer.valueOf(opacity, 10));
			layerSelection.setScale(mapLink.getScale());
			layerSelection.setStyle(style);
			for (LayerDefinition subdef : layerSelection.getSubLayers()) {
				subdef.setScale(mapLink.getScale());
				subdef.setOpacity(layerSelection.getOpacity());
				subdef.setStyle(style);
			}

			mapLink.getMapLinkLayers().add(layerSelection);

		}

	}

	public MapLink parseURLMapLink(String link, MapLayerJSON layers,
			GeometryFactory gf, double[] resolutions)
			throws UnsupportedEncodingException {

		log.info("Parsing " + link);

		MapLink mapLink = new MapLink();

		String[] parts = link.split("&");
		Map<String, String> values = new HashMap<String, String>();
		for (String s : parts) {
			String[] keyValue = s.split("=");
			String key = keyValue[0].toUpperCase();
			String value = keyValue.length > 1 ? keyValue[1] : "";
			values.put(key, value);
		}

		parseMapLinkMapLinkPart(mapLink, values, layers, gf, resolutions);

		if (values.get("WIDTH") != null) {
			mapLink.setWidth(Integer.valueOf(values.get("WIDTH")));
		}
		if (values.get("HEIGHT") != null) {
			mapLink.setHeight(Integer.valueOf(values.get("HEIGHT")));
		}

		mapLink.getValues().putAll(values);

		return mapLink;
	}

	public MapLink parseValueMapLink(Map<String, String> values,
			MapLayerJSON layers, GeometryFactory gf, double[] resolutions)
			throws UnsupportedEncodingException {
		MapLink mapLink = new MapLink();

		parseMapLinkMapLinkPart(mapLink, values, layers, gf, resolutions);

		if (values.get("WIDTH") != null) {
			mapLink.setWidth(Integer.valueOf(values.get("WIDTH")));
		}
		if (values.get("HEIGHT") != null) {
			mapLink.setHeight(Integer.valueOf(values.get("HEIGHT")));
		}

		mapLink.getValues().putAll(values);

		return mapLink;
	}

	int maxWidth = 2560;
	int maxHeight = 2560;
	int minWidth = 128;
	int minHeight = 128;
	
	public void validate(MapLink mapLink) throws IOException {
		

		Map<String, String> values = mapLink.getValues();

		if (values.get("WIDTH") != null) {
			if (Integer.valueOf(values.get("WIDTH"), 10) > maxWidth) {
				throw new IOException("Too Large a map requested");
			}
			if (Integer.valueOf(values.get("WIDTH"), 10) < minWidth) {
				throw new IOException("Too Small a map requested");
			}
		}
		if (values.get("HEIGHT") != null) {
			if (Integer.valueOf(values.get("HEIGHT"), 10) > maxHeight) {
				throw new IOException("Too Large a map requested");
			}
			if (Integer.valueOf(values.get("HEIGHT"), 10) < minHeight) {
				throw new IOException("Too Small a map requested");
			}
		}
		if (values.get("SCALEDWIDTH") != null) {
			if (Integer.valueOf(values.get("SCALEDWIDTH"), 10) > maxWidth) {
				throw new IOException("Too Large a map requested");
			}
			if (Integer.valueOf(values.get("SCALEDWIDTH"), 10) < minWidth) {
				throw new IOException("Too Small a map requested");
			}
		}
		if (values.get("SCALEDHEIGHT") != null) {
			if (Integer.valueOf(values.get("SCALEDHEIGHT"), 10) > maxHeight) {
				throw new IOException("Too Large a map requested");
			}
			if (Integer.valueOf(values.get("SCALEDHEIGHT"), 10) < minHeight) {
				throw new IOException("Too Small a map requested");
			}
		}

		if (mapLink.getWidth() > maxWidth || mapLink.getHeight() > maxHeight) {
			throw new IOException("Too Large a map requested");
		}

		if (mapLink.getMapLinkLayers().size() > 8) {
			throw new IOException("Too Many Layers in Map Link");
		}

	}
}
