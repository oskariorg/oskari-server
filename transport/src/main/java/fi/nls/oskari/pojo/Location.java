package fi.nls.oskari.pojo;

import java.util.ArrayList;
import java.util.List;

import fi.nls.oskari.log.LogFactory;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import fi.nls.oskari.log.Logger;

/**
 * Handles the user's current location on the map
 * 
 * Used for storing location in SessionStore.
 * 
 * @see SessionStore
 */
public class Location {
	private static final Logger log = LogFactory.getLogger(Location.class);
	
	private String srs;
	private List<Double> bbox;
    private Double[] bboxArray;
	private long zoom;
	private ReferencedEnvelope envelope = null;
	private CoordinateReferenceSystem crs = null;

	/**
	 * Constructs object without parameters
	 */
	public Location() {
		bbox = new ArrayList<Double>();
	}

	/**
	 * Constructs object without parameters
	 */
	public Location(String srs) {
		this.srs = srs;
		bbox = new ArrayList<Double>();
	}

	
	/**
	 * Gets srs
	 * 
	 * @return srs
	 */
	public String getSrs() {
		return srs;
	}

	/**
	 * Sets srs
	 * 
	 * @param srs
	 */
	public void setSrs(String srs) {
		this.srs = srs;
	}

	/**
	 * Gets bbox
     *
     * Made for JSON parser.
	 * 
	 * @return bbox
	 */
	public ArrayList<Double> getBbox() {
		return (ArrayList<Double>) bbox;
	}

    /**
     * Gets bbox
     *
     * @return bbox
     */
    @JsonIgnore
    public Double[] getBboxArray() {
        if(bboxArray == null) {
            bboxArray = new Double[]{
                this.getLeft(), // x1
                this.getBottom(), // y1
                this.getRight(), // x2
                this.getTop(), // y2
            };
        }
        return bboxArray;
    }

	/**
	 * Sets bbox
	 * 
	 * @param bbox
	 */
	public void setBbox(List<Double> bbox) {
		this.bbox = bbox;
	}

	/**
	 * Gets left (x1)
	 * 
	 * @return left
	 */
	@JsonIgnore
	public double getLeft() {
		return bbox.get(0);
	}

	/**
	 * Gets bottom (y1)
	 * 
	 * @return bottom
	 */
	@JsonIgnore
	public double getBottom() {
		return bbox.get(1);
	}

	/**
	 * Gets right (x2)
	 * 
	 * @return right
	 */
	@JsonIgnore
	public double getRight() {
		return bbox.get(2);
	}

	/**
	 * Gets top (y2)
	 * 
	 * @return top
	 */
	@JsonIgnore
	public double getTop() {
		return bbox.get(3);
	}

	/**
	 * Gets zoom
	 * 
	 * @return zoom
	 */
	public long getZoom() {
		return zoom;
	}

	/**
	 * Sets zoom
	 * 
	 * @param zoom
	 */
	public void setZoom(long zoom) {
		this.zoom = zoom;
	}

	/**
	 * Key definition
	 * 
	 * Used when saving location specific data.
	 * 
	 * @return key
	 */
	@JsonIgnore
	public String getKey() {
		return this.srs + "_" + this.bbox.get(0) + "_" + this.bbox.get(1) + "_"
				+ this.bbox.get(2) + "_" + this.bbox.get(3) + "_" + this.zoom;
	}

	/**
	 * Creates envelope of the location
	 *
	 * @return envelope
	 */
	@JsonIgnore
	public ReferencedEnvelope getEnvelope() {
		if(this.envelope == null) {
			if(this.crs == null) {
				try {
					this.crs = CRS.decode(this.getSrs());
				} catch (FactoryException e) {
					log.error(e, "CRS decoding failed");
				}
			}

			this.envelope = new ReferencedEnvelope(
					this.getLeft(), // x1
					this.getRight(), // x2
					this.getBottom(), // y1
					this.getTop(), // y2
					this.crs
			);
		}
		return this.envelope;
	}

