package fi.nls.oskari.printout.output.map;

/**
 * OpenLayers (1.12) compatible (copy-paste-mod) metrics.
 */
public class MetricScaleResolutionUtils {

	public enum INCHES_PER_UNIT_OPENLAYERS_2_12 implements ScaleResolution {

		inches(1.0), ft(12.0), mi(63360.0), m(39.3701), km(39370.1), dd(
				4374754.0), yd(36.0);

		private double value;

		public static final double DOTS_PER_INCH = 72;

		INCHES_PER_UNIT_OPENLAYERS_2_12(double value) {
			this.value = value;
		}

		public double getResolutionFromScale(double scale) {
			double normScale = normalizeScale(scale);
			double resolution = 1 / (normScale * getValue() * DOTS_PER_INCH);
			return resolution;
		}

		public double getScaleFromResolution(double resolution) {

			return resolution * getValue() * DOTS_PER_INCH;
		}

		public double getValue() {
			return value;
		}

		public double normalizeScale(double scale) {
			double normScale = (scale > 1.0) ? (1.0 / scale) : scale;
			return normScale;
		};

	}

	public enum INCHES_PER_UNIT_OPENLAYERS_2_13 implements ScaleResolution {

		inches(1.0), ft(12.0), mi(63360.0), m(39.37), km(39370), dd(4374754), yd(
				36);

		private double value;

		public static final double DOTS_PER_INCH = 72;

		INCHES_PER_UNIT_OPENLAYERS_2_13(double value) {
			this.value = value;
		}

		public double getResolutionFromScale(double scale) {
			double normScale = normalizeScale(scale);
			double resolution = 1 / (normScale * getValue() * DOTS_PER_INCH);
			return resolution;
		}

		public double getScaleFromResolution(double resolution) {

			return resolution * getValue() * DOTS_PER_INCH;
		}

		public double getValue() {
			return value;
		}

		public double normalizeScale(double scale) {
			double normScale = (scale > 1.0) ? (1.0 / scale) : scale;
			return normScale;
		};

	};

	public interface ScaleResolution {
		public double getResolutionFromScale(double scale);

		public double getScaleFromResolution(double resolution);
	};

	public enum ScaleResolutionImpl {
		m_ol212, m_ol213
	}

	public static ScaleResolution getScaleResolver(String implId) {
		ScaleResolutionImpl impl = ScaleResolutionImpl.valueOf(implId);
		switch (impl) {
		case m_ol212:
			return INCHES_PER_UNIT_OPENLAYERS_2_12.m;
		case m_ol213:
			return INCHES_PER_UNIT_OPENLAYERS_2_13.m;
		default:
			return INCHES_PER_UNIT_OPENLAYERS_2_12.m;
		}
	}

	public static double getResolutionFromScale(String implId, double scale) {
		ScaleResolutionImpl impl = ScaleResolutionImpl.valueOf(implId);

		switch (impl) {
		case m_ol212:
			return INCHES_PER_UNIT_OPENLAYERS_2_12.m
					.getResolutionFromScale(scale);
		case m_ol213:
			return INCHES_PER_UNIT_OPENLAYERS_2_13.m
					.getResolutionFromScale(scale);
		default:
			return INCHES_PER_UNIT_OPENLAYERS_2_12.m
					.getResolutionFromScale(scale);
		}
	}

	public static double getScaleFromResolution(String implId, double resolution) {
		ScaleResolutionImpl impl = ScaleResolutionImpl.valueOf(implId);

		switch (impl) {
		case m_ol212:
			return INCHES_PER_UNIT_OPENLAYERS_2_12.m
					.getScaleFromResolution(resolution);
		case m_ol213:
			return INCHES_PER_UNIT_OPENLAYERS_2_13.m
					.getScaleFromResolution(resolution);
		default:
			return INCHES_PER_UNIT_OPENLAYERS_2_12.m
					.getScaleFromResolution(resolution);
		}
	}

	
}
