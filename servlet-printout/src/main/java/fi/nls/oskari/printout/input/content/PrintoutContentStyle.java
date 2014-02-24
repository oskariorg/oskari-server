package fi.nls.oskari.printout.input.content;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import fi.nls.oskari.printout.imaging.ColorOps;

public class PrintoutContentStyle {

	public enum MetricsStyleAttr {

		left(), bottom, width, height

		;

		public Float parseStyleAttr(String val) {
			if (val == null) {
				return null;
			}

			if (val.indexOf("cm") != -1) {

				return Float.valueOf(val.substring(0, val.indexOf("cm")));

			}

			return null;

		}
	}

	public enum ColourStyleAttr {
		color, backgroundColor, borderColor;

		final ColorOps colorOps = new ColorOps();

		public Color parseStyleAttr(String val) {
			return colorOps.get(val, null);
		}

		public Color parseStyleAttr(String val, Color defaultValue) {
			return colorOps.get(val, defaultValue);
		}

	}

	final Map<MetricsStyleAttr, Float> metrics = new HashMap<MetricsStyleAttr, Float>();
	final Map<ColourStyleAttr, Color> colours = new HashMap<ColourStyleAttr, Color>();

	Map<String, PrintoutContentStyle> styles;

	public Map<MetricsStyleAttr, Float> getMetrics() {
		return metrics;
	}

	public Map<ColourStyleAttr, Color> getColours() {
		return colours;
	}

	public Map<String, PrintoutContentStyle> getStyles() {
		return styles;
	}

	public void setStyles(Map<String, PrintoutContentStyle> styles) {
		this.styles = styles;
	}

	public String toString() {
		Float width = getMetrics().get(MetricsStyleAttr.width);
		Float height = getMetrics().get(MetricsStyleAttr.height);
		Float left = getMetrics().get(MetricsStyleAttr.left);
		Float bottom = getMetrics().get(MetricsStyleAttr.bottom);
		return "Style {b=" + bottom + ",l=" + left + ",w=" + width + ", h="
				+ height + "}";
	}

}
