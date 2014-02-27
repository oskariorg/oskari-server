package fi.nls.oskari.printout.output.layer;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Font;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

import fi.nls.oskari.printout.imaging.ColorOps;
import fi.nls.oskari.printout.input.layers.LayerDefinition;


/**
 * 
 * This class draws geojson features styled with a 'poor-mans-sld-or-css'.
 * Styling and style defs follow css/sld naming conventions.
 * 
 * This class uses geotools extensions to lookup values from features to
 * be used in simple style rules.
 *  
 */

public class DirectFeatureLayer extends FeatureLayer {
	
	

	enum Css {
		strokeColor, strokeOpacity, strokeWidth, strokeDashstyle,
		/**
		 * 
		 */

		fillColor, fillOpacity,

		/**
		 * 
		 */
		labelAlign, label, labelXOffset, labelYOffset,

		/**
		 * 
		 */
		graphicName, graphicSize, // ?

		/**
		 * 
		 */
		fontColor, fontFamily, fontSize, fontWeight, onlineResource

		;
		
		final ColorOps colorOps = new ColorOps();

		final CharSequence HASHMARK = "#";

		final CharSequence RGBA = "rgba";

		float[] dashStyle(Map<String, ?> styleMapDefaultStyle, float strokeWidth) {
			float widthFactor = 1f;
			String strokeDashStyle = (String) styleMapDefaultStyle
					.get(toString());
			if (strokeDashStyle == null) {
				return null;
			}
			float w = strokeWidth * widthFactor;

			if ("solid".equals(strokeDashStyle))
				return null;
			else if ("dot".equals(strokeDashStyle))
				return new float[] { 1f, 4 * w };
			else if ("dash".equals(strokeDashStyle))
				return new float[] { 4 * w, 4 * w };
			else if ("dashdot".equals(strokeDashStyle))
				return new float[] { 4 * w, 4 * w, 1f, 4 * w };
			else if ("longdash".equals(strokeDashStyle))
				return new float[] { 8 * w, 4 * w };
			else if ("longdashdot".equals(strokeDashStyle))
				return new float[] { 8 * w, 4 * w, 1f, 4 * w };

			return null;
		}

		/**
		 * 
		 * @param styleMapDefaultStyle
		 * @return
		 */

		String get(Map<String, ?> styleMapDefaultStyle) {
			Object obj = styleMapDefaultStyle.get(toString());
			if (obj == null)
				return null;

			return obj.toString();

		}

		Color get(Map<String, ?> styleMapDefaultStyle, Color defaultValue) {

			/*
			 * #00ff00 /* rgba(215, 40, 40, 0.9)
			 */
			Color col = defaultValue;

			Object val = styleMapDefaultStyle.get(toString());
			if (val == null) {
				return defaultValue;
			}
			
			col = colorOps.get((String)val, defaultValue);

			return col;
		}

		Color get(Map<String, ?> styleMapDefaultStyle, Color defaultValue,
				Float alphaFloat) {

			Color col = get(styleMapDefaultStyle, defaultValue);

			if (alphaFloat == null) {
				return col;
			}

			int alpha = new Float(alphaFloat * 256f / 256f).intValue();

			col = new Color(col.getRed(), col.getGreen(), col.getBlue(), alpha);

			return col;
		}

		Float get(Map<String, ?> styleMapDefaultStyle, Float defaultValue) {

			Object val = styleMapDefaultStyle.get(toString());
			if (val == null) {
				return defaultValue;
			}
			if (val instanceof Number) {
				return ((Number) val).floatValue();
			}

			return Float.parseFloat(val.toString());
		}

		String get(Map<String, ?> styleMapDefaultStyle, String defaultValue) {
			Object obj = styleMapDefaultStyle.get(toString());
			if (obj == null)
				return defaultValue;

			String s = obj.toString();
			return s;
		}

		String[] getFontFamily(Map<String, ?> styleMapDefaultStyle,
				String... defaultValue) {
			String s = (String) styleMapDefaultStyle.get(toString());
			if (s == null)
				return defaultValue;

			if (s.indexOf(',') != -1) {
				return s.split(",");
			} else {
				return new String[] { s };
			}

		}

