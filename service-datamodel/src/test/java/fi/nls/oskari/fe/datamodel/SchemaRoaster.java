package fi.nls.oskari.fe.datamodel;

import fi.nls.oskari.fe.schema.XSDDatatype;
import org.apache.log4j.Logger;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.resolver.CollectionURIResolver;
import org.apache.ws.commons.schema.resolver.DefaultURIResolver;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

/* PoC Schema to Jackson Mappings */
/* - implicit ELF, INSPIRE, RYSP assumptions */
/* - will never validate anything - assuming valid XML from quality WFS services.. */

public class SchemaRoaster {

    static final String keywords[] = { "abstract", "assert", "boolean",
            "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "extends", "false",
            "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native",
            "new", "null", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch",
            "synchronized", "this", "throw", "throws", "transient", "true",
            "try", "void", "volatile", "while" };

    static Set<String> reservedWords = new HashSet<String>();

    static {
        reservedWords.addAll(Arrays.asList(keywords));
    }

    public static boolean isJavaKeyword(String keyword) {
        return (Arrays.binarySearch(keywords, keyword) >= 0);
    }

    static final Logger logger = Logger.getLogger("fi.nls.oskari.fe");

    class RoastContext {

        public final Map<String, JavaClassSource> roasttedClasses = new HashMap<String, JavaClassSource>();

        public final RoastContext base;
        public final RoastContext parent;

        public final JavaClassSource javaClass;

        RoastContext() {
            javaClass = Roaster.create(JavaClassSource.class);
            parent = null;
            base = this;

        }

        RoastContext(JavaClassSource src) {
            javaClass = src;
            parent = null;
            base = this;
        }

        RoastContext(JavaClassSource src, RoastContext parent) {
            javaClass = src;
            this.parent = parent;
            this.base = parent.base;
        }

    }

    void roastType(final RoastContext roast,
            final XmlSchemaComplexContentRestriction ccrs) {
        logger.debug("roastType:" + ccrs);
    }

    long anonTypeIndex = 0;

    void roastSeq(final RoastContext roast, final XmlSchemaSequence seq) {
        logger.debug(seq);
        for (XmlSchemaSequenceMember mem : seq.getItems()) {

            if (mem instanceof XmlSchemaAny) {
                // roast..append("/* ??? ANY */");
            } else if (mem instanceof XmlSchemaChoice) {
                roastChoice(roast, (XmlSchemaChoice) mem, 0, -1L);

            } else if (mem instanceof XmlSchemaElement) {
                XmlSchemaElement memEl = (XmlSchemaElement) mem;

                roastSchemaProperty(roast, memEl, memEl.getMinOccurs(),
                        memEl.getMaxOccurs(), memEl.isNillable());
            } else if (mem instanceof XmlSchemaGroup) {
                /* ??? */
                XmlSchemaGroup memGrp = (XmlSchemaGroup) mem;
                XmlSchemaGroupParticle memGrpParticle = memGrp.getParticle();
                roastGroupParticle(roast, memGrpParticle);

            } else if (mem instanceof XmlSchemaGroupRef) {
                XmlSchemaGroupRef grpRef = (XmlSchemaGroupRef) mem;
                XmlSchemaGroupParticle memGrpParticle = grpRef.getParticle();

                roastGroupParticle(roast, memGrpParticle);

            } else if (mem instanceof XmlSchemaSequence) {
                roastSeq(roast, (XmlSchemaSequence) mem);
            }

        }
    }

    void roastType(final RoastContext roast,
            final XmlSchemaComplexContentExtension ccex) {
        logger.debug("roastType:" + ccex);
        XmlSchemaParticle particle = ccex.getParticle();

        if (particle instanceof XmlSchemaAny) {
            XmlSchemaAny any = (XmlSchemaAny) particle;
            // does not work
            roast.javaClass.addField("public Object _any;").addAnnotation(
                    "JacksonXmlProperty");

        } else if (particle instanceof XmlSchemaElement) {

            roastSchemaProperty(roast, (XmlSchemaElement) particle,
                    particle.getMinOccurs(), particle.getMaxOccurs(),
                    ((XmlSchemaElement) particle).isNillable());

        } else if (particle instanceof XmlSchemaChoice) {
            roastChoice(roast, (XmlSchemaChoice) particle,
                    particle.getMinOccurs(), particle.getMaxOccurs());

        } else if (particle instanceof XmlSchemaAll) {

            List<XmlSchemaElement> items = ((XmlSchemaAll) particle).getItems();
            for (XmlSchemaElement item : items) {
                roastSchemaProperty(roast, item, particle.getMinOccurs(),
                        particle.getMaxOccurs(),
                        ((XmlSchemaElement) particle).isNillable());
            }

        } else if (particle instanceof XmlSchemaSequence) {
            XmlSchemaSequence seq = (XmlSchemaSequence) particle;

            roastSeq(roast, seq);
        }
    }

    private void roastChoice(final RoastContext roast,
            XmlSchemaChoice particle, long minOccurs, long maxOccurs) {
        logger.debug("roastChoice:" + particle);
        // buf.append("/* CHOICE " + particle.getItems() + "*/");
        /* ??? */
        List<XmlSchemaObject> list = particle.getItems();

        for (XmlSchemaObject l : list) {

            if (l instanceof XmlSchemaElement) {

                roastSchemaProperty(roast, (XmlSchemaElement) l,
                        ((XmlSchemaElement) l).getMinOccurs(),
                        ((XmlSchemaElement) l).getMaxOccurs(),
                        ((XmlSchemaElement) l).isNillable());

            } else if (l instanceof XmlSchemaGroup) {

                roastGroupParticle(roast, ((XmlSchemaGroup) l).getParticle());

            } else if (l instanceof XmlSchemaGroupRef) {

                roastGroupParticle(roast, ((XmlSchemaGroupRef) l).getParticle());

            } else if (l instanceof XmlSchemaSequence) {

                roastSeq(roast, (XmlSchemaSequence) l);

            } else if (l instanceof XmlSchemaChoice) {
                roastChoice(roast, (XmlSchemaChoice) l, 0,
                        ((XmlSchemaChoice) l).getMinOccurs());
            }

        }

    }

    String XSD_NS = "http://www.w3.org/2001/XMLSchema";

    XmlSchemaType findXSElBaseType(XmlSchemaElement memEl) {
        if (memEl == null) {
            logger.error("memEl NULL");
            return null;
        }
        logger.debug("findXSElBaseType:" + memEl);
        if (memEl.getRef() != null) {
            return findXSElBaseType(memEl.getRef().getTarget());
        }
        return findXSBaseType(memEl.getSchemaType());
    }

