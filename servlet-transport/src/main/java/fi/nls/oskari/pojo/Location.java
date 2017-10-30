package fi.nls.oskari.pojo;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.util.ArrayList;
import java.util.List;

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
	private CoordinateReferenceSystem mapcrs = null;
    private ReferencedEnvelope enlargedEnvelope = null;

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
     * Gets CRS
     *
     * @return crs
     */
    @JsonIgnore
    public CoordinateReferenceSystem getCrs() {
        if(this.crs == null) {
            this.getEnvelope();
        }
        return this.crs;
    }
    
    /**
     * Gets CRS for Map (easting-westing) 
     *
     * @return crs
     */
    @JsonIgnore
    public CoordinateReferenceSystem getCrsForMap() {
        if(this.mapcrs == null) {
            try {
                this.mapcrs = CRS.decode(this.getSrs(),true);
            } catch (NoSuchAuthorityCodeException e) {
                log.warn(e, "Couldn't setup CoordinateReferenceSystem");
            } catch (FactoryException e) {
                log.warn(e, "Couldn't setup CoordinateReferenceSystem");
            }
            
        }
        return this.mapcrs;
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
					this.crs = CRS.decode(this.getSrs(), true);
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
     * Creates envelope of the location
     *
     * @return envelope
     */
    @JsonIgnore
    public ReferencedEnvelope getEnvelopeForMap() {
        if(this.envelope == null) {
            if(this.mapcrs == null) {
                try {
                    this.mapcrs = CRS.decode(this.getSrs(),true);
                } catch (FactoryException e) {
                    log.error(e, "CRS decoding failed");
                }
            }

            this.envelope = new ReferencedEnvelope(
                    this.getLeft(), // x1
                    this.getRight(), // x2
                    this.getBottom(), // y1
                    this.getTop(), // y2
                    this.mapcrs
            );
        }
        return this.envelope;
    }

    /**
     * Creates envelope of the location with crs ignored
     *
     * @return envelope
     */
    @JsonIgnore
    public ReferencedEnvelope getEnvelopeForMapNoCrs() {


            return new ReferencedEnvelope(
                    this.getLeft(), // x1
                    this.getRight(), // x2
                    this.getBottom(), // y1
                    this.getTop(), // y2
                    null
            );

    }

	/**
	 * Transforms envelope to target CRS
	 *
	 * @param env
	 * @param target
	 * @param lenient
	 * @return envelope
	 */
	@JsonIgnore
	public ReferencedEnvelope getTransformEnvelope(ReferencedEnvelope env, String target, boolean lenient) {
		if(env == null) {
			if(this.envelope == null) {
				this.getEnvelope();
			}
            env = this.envelope;
            if(this.getSrs().equals(target)) {
                return env;
            }
		}

		CoordinateReferenceSystem targetCRS = null;
		ReferencedEnvelope envelope = null;

		try {
			targetCRS = CRS.decode(target, true);
			envelope = env.transform(targetCRS, lenient);
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

    /**
     * Creates a transform object for geometries
     *
     * Transforms to Location's CRS (client's).
     *
     * @param source
     * @param lenient
     * @return transform
     */
    @JsonIgnore
    public MathTransform getTransformForClient(String source, boolean lenient) {
        CoordinateReferenceSystem sourceCRS = null;
        try {
            sourceCRS = CRS.decode(source, true);
        } catch (Exception e) {
            log.error(e, "Creating transform CRSs failed");
        }

        return getTransformForClient(sourceCRS, lenient);
    }

    /**
     * Creates a transform object for geometries
     *
     * Transforms to Location's CRS (client's).
     *
     * @param source
     * @param lenient
     * @return transform
     */
    @JsonIgnore
    public MathTransform getTransformForClient(CoordinateReferenceSystem source, boolean lenient) {
        if(this.crs == null) {
            this.getEnvelope();
        }

        return this.getTransform(source, this.crs, lenient);
    }

    /**
     * Creates a transform object for geometries
     *
     * Transforms to Service's CRS.
     *
     * @param target
     * @param lenient
     * @return transform
     */
    @JsonIgnore
    public MathTransform getTransformForService(String target, boolean lenient) {
        CoordinateReferenceSystem targetCRS = null;
        try {
            targetCRS = CRS.decode(target, true);
        } catch (Exception e) {
            log.error(e, "Creating transform CRSs failed");
        }

        return getTransformForService(targetCRS, lenient);
    }

    /**
     * Creates a transform object for geometries
     *
     * Transforms to Service's CRS.
     *
     * @param target
     * @param lenient
     * @return transform
     */
    @JsonIgnore
    public MathTransform getTransformForService(CoordinateReferenceSystem target, boolean lenient) {
        if(this.crs == null) {
            this.getEnvelope();
        }

        return getTransform(this.crs, target, lenient);
    }

    /**
     * Creates a transform object for geometries
     *
     * @param source
     * @param target
     * @param lenient
     * @return transform
     */
    @JsonIgnore
    public static MathTransform getTransform(CoordinateReferenceSystem source, CoordinateReferenceSystem target, boolean lenient) {
        try {
            return CRS.findMathTransform(source, target, lenient);
        } catch (Exception e) {
            log.error(e, "Transforming failed");
        }
        return null;
    }

    /**
     * Creates a scaled envelope
     *
     * The scale factor must be greather than 0. A value greater than 1 will grow the bounds whereas
     * a value of less than 1 will shrink the bounds.
     *
     * @param factor
     * @return envelope
     */
    @JsonIgnore
    public ReferencedEnvelope getScaledEnvelope(double factor) {
        if(factor <= 0) {
            log.error("Scaling failed because invalid factor value (should be greater than 0)", factor);
            return null;
        }

        ReferencedEnvelope envelope = this.getEnvelope();
        double width = envelope.getWidth() * (factor - 1.0) / 2.0;
        double height = envelope.getHeight() * (factor - 1.0) / 2.0;

        return new ReferencedEnvelope(
                this.getLeft() - width, // x1
                this.getRight() + width, // x2
                this.getBottom() - height, // y1
                this.getTop() + height, // y2
                this.crs
        );
    }

    /**
     * Sets an enlarged envelope
     *
     * Adds one tile sized buffer to every direction of the envelope.
     *
     * @param bbox
     */
    @JsonIgnore
    public void setEnlargedEnvelope(List<Double> bbox) {
        if(bbox.size() != 4) {
            log.error("Failed to create enlarged envelope because bbox was invalid");
            return;
        }

        if(this.crs == null) {
            this.getEnvelope();
        }

        double width = bbox.get(2) - bbox.get(0);
        double height = bbox.get(3) - bbox.get(1);

        this.enlargedEnvelope = createEnlargedEnvelope(width, height);
    }


    /**
     * Creates an enlarged envelope
     *
     * Adds one tile sized buffer to every direction of the envelope.
     *
     * @param width
     * @param height
     * @return envelope
     */
    @JsonIgnore
    public ReferencedEnvelope createEnlargedEnvelope(double width, double height) {
        if(width < 0 || height < 0) {
            log.error("Failed to create enlarged envelope because params were invalid");
            return null;
        }

        if(this.crs == null) {
            this.getEnvelope();
        }

        return new ReferencedEnvelope(
                this.getLeft() - width, // x1
                this.getRight() + width, // x2
                this.getBottom() - height, // y1
                this.getTop() + height, // y2
                this.crs);
    }

    /**
     * Gets an enlarged envelope
     *
     * Used in safe requests of WFS data (removing possibility of missing features on boundaries)
     *
     * @return envelope
     */
    @JsonIgnore
    public ReferencedEnvelope getEnlargedEnvelope() {
        if(this.enlargedEnvelope == null) {
            log.debug("Enlarged envelope not created");
            if(this.envelope == null) {
                this.getEnvelope();
            }
            return this.envelope;
        }
        return this.enlargedEnvelope;
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
