package fi.nls.oskari.arcgis;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.WFSCustomStyleStore;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class ArcGisStyleMapper {
	private static final Logger log = LogFactory.getLogger(ArcGisStyleMapper.class);
	public static final String POLYGON_GEOMETRY = "esriGeometryPolygon";
	public static final String POLYLINE_GEOMETRY = "esriGeometryPolyline";
	public static final String POINT_GEOMETRY = "esriGeometryPoint";
	
	public static final String MARKER_SYMBOL_TYPE = "esriSMS";
	public static final String LINE_SYMBOL_TYPE = "esriSLS";
	public static final String FILL_SYMBOL_TYPE = "esriSFS";
	
	public static JSONObject mapStyleToSymbol(WFSCustomStyleStore customStyle, String geometryType) {
		
		if (geometryType == null) {
			log.error("Empty geometry type");
		}
		else if (geometryType.equals(POLYGON_GEOMETRY)) {
			return mapPolygonStyle(customStyle);
		} else if (geometryType.equals(POLYLINE_GEOMETRY)) {
			return mapPolylineStyle(customStyle);
		} else if (geometryType.equals(POINT_GEOMETRY)) {
			return mapPointStyle(customStyle);
		}
		else {
			log.error("Not supported geometry type", geometryType);
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static JSONObject mapPolygonStyle(WFSCustomStyleStore customStyle) 
	{
		JSONObject symbol = new JSONObject();
		symbol.put("type", FILL_SYMBOL_TYPE);
		symbol.put("style", mapFillStyle(customStyle.getFillPattern()));
		JSONArray colorArray = new JSONArray();
		colorArray.addAll(mapColor(customStyle.getFillColor()));
		symbol.put("color", colorArray);		
		
		JSONObject outline = new JSONObject();
		outline.put("type", LINE_SYMBOL_TYPE);
		outline.put("style", mapLineStyle(customStyle.getBorderDasharray()));
		JSONArray outlineColorArray = new JSONArray();
		outlineColorArray.addAll(mapColor(customStyle.getBorderColor()));
		outline.put("color", outlineColorArray);
		outline.put("width", customStyle.getBorderWidth());
		
		symbol.put("outline", outline);
		
		return symbol;
	}
	
	@SuppressWarnings("unchecked")
	private static JSONObject mapPolylineStyle(WFSCustomStyleStore customStyle) {
		
		JSONObject outline = new JSONObject();
		outline.put("type", LINE_SYMBOL_TYPE);
		outline.put("style", mapLineStyle(customStyle.getStrokeDasharray()));
		JSONArray outlineColorArray = new JSONArray();
		outlineColorArray.addAll(mapColor(customStyle.getStrokeColor()));
		outline.put("color", outlineColorArray);
		outline.put("width", customStyle.getStrokeWidth());	
		
		return outline;
	}
	
	@SuppressWarnings("unchecked")
	private static JSONObject mapPointStyle(WFSCustomStyleStore customStyle) {
		
		JSONObject outline = new JSONObject();
		outline.put("type", MARKER_SYMBOL_TYPE);
		outline.put("style", mapPointStyle(customStyle.getDotShape()));
		JSONArray outlineColorArray = new JSONArray();
		outlineColorArray.addAll(mapColor(customStyle.getDotColor()));
		outline.put("color", outlineColorArray);
		outline.put("size", customStyle.getDotSize());	
		
		return outline;
	}
	
	private static String mapPointStyle(int code) {
		String result;
		
		switch (code) {
		case 1:
			result = "esriSMSSquare";
			break;
		case 5:
			result = "esriSMSCircle";
			break;
		default:
			result = "esriSMSCircle";
			break;
		}
		
		return result;
	}
	
	private static String mapLineStyle(String dasharray) {
		String result = "esriSLSSolid";
		
		if (dasharray.equals("5 2"))
			result = "esriSLSDash";
		
		return result;
	}
	
	private static String mapFillStyle(int code) {
		String result = "esriSFSSolid";
		
		if (code == 0 || code == 1) 
			result = "esriSFSBackwardDiagonal";
		else if (code == 2 || code == 3)
			result = "esriSFSHorizontal";
		else if (code == 4) 
			result = "esriSFSNull";
		
		return result;
	}
	
	private static ArrayList<Integer> mapColor(final String color) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		result.add(255);
		result.add(255);
		result.add(255);
		result.add(255);
		String tmpColor = color;
		
		if (tmpColor.startsWith("#"))
			tmpColor = tmpColor.substring(1);
		
		int index = 0;
		while (tmpColor.length() >= 2 && index < 4) {
			result.set(index, Integer.parseInt(tmpColor.substring(0, 2), 16));
			tmpColor = tmpColor.substring(2);
			index++;
		}
		
		return result;
	}
}