		Float getPx(Map<String, ?> styleMapDefaultStyle, Float defaultValue) {
			String px = (String) styleMapDefaultStyle.get(toString());

			if (px == null) {
				return defaultValue;
			}

			if (px.indexOf("px") == -1) {
				return Float.valueOf(px);
			}

			return Float.valueOf(px.substring(0, px.indexOf("px")));
		}

	}

	private int layerOpacity;

	final StyleFactory sf = CommonFactoryFinder.getStyleFactory();

	final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
	StyleBuilder sb = new StyleBuilder();
	private FeatureSource<SimpleFeatureType, SimpleFeature> fs;

	public DirectFeatureLayer(LayerDefinition layerDefinition,
			FeatureSource<SimpleFeatureType, SimpleFeature> fs,
			AffineTransform transform) throws URISyntaxException {
		super(fs, null);
		this.fs = fs;

		super.setStyle(extractLayerStyle(layerDefinition));
	}

	private Fill createFill(Map<String, ?> styleMapDefaultStyle,
			Float fillOpacity) {
		Fill fill = null;

		String fillColorStr = Css.fillColor.get(styleMapDefaultStyle);

		if (fillColorStr != null && fillColorStr.indexOf('$') != -1) {

			Function colorFunc = createPropertyAccessor(fillColorStr);
			Literal literalOpacity = ff.literal(fillOpacity);
			fill = sf.createFill(colorFunc, literalOpacity);

		} else {
			Color fillColor = Css.fillColor.get(styleMapDefaultStyle,
					(Color) null);

			if (fillColor == null) {
				return null;
			}

			Literal literalFill = ff.literal(fillColor);
			Literal literalOpacity = ff.literal(fillOpacity);
			fill = literalFill != null ? sf.createFill(literalFill,
					literalOpacity) : null;
		}

		return fill;
	}

	@SuppressWarnings("unchecked")
	private Expression createGeometryAccessor(
			Map<String, ?> styleMapDefaultStyle) {
		Map<String, ?> geomProps = (Map<String, ?>) styleMapDefaultStyle
				.get("geometry");

		String geomPropName = geomProps != null ? (String) geomProps
				.get("name") : null;
		if (geomPropName == null) {
			return null;
		}

		Object translateX = geomProps.get("translateX");
		Object translateY = geomProps.get("translateY");

		Expression geomAccessor = ff.property(geomPropName);

		if (translateX != null && translateY != null) {

			Expression offsetX = null;
			Expression offsetY = null;

			if (translateX instanceof String) {
				offsetX = createPropertyAccessor((String) translateX);
			} else {
				offsetX = ff.literal(((Number) translateX).doubleValue());
			}

			if (translateY instanceof String) {
				offsetY = createPropertyAccessor((String) translateY);
			} else {
				offsetY = ff.literal(((Number) translateY).doubleValue());
			}

			geomAccessor = ff
					.function("offset", geomAccessor, offsetX, offsetY);

		} else {
			geomAccessor = ff.property(geomPropName);
		}

		return geomAccessor;
	};

	Mark createMark(Map<String, ?> styleMapDefaultStyle, Mark defaultValue) {

		String markGraphicName = Css.graphicName.get(styleMapDefaultStyle);
		if (markGraphicName == null) {
			return defaultValue;
		}

		return sb.createMark(markGraphicName);
	}

	/**
	 * 
	 * force OpenLayers ${} to %1$s Java counterpart
	 */
	private Function createPropertyAccessor(String propertyAccessorDefinition) {
		String propertyAccessor = new String(propertyAccessorDefinition);

		/* 1) FIX labelFormat ${refs} to index Java String refs $1s */
		/* 2) add props to func */
		Collection<PropertyDescriptor> props = fs.getSchema().getDescriptors();
		int exprsCount = props.size();
		int exprsLabelFormatIndex = exprsCount;
		Expression[] exprs = new Expression[1 + props.size()];

		for (PropertyDescriptor pd : props) {
			PropertyType pdt = pd.getType();

			if (pdt instanceof GeometryType) {
				int pdn = (--exprsCount);
				exprs[pdn] = ff.literal(pd.getName().getLocalPart());
				continue;
			}

			int pdn = (--exprsCount);
			exprs[pdn] = ff.property(pd.getName().getLocalPart());

			propertyAccessor = propertyAccessor.replaceAll("\\$\\{"
					+ pd.getName().getLocalPart() + "\\}", "\\%" + (pdn + 1)
					+ "\\$s");

		}

		exprs[exprsLabelFormatIndex] = ff.literal(propertyAccessor);

		Function labelFunc = ff.function("renderproperty", exprs);

		return labelFunc;
	}