    XmlSchemaType findXSBaseType(XmlSchemaType type) {
        logger.debug("findXSBaseType " + type);
        if (type == null) {
            return null;
        }

        if (type.getQName() != null
                && type.getQName().getNamespaceURI().equals(XSD_NS)) {
            return type;
        }

        if (type instanceof XmlSchemaComplexType) {
            XmlSchemaComplexType ctype = (XmlSchemaComplexType) type;
            XmlSchemaContentModel ctcm = ctype.getContentModel();
            if (ctcm != null) {
                XmlSchemaContent ccc = ctcm.getContent();

                if (ccc instanceof XmlSchemaComplexContentRestriction) {
                    XmlSchemaComplexContentRestriction ccrs = (XmlSchemaComplexContentRestriction) ccc;

                    if ((ccrs.getBaseTypeName().getNamespaceURI()
                            .equals(XSD_NS))) {
                        return type.getParent().getParent()
                                .getTypeByQName(ccrs.getBaseTypeName());
                    } else {
                        return findXSBaseType(type.getParent().getParent()
                                .getTypeByQName(ccrs.getBaseTypeName()));
                    }

                } else if (ccc instanceof XmlSchemaComplexContentExtension) {
                    XmlSchemaComplexContentExtension ccex = (XmlSchemaComplexContentExtension) ccc;

                    if ((ccex.getBaseTypeName().getNamespaceURI()
                            .equals(XSD_NS))) {
                        return type.getParent().getParent()
                                .getTypeByQName(ccex.getBaseTypeName());
                    } else {
                        return findXSBaseType(type.getParent().getParent()
                                .getTypeByQName(ccex.getBaseTypeName()));
                    }

                } else if (ccc instanceof XmlSchemaSimpleContentExtension) {
                    XmlSchemaSimpleContentExtension r = (XmlSchemaSimpleContentExtension) ccc;

                    if ((r.getBaseTypeName().getNamespaceURI().equals(XSD_NS))) {
                        return type.getParent().getParent()
                                .getTypeByQName(r.getBaseTypeName());
                    } else {
                        return findXSBaseType(type.getParent().getParent()
                                .getTypeByQName(r.getBaseTypeName()));
                    }

                } else if (ccc instanceof XmlSchemaSimpleContentRestriction) {
                    XmlSchemaSimpleContentRestriction r = (XmlSchemaSimpleContentRestriction) ccc;

                    if ((r.getBaseTypeName().getNamespaceURI().equals(XSD_NS))) {
                        return type.getParent().getParent()
                                .getTypeByQName(r.getBaseTypeName());
                    } else {
                        return findXSBaseType(type.getParent().getParent()
                                .getTypeByQName(r.getBaseTypeName()));
                    }
                }
            } else {
            }

        } else if (type instanceof XmlSchemaSimpleType) {
            XmlSchemaSimpleType stype = (XmlSchemaSimpleType) type;

            XmlSchemaSimpleTypeContent stc = stype.getContent();

            if (stc instanceof XmlSchemaSimpleTypeList) {

                logger.warn("? XmlSchemaSimpleTypeList");

            } else if (stc instanceof XmlSchemaSimpleTypeRestriction) {

                XmlSchemaSimpleTypeRestriction r = (XmlSchemaSimpleTypeRestriction) stc;
                if (r.getBaseTypeName().getNamespaceURI().equals(XSD_NS)) {
                    return type.getParent().getParent()
                            .getTypeByQName(r.getBaseTypeName());
                } else {
                    return findXSBaseType(r.getBaseType());
                }

            } else if (stc instanceof XmlSchemaSimpleTypeUnion) {
                logger.warn("? XmlSchemaSimpleTypeUnion");
            }

        }

        return null;
    }

