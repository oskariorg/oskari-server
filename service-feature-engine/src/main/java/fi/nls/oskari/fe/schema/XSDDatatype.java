package fi.nls.oskari.fe.schema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import fi.nls.oskari.fe.iri.Resource;

/* copy-paste-mod from Jena's XSDDatatype (jena.apache.org) */

public enum XSDDatatype {

	XSDFloat("float", Float.class), XSDDouble("double", Double.class),

	XSDint("int", Integer.class),

	/** Datatype representing xsd:long */
	XSDlong("long", Long.class),

	/** Datatype representing xsd:short */
	XSDshort("short", Short.class),

	/** Datatype representing xsd:byte */
	XSDbyte("byte", Byte.class),

	/** Datatype representing xsd:unsignedByte */
	XSDunsignedByte("unsignedByte"),

	/** Datatype representing xsd:unsignedShort */
	XSDunsignedShort("unsignedShort"),

	/** Datatype representing xsd:unsignedInt */
	XSDunsignedInt("unsignedInt"),

	/** Datatype representing xsd:unsignedLong */
	XSDunsignedLong("unsignedLong"),

	/** Datatype representing xsd:decimal */
	XSDdecimal("decimal", BigDecimal.class),

	/** Datatype representing xsd:integer */
	XSDinteger("integer", BigInteger.class),

	/** Datatype representing xsd:nonPositiveInteger */
	XSDnonPositiveInteger("nonPositiveInteger"),

	/** Datatype representing xsd:nonNegativeInteger */
	XSDnonNegativeInteger("nonNegativeInteger"),

	/** Datatype representing xsd:positiveInteger */
	XSDpositiveInteger("positiveInteger"),

	/** Datatype representing xsd:negativeInteger */
	XSDnegativeInteger("negativeInteger"),

	/** Datatype representing xsd:boolean */
	XSDboolean("boolean", Boolean.class),

	/** Datatype representing xsd:string */
	XSDstring("string", String.class),

	/** Datatype representing xsd:normalizedString */
	XSDnormalizedString("normalizedString", String.class),

	/** Datatype representing xsd:anyURI */
	// If you see this, remove commented lines.
	// Merely temporary during switch over and testing.
	// XSDanyURI("anyURI",
	// URI.class),
	XSDanyURI("anyURI", URI.class),

	/** Datatype representing xsd:token */
	XSDtoken("token"),

	/** Datatype representing xsd:Name */
	XSDName("Name"),

	/** Datatype representing xsd:QName */
	// If you see this, remove commented lines.
	// Merely temporary during switch over and testing.
	// XSDQName("QName"),
	XSDQName("QName"),

	/** Datatype representing xsd:language */
	XSDlanguage("language"),

	/** Datatype representing xsd:NMTOKEN */
	XSDNMTOKEN("NMTOKEN"),

	/** Datatype representing xsd:ENTITY */
	XSDENTITY("ENTITY"),

	/** Datatype representing xsd:ID */
	XSDID("ID"),

	/** Datatype representing xsd:NCName */
	XSDNCName("NCName"),

	/** Datatype representing xsd:IDREF */
	// If you see this, remove commented lines.
	// Merely temporary during switch over and testing.
	// XSDIDREF("IDREF"),
	XSDIDREF("IDREF"),

	/** Datatype representing xsd:NOTATION */
	// If you see this, remove commented lines.
	// Merely temporary during switch over and testing.
	// XSDNOTATION = new
	// XSDDatatype("NOTATION"),
	XSDNOTATION("NOTATION"),

	/** Datatype representing xsd:hexBinary */
	XSDhexBinary("hexBinary"),

	/** Datatype representing xsd:base64Binary */
	XSDbase64Binary("base64Binary"),

	/** Datatype representing xsd:date */
	XSDdate("date"),

	/** Datatype representing xsd:time */
	XSDtime("time"),

	/** Datatype representing xsd:dateTime */
	XSDdateTime("dateTime"),

	/** Datatype representing xsd:duration */
	XSDduration("duration"),

	/** Datatype representing xsd:gDay */
	XSDgDay("gDay"),

	/** Datatype representing xsd:gMonth */
	XSDgMonth("gMonth"),

	/** Datatype representing xsd:gYear */
	XSDgYear("gYear"),

	/** Datatype representing xsd:gYearMonth */
	XSDgYearMonth("gYearMonth"),

	/** Datatype representing xsd:gMonthDay */
	XSDgMonthDay("gMonthDay")

	;

	private Resource resource;
	private String prefixedName;

	protected Class<?> type;

	public static final String XSD = "http://www.w3.org/2001/XMLSchema";

	public static final String XSD_RDF = "http://www.w3.org/2001/XMLSchema#";

	public static Map<XSDDatatype, Resource> RESOURCE = new HashMap<XSDDatatype, Resource>();

	static {

		for (XSDDatatype k : XSDDatatype.values()) {
			RESOURCE.put(k, k.toResource());
		}

	}

	private XSDDatatype(String name) {
		this.resource = Resource.iri(XSD_RDF, name);
		this.prefixedName = XSD_RDF + name;
	}

	private XSDDatatype(String name, Class<?> type) {
		this.resource = Resource.iri(XSD_RDF, name);
		this.prefixedName = XSD_RDF + name;
		this.type = type;
	}

	/** The xsd namespace */

	/** Datatype representing xsd:float */
	;

	public Resource toResource() {
		return resource;
	}

	public String toString() {
		return prefixedName;
	}

}