	private Rule createRule(Map<String, ?> styleMapDefaultStyle)
			throws URISyntaxException {

		/* stroke */
		Float strokeWidth = Css.strokeWidth.get(styleMapDefaultStyle, 0f);
		float[] dashStyle = Css.strokeDashstyle.dashStyle(styleMapDefaultStyle,
				strokeWidth);

		/* fill */
		Float fillOpacity = Css.fillOpacity.get(styleMapDefaultStyle,
				(Float) null);

		String strokeColor = Css.strokeColor.get(styleMapDefaultStyle);
		String fillColor = Css.fillColor.get(styleMapDefaultStyle);

		/* mark */

		/* text */
		Color fontColor = Css.fontColor.get(styleMapDefaultStyle, Color.BLACK);
		String[] fontFamily = Css.fontFamily.getFontFamily(
				styleMapDefaultStyle, "Lucida Sans", "sans-serif");
		Float fontSize = Css.fontSize.getPx(styleMapDefaultStyle, 10f);
		String fontWeight = Css.fontWeight.get(styleMapDefaultStyle);

		boolean isItalic = fontWeight != null
				&& fontWeight.indexOf("italic") != -1;
		boolean isBold = fontWeight != null && fontWeight.indexOf("bold") != -1;

		String labelXOffset = Css.labelXOffset.get(styleMapDefaultStyle);
		String labelYOffset = Css.labelYOffset.get(styleMapDefaultStyle);

		Rule rule = sf.createRule();

		/* stroke */

		if (strokeColor != null && strokeWidth > 0) {
			Stroke stroke = null;
			stroke = strokeColor != null ? createStroke(styleMapDefaultStyle,
					strokeWidth) : null;
			if (dashStyle != null && stroke != null) {
				stroke.setDashArray(dashStyle);
			}
			Symbolizer symbolizer = null;

			symbolizer = sf.createLineSymbolizer(stroke, null);

			symbolizer
					.setGeometry(createGeometryAccessor(styleMapDefaultStyle));

			rule.symbolizers().add(symbolizer);
		}

		/* fill */
		if (fillColor != null) {
			Stroke stroke = null;
			stroke = strokeWidth > 0 && strokeColor != null ? createStroke(
					styleMapDefaultStyle, strokeWidth) : null;
			if (dashStyle != null && stroke != null) {
				stroke.setDashArray(dashStyle);
			}

			Symbolizer symbolizer = null;
			Fill fill = null;

			fill = createFill(styleMapDefaultStyle, fillOpacity);
			symbolizer = sf.createPolygonSymbolizer(stroke, fill, null);
			symbolizer
					.setGeometry(createGeometryAccessor(styleMapDefaultStyle));

			rule.symbolizers().add(symbolizer);

		}

		Mark mark = createMark(styleMapDefaultStyle, null);
		if (mark != null) {
			Stroke stroke = null;
			stroke = strokeColor != null && strokeWidth > 0 ? createStroke(
					styleMapDefaultStyle, strokeWidth) : null;
			if (dashStyle != null && stroke != null) {
				stroke.setDashArray(dashStyle);
			}

			Symbolizer symbolizer = null;
			Fill fill = null;

			fill = createFill(styleMapDefaultStyle, fillOpacity);

			mark.setFill(fill);
			mark.setStroke(stroke);

			Graphic graphic = sf.createDefaultGraphic();
			graphic.graphicalSymbols().clear();
			graphic.graphicalSymbols().add(mark);

			String graphicSize = Css.graphicSize
					.get(styleMapDefaultStyle, "10");
			graphic.setSize(createPropertyAccessor(graphicSize));

			symbolizer = sf.createPointSymbolizer(graphic, null);
			symbolizer
					.setGeometry(createGeometryAccessor(styleMapDefaultStyle));

			rule.symbolizers().add(symbolizer);
		}

		String label = Css.label.get(styleMapDefaultStyle);
		if (label != null) {

			Function labelFunc = createPropertyAccessor(label);

			PointPlacement pointPlacement = sb.createPointPlacement();

			if (labelXOffset == null && labelYOffset == null) {
				pointPlacement.setDisplacement(sb.createDisplacement(0, 0));
			} else if (labelXOffset.indexOf('$') == -1
					&& labelYOffset.indexOf('$') == -1) {

				Double xoffset = Double.valueOf(labelXOffset);
				Double yoffset = Double.valueOf(labelYOffset);

				pointPlacement.setDisplacement(sb.createDisplacement(xoffset,
						yoffset));

			} else {
				Function labelXOffsetFunc = createPropertyAccessor(labelXOffset);
				Function labelYOffsetFunc = createPropertyAccessor(labelYOffset);

				pointPlacement.setDisplacement(sb.createDisplacement(
						labelXOffsetFunc, labelYOffsetFunc));
			}

			Font[] fonts = new Font[fontFamily.length];
			int f = 0;
			for (String ff : fontFamily) {
				fonts[f++] = sb.createFont(ff, isItalic, isBold, fontSize);
			}

			TextSymbolizer symbolizer = sb.createTextSymbolizer(
					sb.createFill(fontColor), fonts, null, labelFunc,
					pointPlacement, null);
			symbolizer
					.setGeometry(createGeometryAccessor(styleMapDefaultStyle));

			rule.symbolizers().add(symbolizer);

		}

		return rule;
	}

