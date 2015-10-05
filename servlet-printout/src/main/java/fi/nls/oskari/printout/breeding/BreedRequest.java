package fi.nls.oskari.printout.breeding;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import java.util.Date;

public class BreedRequest implements Constants {

	Long id = null;

	Date timestamp = new Date();

	private String layerName = null;

	private String gridSubsetName = null;
	private String title = null;
	private String description = null;
	private String sourceUrl = null;
	private String sourceSchema = null;

	private String sourceClob = null;
	private byte[] sourceBlob = null;

	private Geometry p = null;
	private Polygon maxExtent = null;

	private javax.xml.transform.Source xml;

	int zoomStart;

	int zoomStop;

	private BreedRequestType breedRequestType;
	Status status = Status.INITIAL;

	int percentComplete = 0;
	public BreedRequestType getBreedRequestType() {
		return breedRequestType;
	}
	public String getDescription() {
		return description;
	}

	public String getGridSubsetName() {
		return gridSubsetName;
	}

	public Long getId() {
		return id;
	}

	public String getLayerName() {
		return layerName;
	}

	public Polygon getMaxExtent() {
		return maxExtent;
	}

	public Geometry getP() {
		return p;
	}

	public int getPercentComplete() {
		return percentComplete;
	}

	public byte[] getSourceBlob() {
		return sourceBlob;
	}

	public String getSourceClob() {
		return sourceClob;
	}

	public String getSourceSchema() {
		return sourceSchema;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public Status getStatus() {
		return status;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getTitle() {
		return title;
	}

	public javax.xml.transform.Source getXml() {
		return xml;
	}

	public int getZoomStart() {
		return zoomStart;
	}

	public int getZoomStop() {
		return zoomStop;
	}

	public void setBreedRequestType(BreedRequestType breedRequestType) {
		this.breedRequestType = breedRequestType;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setGridSubsetName(String gridSubsetName) {
		this.gridSubsetName = gridSubsetName;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}

	public void setMaxExtent(Polygon maxExtent) {
		this.maxExtent = maxExtent;
	}

	public void setP(Geometry p) {
		this.p = p;
	}

	public void setPercentComplete(int percentComplete) {
		this.percentComplete = percentComplete;
	}

	public void setSourceBlob(byte[] sourceBlob) {
		this.sourceBlob = sourceBlob;
	}

	public void setSourceClob(String sourceClob) {
		this.sourceClob = sourceClob;
	}

	public void setSourceSchema(String sourceSchema) {
		this.sourceSchema = sourceSchema;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setXml(javax.xml.transform.Source xml) {
		this.xml = xml;
	}

	public void setZoomStart(int zoomStart) {
		this.zoomStart = zoomStart;
	}

	public void setZoomStop(int zoomStop) {
		this.zoomStop = zoomStop;
	}

}