	/**
	 * Transforms envelope to target CRS
	 *
	 * @param bbox
	 * @param target
	 * @param lenient
	 * @return envelope
	 */
	@JsonIgnore
	public ReferencedEnvelope getTransformEnvelope(ReferencedEnvelope bbox, String target, boolean lenient) {
		if(bbox == null) {
			if(this.envelope == null) {
				this.getEnvelope();
			}
			bbox = this.envelope;
            if(this.getSrs().equals(target)) {
                return bbox;
            }
		}

		CoordinateReferenceSystem targetCRS = null;
		ReferencedEnvelope envelope = null;

		try {
			targetCRS = CRS.decode(target);
			envelope = bbox.transform(targetCRS, lenient);
		} catch (TransformException e) {
			log.error(e, "Transforming failed");
		} catch (FactoryException e) {
			log.error(e, "CRS decoding on transform failed");
		} catch (Exception e) {
			log.error(e, "Creating envelope on transform failed");
		}

		return envelope;
	}

	/**
	 * Transforms envelope to target CRS
	 *
	 * @param target
	 * @param lenient
	 * @return envelope
	 */
	@JsonIgnore
	public ReferencedEnvelope getTransformEnvelope(String target, boolean lenient) {
		return getTransformEnvelope(null, target, lenient);
	}

    @JsonIgnore
    /**
     * Creates a transform object for geometries
     *
     * Transforms to Location's CRS (client's).
     *
     * @param source
     * @param lenient
     * @return transform
     */
    public MathTransform getTransformForClient(String source, boolean lenient) {
        CoordinateReferenceSystem sourceCRS = null;
        try {
            sourceCRS = CRS.decode(source);
        } catch (Exception e) {
            log.error(e, "Creating transform CRSs failed");
        }

        return getTransformForClient(sourceCRS, lenient);
    }

    @JsonIgnore
    /**
     * Creates a transform object for geometries
     *
     * Transforms to Location's CRS (client's).
     *
     * @param source
     * @param lenient
     * @return transform
     */
    public MathTransform getTransformForClient(CoordinateReferenceSystem source, boolean lenient) {
        if(this.crs == null) {
            this.getEnvelope();
        }

        return this.getTransform(source, this.crs, lenient);
    }

    @JsonIgnore
    /**
     * Creates a transform object for geometries
     *
     * Transforms to Service's CRS.
     *
     * @param target
     * @param lenient
     * @return transform
     */
    public MathTransform getTransformForService(String target, boolean lenient) {
        CoordinateReferenceSystem targetCRS = null;
        try {
            targetCRS = CRS.decode(target);
        } catch (Exception e) {
            log.error(e, "Creating transform CRSs failed");
        }

        return getTransformForService(targetCRS, lenient);
    }

    @JsonIgnore
    /**
     * Creates a transform object for geometries
     *
     * Transforms to Service's CRS.
     *
     * @param target
     * @param lenient
     * @return transform
     */
    public MathTransform getTransformForService(CoordinateReferenceSystem target, boolean lenient) {
        if(this.crs == null) {
            this.getEnvelope();
        }

        return this.getTransform(this.crs, target, lenient);
    }

    @JsonIgnore
    /**
     * Creates a transform object for geometries
     *
     * @param source
     * @param target
     * @param lenient
     * @return transform
     */
    public MathTransform getTransform(CoordinateReferenceSystem source, CoordinateReferenceSystem target, boolean lenient) {
        try {
            return CRS.findMathTransform(source, target, lenient);
        } catch (Exception e) {
            log.error(e, "Transforming failed");
        }
        return null;
    }

	/**
	 * Print format
	 * 
	 * @return object description
	 */
	@JsonIgnore
	public String toString() {
		return "srs: " + this.srs + ", left: " + this.bbox.get(0) + // x1
				", bottom: " + this.bbox.get(1) + // y1
				", right: " + this.bbox.get(2) + // x2
				", top: " + this.bbox.get(3) + // y2
				", zoom: " + this.zoom;
	}
}