	private Stroke createStroke(Map<String, ?> styleMapDefaultStyle,
			Float strokeWidth) {
		Stroke stroke = null;
		String strokeColorStr = Css.strokeColor.get(styleMapDefaultStyle);
		if (strokeColorStr != null && strokeColorStr.indexOf('$') != -1) {
			Function colorFunc = createPropertyAccessor(strokeColorStr);

			stroke = sf.createStroke(colorFunc, ff.literal(strokeWidth));

		} else {
			Color strokeColor = Css.strokeColor.get(styleMapDefaultStyle,
					(Color) null);

			if (strokeColor == null)
				return null;

			stroke = sf.createStroke(ff.literal(strokeColor),
					ff.literal(strokeWidth));
		}
		return stroke;
	}

	@SuppressWarnings("unchecked")
	public Style extractLayerStyle(LayerDefinition layerDefinition)
			throws URISyntaxException {

		fi.nls.oskari.printout.input.layers.LayerDefinition.Style layerStyle = layerDefinition
				.getStyle() != null ? layerDefinition.getStyles().get(
				layerDefinition.getStyle()) : null;
		Style style = null;

		if (layerStyle != null && layerStyle.getStyleMap() != null) {
			Map<String, ?> styleMap = layerStyle.getStyleMap();

			Map<String, ?> styleMapDefaultStyle = (Map<String, ?>) styleMap
					.get("default");

			Rule rule = createRule(styleMapDefaultStyle);

			FeatureTypeStyle fts = sf.createFeatureTypeStyle();
			fts.rules().add(rule);

			style = sf.createStyle();
			style.featureTypeStyles().add(fts);

		} else {

			Stroke stroke = sf.createStroke(ff.literal(Color.BLUE),
					ff.literal(1));

			LineSymbolizer sym = sf.createLineSymbolizer(stroke, null);

			Rule rule = sf.createRule();
			rule.symbolizers().add(sym);
			FeatureTypeStyle fts = sf
					.createFeatureTypeStyle(new Rule[] { rule });
			style = sf.createStyle();
			style.featureTypeStyles().add(fts);
		}

		return style;
	}

	public int getLayerOpacity() {
		return layerOpacity;
	}

	public void setLayerOpacity(int layerOpacity) {
		this.layerOpacity = layerOpacity;
	}

}