    Map<QName, String> typesWellKnown = new HashMap<QName, String>() {

        /**
         * 
         */
        private static final long serialVersionUID = 405940734908097802L;
        {
            put(new QName("http://inspire.ec.europa.eu/schemas/base/3.3rc3/",
                    "Distance"), "fi.nls.oskari.isotc211.gco.Distance");
            put(new QName("http://www.isotc211.org/2005/gmd",
                    "LocalisedCharacterString"),
                    "fi.nls.oskari.isotc211.gmd.LocalisedCharacterString");

            put(new QName("http://www.isotc211.org/2005/gmd", "Country"),
                    "fi.nls.oskari.fe.gml.util.CodeType");
            put(new QName("http://www.isotc211.org/2005/gmd",
                    "Country_PropertyType"),
                    "fi.nls.oskari.fe.gml.util.CodeType");

            put(new QName("http://www.isotc211.org/2005/gmd", "MD_Resolution"),
                    "fi.nls.oskari.isotc211.gmd.MD_Resolution");

            put(new QName("http://inspire.ec.europa.eu/schemas/base/3.3rc3/",
                    "IdentifierPropertyType"),
                    "fi.nls.oskari.eu.inspire.schemas.base.Identifier");
            put(new QName("http://inspire.ec.europa.eu/schemas/base/3.3rc3/",
                    "Identifier"),
                    "fi.nls.oskari.eu.inspire.schemas.base.Identifier");

            put(new QName(
                    "http://inspire.ec.europa.eu/draft-schemas/bu/3.0rc3",
                    "IdentifierPropertyType"),
                    "fi.nls.oskari.eu.inspire.schemas.base.Identifier");

            // GML 3.2

             
            put(new QName("http://www.opengis.net/gml/3.2", "Null"),
                    "fi.nls.oskari.fe.gml.util.Null");
            
            put(new QName("http://www.opengis.net/gml/3.2", "ReferenceType"),
                    "fi.nls.oskari.fe.xml.util.Reference");

            put(new QName("http://www.opengis.net/gml/3.2", "BoundingShapeType"),
                    "fi.nls.oskari.fe.gml.util.BoundingProperty");
            put(new QName("http://www.opengis.net/gml/3.2",
                    "LocationPropertyType"),
                    "fi.nls.oskari.fe.gml.util.LocationProperty");

            put(new QName("http://www.opengis.net/gml/3.2",
                    "AbstractGeometryType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2",
                    "GeometricPrimitivePropertyType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2",
                    "GeometryPropertyType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2", "PointType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2", "PointPropertyType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");

            put(new QName("http://www.opengis.net/gml/3.2", "AbstractCurveType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2",
                    "AbstractCurvePropertyType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2", "LineStringType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2",
                    "CompositeCurveType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2", "CurveType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2", "CurvePropertyType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2",
                    "OrientableCurveType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2", "SolidType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2", "AbstractSolidType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2",
                    "CompositeSolidType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2",
                    "CompositeSolidType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2", "PolygonType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2",
                    "AbstractSurfaceType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2", "SurfaceType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2",
                    "SurfacePropertyType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2", "MultiSurface"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2", "MultiSurfaceType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2",
                    "MultiSurfacePropertyType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2", "AbstractSurface"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2",
                    "AbstractSurfaceType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml/3.2",
                    "AbstractSurfacePropertyType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");

            put(new QName("http://www.opengis.net/gml/3.2",
                    "DirectPositionType"),
                    "fi.nls.oskari.fe.gml.util.DirectPositionType");

            // GML 3.1
            put(new QName("http://www.opengis.net/gml", "Null"),
                    "fi.nls.oskari.fe.gml.util.Null");

            
            put(new QName("http://www.opengis.net/gml", "ReferenceType"),
                    "fi.nls.oskari.fe.xml.util.Reference");

            put(new QName("http://www.opengis.net/gml", "BoundingShapeType"),
                    "fi.nls.oskari.fe.gml.util.BoundingProperty");
            put(new QName("http://www.opengis.net/gml", "LocationPropertyType"),
                    "fi.nls.oskari.fe.gml.util.LocationProperty");

            put(new QName("http://www.opengis.net/gml", "AbstractGeometryType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml",
                    "GeometricPrimitivePropertyType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "GeometryPropertyType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "PointType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "PointPropertyType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");

            put(new QName("http://www.opengis.net/gml", "AbstractCurveType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml",
                    "AbstractCurvePropertyType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "LineStringType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "CompositeCurveType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "CurveType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "_Curve"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "CurvePropertyType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "OrientableCurveType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "SolidType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "AbstractSolidType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "CompositeSolidType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "CompositeSolidType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "PolygonType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "AbstractSurfaceType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "SurfaceType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "_Surface"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "SurfacePropertyType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "MultiSurface"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "MultiSurfaceType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml",
                    "MultiSurfacePropertyType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "AbstractSurface"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml", "AbstractSurfaceType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");
            put(new QName("http://www.opengis.net/gml",
                    "AbstractSurfacePropertyType"),
                    "fi.nls.oskari.fe.gml.util.GeometryProperty");

            put(new QName("http://www.opengis.net/gml", "DirectPositionType"),
                    "fi.nls.oskari.fe.gml.util.DirectPositionType");
        }
    };

    private String mapToWellKnownType(final RoastContext roast,
            final QName schemaTypeQn) {

        String result = typesWellKnown.get(schemaTypeQn);
        logger.debug("mapToWellKnownType:" + schemaTypeQn + " -> " + result);
        return result;
    }

    private String mapToXSDType(final QName schemaTypeQn) {
        // return "Object /* + " + schemaTypeQn.getLocalPart() + "*/";
        logger.debug("mapToXSDType:" + schemaTypeQn);
        System.out.println(schemaTypeQn.toString());
        return XSDDatatype.QNAME.get(schemaTypeQn).getType().getCanonicalName();

    }

    void roastSchemaProperty(final RoastContext roast, XmlSchemaElement memEl,
            long minOccurs, long maxOccurs, boolean isNillable) {
        logger.debug("roastSchemaProperty:" + memEl);
        String memType = "Object";
        boolean isPrimitive = false;

        QName refQn = memEl.getRef() != null ? memEl.getRef().getTargetQName()
                : null;
        QName schemaTypeQn = memEl.getSchemaTypeName();

        /*
         * if (memEl.getQName() == null) {
         * 
         * if (memEl.getRef() != null ) {
         * 
         * memEl = memEl.getRef().getTarget(); schemaTypeQn =
         * memEl.getSchemaTypeName();
         * 
         * } else {
         * 
         * 
         * } return; }
         */

        if (schemaTypeQn != null) {

            if (schemaTypeQn.getNamespaceURI().equals(
                    "http://www.w3.org/2001/XMLSchema")) {
                memType = mapToXSDType(schemaTypeQn);
            } else if (schemaTypeQn.getNamespaceURI().equals(
                    "http://www.opengis.net/gml/3.2")
                    && schemaTypeQn.getLocalPart().equals("ReferenceType")) {
                memType = "fi.nls.oskari.fe.xml.util.Reference";
            } else if (schemaTypeQn.getNamespaceURI().equals(
                    "http://www.opengis.net/gml")
                    && schemaTypeQn.getLocalPart().equals("ReferenceType")) {
                memType = "fi.nls.oskari.fe.xml.util.Reference";
            } else {
                XmlSchemaType baseType = findXSBaseType(memEl.getParent()
                        .getParent().getTypeByQName(schemaTypeQn));
                if (baseType != null) {
                    XSDDatatype xsdType = XSDDatatype.QNAME.get(baseType
                            .getQName());
                    if (xsdType != null) {
                        Class<?> cls = xsdType.getType();
                        if (cls != null) {
                            memType = cls.getCanonicalName();
                            isPrimitive = true;
                        } else {
                            memType = "String";
                            isPrimitive = true;
                        }
                    } else {
                        /* ??? */
                    }
                } else if (mapToWellKnownType(roast, schemaTypeQn) != null) {
                    memType = mapToWellKnownType(roast, schemaTypeQn);

                } else if (memEl.getSchemaType().isAnonymous()) {

                    String anonType = roastAnonMember(roast, memEl, isNillable,
                            memEl.getMaxOccurs(), memEl.getMaxOccurs());
                    if (anonType != null) {
                        memType = anonType;
                    }
                } else {
                    String anonType = roastNamedMember(roast, memEl,
                            isNillable, memEl.getMaxOccurs(),
                            memEl.getMaxOccurs());
                    if (anonType != null) {
                        memType = anonType;
                    }
                    // memType += "/* ???: " + schemaTypeQn + " */";
                }
            }
        } else if (refQn != null) {
            XmlSchemaElement refEl = memEl.getParent().getParent()
                    .getElementByQName(refQn);
            XmlSchemaType refElType = refEl.getSchemaType();
            XmlSchemaType baseType = findXSElBaseType(refEl);
            /*
             * memType = XSDDatatype.QNAME
             * .get(findXSElBaseType(memEl.getRef().getTarget())
             * .getQName()).getType().getCanonicalName();
             */
            if (mapToWellKnownType(roast, refEl.getQName()) != null) {
                memType = mapToWellKnownType(roast, refEl.getQName());
            } else if (refElType != null
                    && mapToWellKnownType(roast, refElType.getQName()) != null) {
                memType = mapToWellKnownType(roast, refElType.getQName());

            } else if (baseType != null) {
                XSDDatatype xsdType = XSDDatatype.QNAME
                        .get(baseType.getQName());
                if (xsdType != null) {
                    Class<?> cls = xsdType.getType();
                    if (cls != null) {
                        memType = xsdType.getType().getCanonicalName();
                        isPrimitive = true;
                    } else {
                        memType = "String";
                        isPrimitive = true;
                    }
                } else if (mapToWellKnownType(roast, baseType.getQName()) != null) {
                    memType = mapToWellKnownType(roast, baseType.getQName());
                } else {
                    /* ??? */
                    memType = baseType.getQName().getLocalPart();
                }
            } else if (refEl.getSchemaType().isAnonymous()) {
                memType = refQn.getLocalPart();
                String anonType = roastAnonMember(roast, memEl, isNillable,
                        memEl.getMaxOccurs(), memEl.getMaxOccurs());
                if (anonType != null) {
                    memType = anonType;
                }
            } else {
                String anonType = roastNamedMember(roast, refEl, isNillable,
                        memEl.getMaxOccurs(), memEl.getMaxOccurs());
                if (anonType != null) {
                    memType = anonType;
                }
            }
            /*
             * } else if (mapToWellKnownType(roast, memEl.getSchemaType()
             * 
             * .getQName()) != null) { memType = mapToWellKnownType(roast,
             * memEl.getSchemaType() .getQName()); } else if
             * (memEl.getSchemaType().isAnonymous()) {
             * 
             * String anonType = roastAnonMember(roast, memEl, isNillable,
             * memEl.getMaxOccurs(), memEl.getMaxOccurs()); if (anonType !=
             * null) { memType = anonType; }
             * 
             * } else { String anonType = roastNamedMember(roast, memEl,
             * isNillable, memEl.getMaxOccurs(), memEl.getMaxOccurs()); if
             * (anonType != null) { memType = anonType; }
             * 
             * }
             */

        } else if (memEl.getSchemaType() != null) {
            XmlSchemaType baseType = findXSBaseType(memEl.getSchemaType());
            if (baseType != null) {
                XSDDatatype xsdType = XSDDatatype.QNAME
                        .get(baseType.getQName());
                if (xsdType != null) {
                    Class<?> cls = xsdType.getType();
                    if (cls != null) {
                        memType = xsdType.getType().getCanonicalName();
                        isPrimitive = true;
                    } else {
                        memType = "String";
                        isPrimitive = true;
                    }
                } else if (mapToWellKnownType(roast, baseType.getQName()) != null) {
                    memType = mapToWellKnownType(roast, baseType.getQName());
                } else {
                    /* ??? */
                }
            } else if (mapToWellKnownType(roast, memEl.getSchemaType()
                    .getQName()) != null) {
                memType = mapToWellKnownType(roast, memEl.getSchemaType()
                        .getQName());
            } else if (memEl.getSchemaType().isAnonymous()) {

                String anonType = roastAnonMember(roast, memEl, isNillable,
                        memEl.getMaxOccurs(), memEl.getMaxOccurs());
                if (anonType != null) {
                    memType = anonType;
                }

            } else if (memEl.getSchemaType().getQName() != null) {
                /* ??? */
            } else {
                /* ??? */

            }

        }

        memType = memType.replaceAll("-", "_");

        if (memType.equals("java.lang.Double")) {
            memType = "String";
        }

        if (memType.equals("java.util.Calendar")) {
            memType = "String";
        }

        boolean required = true;

        if (minOccurs == 0 || isNillable) {
            required = false;
        }
        if (memEl.getMaxOccurs() == -1L || memEl.getMaxOccurs() > 1) {
            String elType = memType;
            memType = "java.util.List<" + memType + ">";

            QName member = memEl.getQName() != null ? memEl.getQName() : memEl
                    .getRef().getTargetQName();

            String memberName = member.getLocalPart().replaceAll("-", "_");
            String UmemberName = Character.toUpperCase(memberName.charAt(0))
                    + memberName.substring(1).replaceAll("-", "_");

            // note: roast has some issues with generics

            String fldInitializer = "new java.util.ArrayList<" + elType
                    + ">();";
            FieldSource<JavaClassSource> fld = roast.javaClass.addField(memType
                    + " " + memberName + " = " + fldInitializer);
            fld.addAnnotation("XmlElement")
                    .setLiteralValue("required", "false");
            fld.setPublic();

            MethodSource<JavaClassSource> setter = roast.javaClass
                    .addMethod("void set" + UmemberName + "(final " + elType
                            + " obj) {}");
            setter.setPublic().setReturnTypeVoid();
            setter.setBody("if( obj != null ) { " + memberName
                    + ".add(  obj ); }");
            setter.addAnnotation("JacksonXmlProperty")
                    .setStringValue("namespace", member.getNamespaceURI())
                    .setStringValue("localName", member.getLocalPart());

            roast.javaClass.addMethod(memType + " get" + UmemberName
                    + "() { return " + memberName + "; }");

        } else {
            QName member = memEl.getQName() != null ? memEl.getQName() : memEl
                    .getRef().getTargetQName();
            String memberName = member.getLocalPart();

            if (isNillable && isPrimitive) {
                memType = "fi.nls.oskari.fe.xml.util.NillableType<" + memType
                        + ">";
            }

            FieldSource<JavaClassSource> fld = roast.javaClass.addField();

            if (reservedWords.contains(memberName)) {
                memberName = "_" + memberName;
            }

            memberName = memberName.replaceAll("-", "_");

            fld.setPublic().setName(memberName).setType(memType);

            fld.addAnnotation("JacksonXmlProperty")
                    .setStringValue("namespace", member.getNamespaceURI())
                    .setStringValue("localName", member.getLocalPart());

            fld.addAnnotation("XmlElement")
                    .setLiteralValue("required", "false");
            // required ? "true" : "false");
        }
    }

    private String figureOutListElType(XmlSchemaElement memEl, QName name,
            boolean isNillable, long minOccurs, long maxOccurs) {
        logger.debug("figureOutListElType:" + memEl);
        // IF anonymous && complexContent with 1 member sequence element ref to
        // Type
        // RETURN memEl.getQName().getLocalPart() +
        // seq[0].getRef().getTarget().getQName().getLocalName()
        if (maxOccurs > 1) {

            // if (memEl.getSchemaType().isAnonymous()) {

            if (memEl.getSchemaType() instanceof XmlSchemaComplexType) {
                XmlSchemaComplexType ctype = (XmlSchemaComplexType) memEl
                        .getSchemaType();

                XmlSchemaParticle particle = ctype.getParticle();
                XmlSchemaContentModel cm = ctype.getContentModel();

                if (particle instanceof XmlSchemaSequence) {

                    XmlSchemaSequence seq = (XmlSchemaSequence) particle;
                    if (seq.getItems().size() == 1) {

                        XmlSchemaSequenceMember itm = seq.getItems().get(0);

                        if (itm instanceof XmlSchemaElement) {
                            XmlSchemaElement refel = (XmlSchemaElement) itm;

                            if (refel.getRef() != null
                                    && refel.getRef().getTarget() != null) {
                                QName qn = refel.getRef().getTarget()
                                        .getQName();

                                /*
                                 * if( name != null ) { return
                                 * name.getLocalPart() + "_" +
                                 * qn.getLocalPart(); } else {
                                 */
                                return qn.getLocalPart();
                                /*
                                 * }
                                 */
                            }
                        }

                    }
                }

                if (cm instanceof XmlSchemaComplexContent) {
                    XmlSchemaComplexContent cc = (XmlSchemaComplexContent) cm;
                    XmlSchemaContent content = cm.getContent();

                    if (content instanceof XmlSchemaComplexContentExtension) {
                        XmlSchemaComplexContentExtension extension = (XmlSchemaComplexContentExtension) content;
                        particle = extension.getParticle();
                        if (particle instanceof XmlSchemaSequence) {

                            XmlSchemaSequence seq = (XmlSchemaSequence) particle;
                            if (seq.getItems().size() == 1) {

                                XmlSchemaSequenceMember itm = seq.getItems()
                                        .get(0);

                                if (itm instanceof XmlSchemaElement) {
                                    XmlSchemaElement refel = (XmlSchemaElement) itm;

                                    if (refel.getRef() != null) {
                                        QName qn = refel.getRef()
                                                .getTargetQName();
                                        if (qn == null) {
                                            return null;
                                        }

                                        /*
                                         * if( name != null ) { return
                                         * name.getLocalPart() + "_" +
                                         * qn.getLocalPart(); } else {
                                         */
                                        return qn.getLocalPart();
                                        /*
                                         * }
                                         */
                                    }
                                }

                            }
                        }
                    } else if (content instanceof XmlSchemaComplexContentRestriction) {
                        XmlSchemaComplexContentRestriction restriction = (XmlSchemaComplexContentRestriction) content;
                        particle = restriction.getParticle();
                        if (particle instanceof XmlSchemaSequence) {

                            XmlSchemaSequence seq = (XmlSchemaSequence) particle;
                            if (seq.getItems().size() == 1) {

                                XmlSchemaSequenceMember itm = seq.getItems()
                                        .get(0);

                                if (itm instanceof XmlSchemaElement) {
                                    XmlSchemaElement refel = (XmlSchemaElement) itm;

                                    if (refel.getRef() != null) {
                                        QName qn = refel.getRef().getTarget()
                                                .getQName();

                                        /*
                                         * if( name != null ) { return
                                         * name.getLocalPart() + "_" +
                                         * qn.getLocalPart(); } else {
                                         */
                                        return qn.getLocalPart();
                                        /*
                                         * }
                                         */
                                    }
                                }

                            }
                        }
                    }

                }

            }

        }

        return null;
    }

    private String figureOutElType(XmlSchemaElement memEl, QName name,
            boolean isNillable, long minOccurs, long maxOccurs) {
        logger.debug("figureOutListElType:" + memEl);
        // IF anonymous && complexContent with 1 member sequence element ref to
        // Type
        // RETURN memEl.getQName().getLocalPart() +
        // seq[0].getRef().getTarget().getQName().getLocalName()
        if (maxOccurs == 1) {

            // if (memEl.getSchemaType().isAnonymous()) {

            if (memEl.getSchemaType() instanceof XmlSchemaComplexType) {
                XmlSchemaComplexType ctype = (XmlSchemaComplexType) memEl
                        .getSchemaType();

                XmlSchemaParticle particle = ctype.getParticle();
                XmlSchemaContentModel cm = ctype.getContentModel();
                if (particle instanceof XmlSchemaSequence) {

                    XmlSchemaSequence seq = (XmlSchemaSequence) particle;
                    if (seq.getItems().size() == 1) {

                        XmlSchemaSequenceMember itm = seq.getItems().get(0);

                        if (itm instanceof XmlSchemaElement) {
                            XmlSchemaElement refel = (XmlSchemaElement) itm;

                            if (refel.getRef() != null
                                    && refel.getRef().getTarget() != null) {
                                QName qn = refel.getRef().getTarget()
                                        .getQName();

                                /*
                                 * if( name != null ) { return
                                 * name.getLocalPart() + "_" +
                                 * qn.getLocalPart(); } else {
                                 */
                                return qn.getLocalPart();
                                /*
                                 * }
                                 */
                            }
                        }

                    }
                }

                if (cm instanceof XmlSchemaComplexContent) {
                    XmlSchemaComplexContent cc = (XmlSchemaComplexContent) cm;
                    XmlSchemaContent content = cm.getContent();

                    if (content instanceof XmlSchemaComplexContentExtension) {
                        XmlSchemaComplexContentExtension extension = (XmlSchemaComplexContentExtension) content;
                        particle = extension.getParticle();
                        if (particle instanceof XmlSchemaSequence) {

                            XmlSchemaSequence seq = (XmlSchemaSequence) particle;
                            if (seq.getItems().size() == 1) {

                                XmlSchemaSequenceMember itm = seq.getItems()
                                        .get(0);

                                if (itm instanceof XmlSchemaElement) {
                                    XmlSchemaElement refel = (XmlSchemaElement) itm;

                                    if (refel.getRef() != null
                                            && refel.getRef() != null
                                            && refel.getRef().getTarget() != null) {
                                        QName qn = refel.getRef().getTarget()
                                                .getQName();

                                        /*
                                         * if( name != null ) { return
                                         * name.getLocalPart() + "_" +
                                         * qn.getLocalPart(); } else {
                                         */
                                        return "java.util.List<"
                                                + qn.getLocalPart() + ">";
                                        /*
                                         * }
                                         */
                                    }
                                }

                            }
                        }
                    } else if (content instanceof XmlSchemaComplexContentRestriction) {
                        XmlSchemaComplexContentRestriction restriction = (XmlSchemaComplexContentRestriction) content;
                        particle = restriction.getParticle();
                        if (particle instanceof XmlSchemaSequence) {

                            XmlSchemaSequence seq = (XmlSchemaSequence) particle;
                            if (seq.getItems().size() == 1) {

                                XmlSchemaSequenceMember itm = seq.getItems()
                                        .get(0);

                                if (itm instanceof XmlSchemaElement) {
                                    XmlSchemaElement refel = (XmlSchemaElement) itm;

                                    if (refel.getRef() != null) {
                                        QName qn = refel.getRef().getTarget()
                                                .getQName();

                                        /*
                                         * if( name != null ) { return
                                         * name.getLocalPart() + "_" +
                                         * qn.getLocalPart(); } else {
                                         */
                                        return qn.getLocalPart();
                                        /*
                                         * }
                                         */
                                    }
                                }

                            }
                        }
                    }

                }

            }

            // }

        }

        return null;
    }

    private String roastNamedMember(RoastContext roast, XmlSchemaElement memEl,
            boolean isNillable, long minOccurs, long maxOccurs) {
        logger.debug("roastNamedMember:" + memEl.getQName());
        // String memType = memEl.getQName().getLocalPart();

        /*
         * String listMemType = figureOutListElType(memEl, null, isNillable,
         * minOccurs, maxOccurs);
         */
        /*
         * String elMemType = figureOutElType(memEl, null, isNillable,
         * minOccurs, maxOccurs);
         */

        String memType = roastSchemaElement(roast, null, memEl, isNillable,
                false);

        /*
         * if (elMemType != null) { memType = elMemType; }
         */

        /*
         * if (listMemType != null) { memType = listMemType; }
         */

        return memType;
    }

    private String roastAnonMember(RoastContext roast, XmlSchemaElement memEl,
            boolean isNillable, long minOccurs, long maxOccurs) {
        logger.debug("roastAnonMember:" + memEl);
        // String memType = memEl.getQName().getLocalPart();

        QName name = new QName("Anon", "A_" + (++anonTypeIndex));

        /*
         * String listMemType = figureOutListElType(memEl, name, isNillable,
         * minOccurs, maxOccurs); String elMemType = figureOutElType(memEl,
         * name, isNillable, minOccurs, maxOccurs);
         */
        String memType = roastSchemaElement(roast, name, memEl, isNillable,
                false);

        return memType;

    }

    String roastSchemaElement(final RoastContext roast, final QName context,
            final XmlSchemaElement el, boolean isNillable, boolean isRootElement) {
        logger.debug("roastSchemaElement:" + el.getQName());

        if (typesWellKnown.containsKey(el.getQName())) {
            return typesWellKnown.get(el.getQName());
        }

        XmlSchemaType type = el.getSchemaType();

        String classname = el.getQName() != null ? el.getQName().getLocalPart()
                : context.getLocalPart();

        logger.debug("nested Type:" + classname);
        JavaClassSource nested = null;

        if (isRootElement) {
            if (roast.roasttedClasses.get(classname) != null) {
                logger.warn(classname + " ALREADY DEFINED");
                return classname;
            }

            nested = roast.javaClass.addNestedType("public static class "
                    + classname);
            roast.roasttedClasses.put(classname, nested);
        } else {
            if (context != null) {
                classname = // roast.javaClass.getName() +
                context.getLocalPart() + "_" + classname;
            } else {
                // classname = "_"+classname;
                if (classname.charAt(0) == Character.toLowerCase(classname
                        .charAt(0))) {
                    classname = "_" + classname;
                }
            }

            if (roast.base.roasttedClasses.get(classname) != null) {
                logger.warn(classname + " ALREADY DEFINED");
                return classname;
            }

            nested = roast.base.javaClass.addNestedType("public static class "
                    + classname);
            roast.base.roasttedClasses.put(classname, nested);
        }
        nested.setName(classname);

        RoastContext nestedRoast = new RoastContext(nested, roast);

        if (isRootElement) {
            nestedRoast.javaClass.addAnnotation("JacksonXmlRootElement")
                    .setStringValue("namespace",
                            el.getQName().getNamespaceURI());
        }

        // we're always ready to get nilled
        // if (isNillable) {
        nestedRoast.javaClass
                .setSuperType("fi.nls.oskari.fe.xml.util.Nillable");
        // }

        if (el.getQName() != null) {
            FieldSource<JavaClassSource> fldNS = nestedRoast.javaClass
                    .addField();
            fldNS.setName("NS").setPublic().setStatic(true).setFinal(true)
                    .setType("String")
                    .setStringInitializer(el.getQName().getNamespaceURI());
            FieldSource<JavaClassSource> fldQN = nestedRoast.javaClass

            .addField("QName QN = new QName(NS, \""
                    + el.getQName().getLocalPart() + "\");");
            fldQN.setPublic().setStatic(true).setFinal(true);
        }

        roastSchemaType(nestedRoast, type);

        logger.debug("done with nested Type:" + classname);

        return classname;
    }

    void roastAtts(final RoastContext roast, final XmlSchemaAttributeGroup atts) {
        logger.debug("roastAtts:" + atts);
        for (XmlSchemaAttributeGroupMember att : atts.getAttributes()) {
            if (att instanceof XmlSchemaAttributeGroup) {
                roastAtts(roast, (XmlSchemaAttributeGroup) att);
            } else if (att instanceof XmlSchemaAttributeGroupRef) {
                // roastAtt(roast, (XmlSchemaAttributeGroupRef) att);
            }
        }
    }

    void roastAtts(final RoastContext roast,
            final List<XmlSchemaAttributeGroupRef> atts) {
        logger.debug("roastAtts:" + atts);
        for (XmlSchemaAttributeGroupRef att : atts) {
            roastAtts(roast, att.getRef().getTarget());

        }
    }

    void roastAttGroupOrRef(final RoastContext roast,
            List<XmlSchemaAttributeOrGroupRef> atts) {
        logger.debug("roastAttGroupOrRef:" + atts);
        for (XmlSchemaAttributeOrGroupRef att : atts) {
            roastAtt(roast, att);
        }

    }

    void roastAtt(final RoastContext roast, XmlSchemaAttributeOrGroupRef att) {
        logger.debug("roastAtt:" + att);
        if (att instanceof XmlSchemaAttribute) {

            XmlSchemaAttribute aatt = (XmlSchemaAttribute) att;
            boolean isRequired = aatt.getUse() == XmlSchemaUse.REQUIRED;

            if (aatt.getRef() != null) {

                if (aatt.getRef().getTarget() != null) {
                    aatt = aatt.getRef().getTarget();
                }
            }

            XmlSchemaForm form = aatt.getParent() != null ? aatt.getParent()
                    .getAttributeFormDefault() : XmlSchemaForm.UNQUALIFIED;

            if (aatt.isFormSpecified()) {
                form = aatt.getForm();
            }

            FieldSource<JavaClassSource> fld;

            switch (form) {
            case NONE:
                break;
            case QUALIFIED:

                fld = roast.javaClass.addField("public String "
                        + aatt.getQName().getLocalPart() + ";");
                fld.addAnnotation("XmlAttribute")
                        .setLiteralValue("required",
                                isRequired ? "true" : "false")
                        .setStringValue("name", aatt.getQName().getLocalPart())
                        .setStringValue("namespace",
                                aatt.getQName().getNamespaceURI());
                break;
            case UNQUALIFIED:
                fld = roast.javaClass.addField("public String "
                        + aatt.getQName().getLocalPart() + ";");
                fld.addAnnotation("XmlAttribute")
                        .setLiteralValue("required",
                                isRequired ? "true" : "false")
                        .setStringValue("name", aatt.getQName().getLocalPart());
                break;
            }
        } else if (att instanceof XmlSchemaAttributeGroupRef) {
            XmlSchemaAttributeGroupRef attRef = (XmlSchemaAttributeGroupRef) att;
            XmlSchemaAttributeGroup attGrp = attRef.getRef().getTarget();

            for (XmlSchemaAttributeGroupMember attGroups : attGrp
                    .getAttributes()) {

                if (attGroups instanceof XmlSchemaAttribute) {
                    roastAtt(roast, (XmlSchemaAttribute) attGroups);
                } else if (attGroups instanceof XmlSchemaAttributeGroup) {
                    roastAtts(roast, (XmlSchemaAttributeGroup) attGroups);
                } else if (attGroups instanceof XmlSchemaAttributeGroupRef) {
                    roastAtts(roast, ((XmlSchemaAttributeGroupRef) attGroups)
                            .getRef().getTarget());
                }
            }

        }

    }

    void roastSchemaType(final RoastContext roast, final XmlSchemaType type) {
        logger.debug("roastSchemaType:" + type);
        if (type instanceof XmlSchemaComplexType) {
            XmlSchemaComplexType ctype = (XmlSchemaComplexType) type;
            roastAttGroupOrRef(roast, ctype.getAttributes());

            XmlSchemaContentModel cm = ctype.getContentModel();
            XmlSchemaParticle particle = ctype.getParticle();

            if (cm != null) {

                if (cm instanceof XmlSchemaComplexContent) {

                    XmlSchemaComplexContent cc = (XmlSchemaComplexContent) cm;

                    XmlSchemaContent ccc = cc.getContent();

                    if (ccc instanceof XmlSchemaComplexContentRestriction) {
                        XmlSchemaComplexContentRestriction ccrs = (XmlSchemaComplexContentRestriction) cc
                                .getContent();

                        roastType(roast, ccrs);

                        XmlSchemaType baseType = type.getParent().getParent()
                                .getTypeByQName(ccrs.getBaseTypeName());
                        roastSchemaType(roast, baseType);

                    } else if (ccc instanceof XmlSchemaComplexContentExtension) {
                        XmlSchemaComplexContentExtension ccex = (XmlSchemaComplexContentExtension) cc
                                .getContent();

                        if (ccex.getBaseTypeName() != null) {
                            XmlSchemaType baseType = type.getParent()
                                    .getParent()
                                    .getTypeByQName(ccex.getBaseTypeName());
                            roastSchemaType(roast, baseType);
                        }

                        roastType(roast, ccex);
                    }

                } else if (cm instanceof XmlSchemaSimpleContent) {

                    XmlSchemaSimpleContent cc = (XmlSchemaSimpleContent) cm;
                    XmlSchemaContent ccc = cc.getContent();

                    if (ccc instanceof XmlSchemaComplexContentRestriction) {
                        XmlSchemaComplexContentRestriction ccrs = (XmlSchemaComplexContentRestriction) cc
                                .getContent();

                        roastType(roast, ccrs);

                        XmlSchemaType baseType = type.getParent().getParent()
                                .getTypeByQName(ccrs.getBaseTypeName());
                        roastSchemaType(roast, baseType);

                    } else if (ccc instanceof XmlSchemaComplexContentExtension) {
                        XmlSchemaComplexContentExtension ccex = (XmlSchemaComplexContentExtension) cc
                                .getContent();

                        if (ccex.getBaseTypeName() != null) {
                            XmlSchemaType baseType = type.getParent()
                                    .getParent()
                                    .getTypeByQName(ccex.getBaseTypeName());
                            roastSchemaType(roast, baseType);
                        }

                        roastType(roast, ccex);
                    }

                }
            } else if (particle != null) {

                if (particle instanceof XmlSchemaAll) {
                    List<XmlSchemaElement> items = ((XmlSchemaAll) particle)
                            .getItems();
                    for (XmlSchemaElement item : items) {
                        roastSchemaProperty(roast, item,
                                particle.getMinOccurs(),
                                particle.getMaxOccurs(),
                                ((XmlSchemaElement) particle).isNillable());
                    }

                } else if (particle instanceof XmlSchemaAny) {

                } else if (particle instanceof XmlSchemaChoice) {
                    roastChoice(roast, (XmlSchemaChoice) particle,
                            particle.getMinOccurs(), particle.getMaxOccurs());

                } else if (particle instanceof XmlSchemaElement) {
                    XmlSchemaElement memEl = (XmlSchemaElement) particle;

                    roastSchemaProperty(roast, memEl, memEl.getMinOccurs(),
                            memEl.getMaxOccurs(), memEl.isNillable());

                } else if (particle instanceof XmlSchemaGroupRef) {
                    XmlSchemaGroupRef grpRef = (XmlSchemaGroupRef) particle;
                    roastGroupParticle(roast, grpRef.getParticle());

                } else if (particle instanceof XmlSchemaSequence) {
                    roastSeq(roast, (XmlSchemaSequence) particle);
                }

            }

        } else if (type instanceof XmlSchemaSimpleType) {
            XmlSchemaSimpleType stype = (XmlSchemaSimpleType) type;

            XmlSchemaSimpleTypeContent stc = stype.getContent();

            if (stc instanceof XmlSchemaSimpleTypeList) {

            } else if (stc instanceof XmlSchemaSimpleTypeRestriction) {

                XmlSchemaSimpleTypeRestriction r = (XmlSchemaSimpleTypeRestriction) stc;

                roastSchemaType(roast, r.getBaseType());

            } else if (stc instanceof XmlSchemaSimpleTypeUnion) {

            }

        }

    }

    private void roastGroupParticle(RoastContext roast,
            XmlSchemaGroupParticle groupParticle) {
        logger.debug("roastGroupParticle:" + groupParticle);
        if (groupParticle instanceof XmlSchemaAll) {
            List<XmlSchemaElement> items = ((XmlSchemaAll) groupParticle)
                    .getItems();
            for (XmlSchemaElement item : items) {
                roastSchemaProperty(roast, item, groupParticle.getMinOccurs(),
                        groupParticle.getMaxOccurs(), false);
            }

        } else if (groupParticle instanceof XmlSchemaChoice) {
            roastChoice(roast, (XmlSchemaChoice) groupParticle,
                    groupParticle.getMinOccurs(), groupParticle.getMaxOccurs());

        } else if (groupParticle instanceof XmlSchemaSequence) {
            roastSeq(roast, (XmlSchemaSequence) groupParticle);
        }

    }

    protected final Map<String, String> defaultResolvers = new HashMap<String, String>();

    public Map<String, String> getDefaultResolvers() {
        return defaultResolvers;
    }

    public void roastSchema(final String packageName, final String subPackage,
            final String classname, final String feature,
            final String targetNS, String url) throws MalformedURLException,
            IOException {
        logger.debug("roastSchema:" + url);

        final String root = url.substring(0, url.lastIndexOf('/') + 1);
        logger.debug("schema root " + root);

        CollectionURIResolver schemaResolver = new DefaultURIResolver() {

            @Override
            public String getCollectionBaseURI() {
                return super.getCollectionBaseURI();
            }

            protected URL getURL(URL contextURL, String spec)
                    throws IOException {
                return super.getURL(contextURL, spec);
            }

            protected URL getFileURL(URL contextURL, String path)
                    throws IOException {
                throw new IOException("Forbidden");
            }

            @Override
            public InputSource resolveEntity(String targetNamespace,
                    String schemaLocation, String baseUri) {

                logger.debug("A schema resolver " + targetNamespace);
                logger.debug("A schema resolver " + schemaLocation);
                logger.debug("A schema resolver " + baseUri);

                String schemaLoc = schemaLocation.concat("");

                for (Entry<String, String> kv : defaultResolvers.entrySet()) {

                    if (schemaLoc.startsWith(kv.getKey())) {
                        schemaLoc = schemaLoc.replace(kv.getKey(),
                                kv.getValue());
                        break;
                    }
                }

                if (!isAbsolute(schemaLocation)
                        && (baseUri == null || baseUri.isEmpty())) {
                    baseUri = root;
                }

                logger.debug("P schema resolver " + targetNamespace);
                logger.debug("P schema resolver " + schemaLoc);
                logger.debug("P schema resolver " + baseUri);

                return super.resolveEntity(targetNamespace, schemaLoc, baseUri);
            }

            @Override
            public void setCollectionBaseURI(String uri) {
                super.setCollectionBaseURI(uri);
            }

        };

        String fileName = url;
        InputStream is = new URL(fileName).openStream();
        try {

            final XmlSchemaCollection schemaCol = new XmlSchemaCollection();
            schemaCol.setSchemaResolver(schemaResolver);
            schemaCol.read(new StreamSource(is));

            for (XmlSchema schema : schemaCol.getXmlSchemas()) {

                exportElements(packageName, subPackage, classname, feature,
                        targetNS, schema.getElements(), url);
            }
        } finally {
            is.close();
        }

    }

    protected static final List<String> defaultImports = Arrays
            .asList(new String[] {
                    "java.net.URI",
                    "fi.nls.oskari.fe.gml.util.CodeType",
                    "fi.nls.oskari.isotc211.gco.Distance",
                    "fi.nls.oskari.isotc211.gmd.LocalisedCharacterString",
                    "javax.xml.bind.annotation.XmlElement",
                    "com.fasterxml.jackson.annotation.JsonGetter",
                    "com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty",
                    "com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement",
                    "javax.xml.namespace.QName",
                    "javax.xml.bind.annotation.XmlAttribute",
                    "com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText",
                    "java.util.Calendar", "java.math.BigInteger"

            });

    protected void exportElements(final String packageName,
            final String subPackage, final String classname,
            final String feature, final String targetNS,
            Map<QName, XmlSchemaElement> elements, String url) {

        for (Entry<QName, XmlSchemaElement> kv : elements.entrySet()) {

            boolean match = kv.getKey().getNamespaceURI().equals(targetNS)
                    && (feature == null || kv.getKey().getLocalPart()
                            .equals(feature));

            if (match) {
                logger.debug("SCHEMA FILTER MATCH NS "
                        + kv.getKey().getNamespaceURI() + " vs " + targetNS);
                logger.debug("SCHEMA FILTER MATCH QN "
                        + kv.getKey().getLocalPart() + " vs " + feature);
            }

            if (!match) {
                continue;
            }

            XmlSchemaElement el = kv.getValue();

            logger.debug("Process " + kv.getKey());

            RoastContext roast = new RoastContext();

            final StringBuffer javaDoc = new StringBuffer();
            final String timestamp = new Date().toString();

            javaDoc.append("\n- URL " + url);
            javaDoc.append("\n- timestamp " + timestamp);

            roast.javaClass.getJavaDoc().setText(javaDoc.toString());
            roast.javaClass.setPackage(packageName + subPackage);
            roast.javaClass.setName(classname);

            roast.javaClass.addField().setPublic().setStatic(true)
                    .setFinal(true).setType("java.lang.String")
                    .setName("TIMESTAMP").setStringInitializer(timestamp);
            roast.javaClass.addField().setPublic().setStatic(true)
                    .setFinal(true).setType("java.lang.String")
                    .setName("SCHEMASOURCE").setStringInitializer(url);

            for (String i : defaultImports) {
                roast.javaClass.addImport(i);
            }

            roastSchemaElement(roast, null, el, false, true);

            logger.debug("Processed " + kv.getKey());
            logger.debug("OUTPUT" + kv.getKey());
            System.out.println(roast.javaClass.toString());

            logger.debug("Done." + kv.getKey());
        }

    }
}
