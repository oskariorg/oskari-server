package fi.nls.oskari.fe.datamodel;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaAttributeGroup;
import org.apache.ws.commons.schema.XmlSchemaAttributeGroupMember;
import org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef;
import org.apache.ws.commons.schema.XmlSchemaAttributeOrGroupRef;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaForm;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
import org.apache.ws.commons.schema.XmlSchemaGroupRef;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeList;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeUnion;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.XmlSchemaUse;
import org.apache.ws.commons.schema.resolver.CollectionURIResolver;
import org.apache.ws.commons.schema.resolver.DefaultURIResolver;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.InputSource;

import fi.nls.oskari.fe.schema.XSDDatatype;

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

    protected static void setupProxy() {

        System.setProperty("http.proxyHost", "wwwp.nls.fi");
        System.setProperty("http.proxyPort", "800");
        System.setProperty("http.nonProxyHosts",
                "*.nls.fi|127.0.0.1|*.paikkatietoikkuna.fi|*.maanmittauslaitos.fi");

    }

    @BeforeClass
    public static void setUp() throws IOException {
        setupProxy();

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
            System.err.println("memEl NULL");
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
            ;

            /* SUHT OK ELFiin */
            /*
             * MethodSource<JavaClassSource> setter = roast.javaClass
             * .addMethod("void set" + UmemberName + "(final " + memType +
             * " list) {}"); // setter.setName("set" + UmemberName);
             * setter.setPublic().setReturnTypeVoid(); //
             * setter.addParameter(memType, "list");
             * setter.setBody("if( list != null ) { " + memberName +
             * ".addAll(  list ); } else { " + memberName + ".clear();}");
             * setter.addAnnotation("JacksonXmlProperty")
             * .setStringValue("namespace", memEl.getQName().getNamespaceURI())
             * .setStringValue("localName", memEl.getQName().getLocalPart());
             * 
             * // ! WE DO NOT require ANYTHING...
             * setter.addAnnotation("XmlElement").setLiteralValue("required",
             * "false"); // required ? "true" : "false");
             * 
             * String fldInitializer = "new java.util.ArrayList<" + elType +
             * ">();"; FieldSource<JavaClassSource> fld =
             * roast.javaClass.addField(memType + " " + memberName + " = " +
             * fldInitializer); fld.setPrivate();
             */

            /* SUHT OK RYSPiin */
            /*
             * public java.util.List<_metatieto> metatieto = new
             * java.util.ArrayList<_metatieto>();
             * 
             * @JacksonXmlProperty(namespace =
             * "http://www.paikkatietopalvelu.fi/gml/kantakartta", localName =
             * "metatieto") public void setMetatieto(_metatieto obj) {
             * metatieto.add(obj); //System.err.println(obj); }
             */

            // ! WE DO NOT require ANYTHING...

            // required ? "true" : "false");

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
            // setter.setName("set" + UmemberName);
            setter.setPublic().setReturnTypeVoid();
            // setter.addParameter(memType, "list");
            setter.setBody("if( obj != null ) { " + memberName
                    + ".add(  obj ); }");
            setter.addAnnotation("JacksonXmlProperty")
                    .setStringValue("namespace", member.getNamespaceURI())
                    .setStringValue("localName", member.getLocalPart());

            /*
             * OLD roast.javaClass.addField("private " + memType + " " +
             * memberName + " = new java.util.ArrayList<" + elType + ">();");
             */

            MethodSource<JavaClassSource> getter = roast.javaClass
                    .addMethod(memType + " get" + UmemberName + "() { return "
                            + memberName + "; }");

            // getter.setName("get" + UmemberName).setReturnType(memType)
            /*
             * getter.setPublic()// .setBody("return " + memberName + ";")
             * .addAnnotation("JsonGetter");
             */
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

            // }

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

        String listMemType = figureOutListElType(memEl, null, isNillable,
                minOccurs, maxOccurs);
        String elMemType = figureOutElType(memEl, null, isNillable, minOccurs,
                maxOccurs);

        String memType = roastSchemaElement(roast, null, memEl, isNillable,
                false);

        if (elMemType != null) {
            memType = elMemType;
        }

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

        String listMemType = figureOutListElType(memEl, name, isNillable,
                minOccurs, maxOccurs);
        String elMemType = figureOutElType(memEl, name, isNillable, minOccurs,
                maxOccurs);

        String memType = roastSchemaElement(roast, name, memEl, isNillable,
                false);

        /*
         * if (elMemType != null) { memType = elMemType; }
         */
        /*
         * if (listMemType != null) { memType = listMemType; }
         */

        return memType;

    }

    String roastSchemaElement(final RoastContext roast, final QName context,
            final XmlSchemaElement el, boolean isNillable, boolean isRootElement) {
        logger.debug("roastSchemaElement:" + el.getQName());

        if (typesWellKnown.containsKey(el.getQName())) {
            return typesWellKnown.get(el.getQName());
        }

        XmlSchemaType type = el.getSchemaType();

        String classname = el.getQName().getLocalPart();

        logger.debug("nested Type:" + classname);
        JavaClassSource nested = null;

        if (isRootElement) {
            if (roast.roasttedClasses.get(classname) != null) {
                System.err.println(classname + " ALREADY DEFINED");
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
                System.err.println(classname + " ALREADY DEFINED");
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

        if (isNillable) {
            nestedRoast.javaClass
                    .setSuperType("fi.nls.oskari.fe.xml.util.Nillable");
        }

        FieldSource<JavaClassSource> fldNS = nestedRoast.javaClass.addField();
        fldNS.setName("NS").setPublic().setStatic(true).setFinal(true)
                .setType("String")
                .setStringInitializer(el.getQName().getNamespaceURI());

        FieldSource<JavaClassSource> fldQN = nestedRoast.javaClass
                .addField("QName QN = new QName(NS, \""
                        + el.getQName().getLocalPart() + "\");");
        fldQN.setPublic().setStatic(true).setFinal(true);

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

    void roastSchema(final String packageName, final String subPackage,
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

                if (schemaLoc
                        .startsWith("http://www.locationframework.eu/ELF10/")) {
                    schemaLoc = schemaLoc.replace(
                            "http://www.locationframework.eu/ELF10/",
                            "http://elfserver.kartverket.no/schemas/elf1.0/");
                }
                if (schemaLoc
                        .startsWith("http://www.locationframework.eu/ELF/")) {
                    schemaLoc = schemaLoc.replace(
                            "http://www.locationframework.eu/ELF/",
                            "http://elfserver.kartverket.no/schemas/elf1.0/");
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
                        targetNS, schema.getElements());
            }
        } finally {
            is.close();
        }

    }

    protected void exportElements(final String packageName,
            final String subPackage, final String classname,
            final String feature, final String targetNS,
            Map<QName, XmlSchemaElement> elements) {

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

            roast.javaClass.setPackage(packageName + subPackage);
            roast.javaClass.addImport("java.net.URI");
            roast.javaClass.addImport("fi.nls.oskari.fe.gml.util.CodeType");
            roast.javaClass.addImport("fi.nls.oskari.isotc211.gco.Distance");
            roast.javaClass
                    .addImport("fi.nls.oskari.isotc211.gmd.LocalisedCharacterString");
            roast.javaClass.addImport("javax.xml.bind.annotation.XmlElement");
            roast.javaClass
                    .addImport("com.fasterxml.jackson.annotation.JsonGetter");
            roast.javaClass
                    .addImport("com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty");
            roast.javaClass
                    .addImport("com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement");
            roast.javaClass.addImport("javax.xml.namespace.QName");
            roast.javaClass.addImport("javax.xml.bind.annotation.XmlAttribute");
            roast.javaClass
                    .addImport("com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText");
            roast.javaClass.addImport("java.util.Calendar");
            roast.javaClass.addImport("java.math.BigInteger");
            // final String classname = "ELF_Buildings";
            roast.javaClass.setName(classname);

            roastSchemaElement(roast, null, el, false, true);

            logger.debug("Processed " + kv.getKey());
            logger.debug("OUTPUT" + kv.getKey());
            // String formattedCode =
            // Roaster.format(roast.javaClass.toString());
            System.out.println(roast.javaClass.toString());

            logger.debug("Done." + kv.getKey());
        }

    }

    @Test
    public void testElfLoD0Building_WFS() throws MalformedURLException,
            IOException {

        Properties log4jprops = new Properties();
        log4jprops.put("log4jprops.log4j.rootLogger", "DEBUG, A1");
        log4jprops.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        log4jprops.put("log4j.appender.A1.layout",
                "org.apache.log4j.PatternLayout");
        log4jprops.put("log4j.appender.A1.layout.ConversionPattern",
                "%-4r [%t] %-5p %c %x - %m%n");

        org.apache.log4j.PropertyConfigurator.configure(log4jprops);

        final String url = "http://elf-wfs.maanmittauslaitos.fi/elf-wfs/services/elf-lod0bu?service=WFS&request=DescribeFeatureType&TYPENAMES=elf-lod0bu:Building&version=2.0.0&NAMESPACES=xmlns(elf-lod0bu,http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0)";

        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "Building";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "buildings";
        final String classname = "ELF_MasterLoD0_Building_nls_fi_wfs";

        final String targetNS = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0";

        roastSchema(packageName, subPackage, feature, classname, targetNS, url);

    }

    @Test
    public void testElfLoD0Building() throws MalformedURLException, IOException {

        Properties log4jprops = new Properties();
        log4jprops.put("log4jprops.log4j.rootLogger", "DEBUG, A1");
        log4jprops.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        log4jprops.put("log4j.appender.A1.layout",
                "org.apache.log4j.PatternLayout");
        log4jprops.put("log4j.appender.A1.layout.ConversionPattern",
                "%-4r [%t] %-5p %c %x - %m%n");

        org.apache.log4j.PropertyConfigurator.configure(log4jprops);

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD0_Buildings.xsd";

        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "Building";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "buildings";
        final String classname = "ELF_MasterLoD0_Building";

        final String targetNS = "http://www.locationframework.eu/schemas/Buildings/MasterLoD0/1.0";

        roastSchema(packageName, subPackage, classname, feature, targetNS, url);

    }

    @Test
    public void testElfLoD0Address() throws MalformedURLException, IOException {

        Properties log4jprops = new Properties();
        log4jprops.put("log4jprops.log4j.rootLogger", "DEBUG, A1");
        log4jprops.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        log4jprops.put("log4j.appender.A1.layout",
                "org.apache.log4j.PatternLayout");
        log4jprops.put("log4j.appender.A1.layout.ConversionPattern",
                "%-4r [%t] %-5p %c %x - %m%n");

        org.apache.log4j.PropertyConfigurator.configure(log4jprops);

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD0_Addresses.xsd";

        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "Address";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "addresses";
        final String classname = "ELF_MasterLoD0_Address";

        final String targetNS = "http://www.locationframework.eu/schemas/Addresses/MasterLoD0/1.0";

        roastSchema(packageName, subPackage, classname, feature, targetNS, url);

    }

    @Test
    public void testElfLoD0CadastralParcels() throws MalformedURLException,
            IOException {

        Properties log4jprops = new Properties();
        log4jprops.put("log4jprops.log4j.rootLogger", "DEBUG, A1");
        log4jprops.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        log4jprops.put("log4j.appender.A1.layout",
                "org.apache.log4j.PatternLayout");
        log4jprops.put("log4j.appender.A1.layout.ConversionPattern",
                "%-4r [%t] %-5p %c %x - %m%n");

        org.apache.log4j.PropertyConfigurator.configure(log4jprops);

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD0_CadastralParcels.xsd";

        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "CadastralParcel";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "cadastralparcels";
        final String classname = "ELF_MasterLoD0_CadastralParcel";

        final String targetNS = "http://www.locationframework.eu/schemas/CadastralParcels/MasterLoD0/1.0";

        roastSchema(packageName, subPackage, classname, feature, targetNS, url);

    }

    @Test
    public void testElfLoD1AdministrativeUnit() throws MalformedURLException,
            IOException {

        Properties log4jprops = new Properties();
        log4jprops.put("log4jprops.log4j.rootLogger", "DEBUG, A1");
        log4jprops.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        log4jprops.put("log4j.appender.A1.layout",
                "org.apache.log4j.PatternLayout");
        log4jprops.put("log4j.appender.A1.layout.ConversionPattern",
                "%-4r [%t] %-5p %c %x - %m%n");

        org.apache.log4j.PropertyConfigurator.configure(log4jprops);

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_AdministrativeUnits.xsd";

        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "AdministrativeUnit";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "administrativeunits";
        final String classname = "ELF_MasterLoD1_AdministrativeUnit";

        final String targetNS = "http://www.locationframework.eu/schemas/AdministrativeUnits/MasterLoD1/1.0";

        roastSchema(packageName, subPackage, classname, feature, targetNS, url);

    }

    @Test
    public void testElfLoD1AdministrativeBoundary()
            throws MalformedURLException, IOException {

        Properties log4jprops = new Properties();
        log4jprops.put("log4jprops.log4j.rootLogger", "DEBUG, A1");
        log4jprops.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        log4jprops.put("log4j.appender.A1.layout",
                "org.apache.log4j.PatternLayout");
        log4jprops.put("log4j.appender.A1.layout.ConversionPattern",
                "%-4r [%t] %-5p %c %x - %m%n");

        org.apache.log4j.PropertyConfigurator.configure(log4jprops);

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_AdministrativeUnits.xsd";

        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "AdministrativeBoundary";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "administrativeunits";
        final String classname = "ELF_MasterLoD1_AdministrativeBoundary";

        final String targetNS = "http://www.locationframework.eu/schemas/AdministrativeUnits/MasterLoD1/1.0";

        roastSchema(packageName, subPackage, classname, feature, targetNS, url);

    }

    @Test
    public void testElfLoD1GeographicalNames() throws MalformedURLException,
            IOException {

        Properties log4jprops = new Properties();
        log4jprops.put("log4jprops.log4j.rootLogger", "DEBUG, A1");
        log4jprops.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        log4jprops.put("log4j.appender.A1.layout",
                "org.apache.log4j.PatternLayout");
        log4jprops.put("log4j.appender.A1.layout.ConversionPattern",
                "%-4r [%t] %-5p %c %x - %m%n");

        org.apache.log4j.PropertyConfigurator.configure(log4jprops);

        final String url = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_GeographicalNames.xsd";

        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "NamedPlace";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "geographicalnames";
        final String classname = "ELF_MasterLoD1_NamedPlace";

        final String targetNS = "http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0";

        roastSchema(packageName, subPackage, classname, feature, targetNS, url);

    }

    @Ignore("Incomplete")
    @Test
    public void testRYSPRakennusvalvontaRakennusvalvonta()
            throws MalformedURLException, IOException {

        Properties log4jprops = new Properties();
        log4jprops.put("log4jprops.log4j.rootLogger", "DEBUG, A1");
        log4jprops.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        log4jprops.put("log4j.appender.A1.layout",
                "org.apache.log4j.PatternLayout");
        log4jprops.put("log4j.appender.A1.layout.ConversionPattern",
                "%-4r [%t] %-5p %c %x - %m%n");

        org.apache.log4j.PropertyConfigurator.configure(log4jprops);

        final String url = "http://www.paikkatietopalvelu.fi/gml/rakennusvalvonta/2.1.6/rakennusvalvonta.xsd";

        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "Rakennusvalvonta";
        final String packageName = "fi.nls.oskari.fi.rysp.";
        final String subPackage = "rakennusvalvonta";
        final String classname = "RYSP_rakennusvalvonta_Rakennusvalvonta";

        final String targetNS = "http://www.paikkatietopalvelu.fi/gml/rakennusvalvonta";

        roastSchema(packageName, subPackage, classname, feature, targetNS, url);

    }

    @Test
    public void testRYSPKantakarttaRakennus() throws MalformedURLException,
            IOException {

        Properties log4jprops = new Properties();
        log4jprops.put("log4jprops.log4j.rootLogger", "DEBUG, A1");
        log4jprops.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        log4jprops.put("log4j.appender.A1.layout",
                "org.apache.log4j.PatternLayout");
        log4jprops.put("log4j.appender.A1.layout.ConversionPattern",
                "%-4r [%t] %-5p %c %x - %m%n");

        org.apache.log4j.PropertyConfigurator.configure(log4jprops);

        final String url = "http://www.paikkatietopalvelu.fi/gml/kantakartta/2.0.1/kantakartta.xsd";

        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "Rakennus";
        final String packageName = "fi.nls.oskari.fi.rysp.";
        final String subPackage = "kantakartta";
        final String classname = "RYSP_kanta_Rakennus";

        final String targetNS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";

        roastSchema(packageName, subPackage, classname, feature, targetNS, url);

    }

    @Test
    public void testRYSPKantakarttaLiikennevayla()
            throws MalformedURLException, IOException {

        Properties log4jprops = new Properties();
        log4jprops.put("log4jprops.log4j.rootLogger", "DEBUG, A1");
        log4jprops.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        log4jprops.put("log4j.appender.A1.layout",
                "org.apache.log4j.PatternLayout");
        log4jprops.put("log4j.appender.A1.layout.ConversionPattern",
                "%-4r [%t] %-5p %c %x - %m%n");

        org.apache.log4j.PropertyConfigurator.configure(log4jprops);

        final String url = "http://www.paikkatietopalvelu.fi/gml/kantakartta/2.0.1/kantakartta.xsd";

        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");

        final String feature = "Liikennevayla";
        final String packageName = "fi.nls.oskari.fi.rysp.";
        final String subPackage = "kantakartta";
        final String classname = "RYSP_kanta_Liikennevayla";

        final String targetNS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";

        roastSchema(packageName, subPackage, classname, feature, targetNS, url);

    }

    
    @Ignore("Schema failture")
    @Test
    public void testInspireTnRoRoadLink()
            throws MalformedURLException, IOException {
        Properties log4jprops = new Properties();
        log4jprops.put("log4jprops.log4j.rootLogger", "DEBUG, A1");
        log4jprops.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        log4jprops.put("log4j.appender.A1.layout",
                "org.apache.log4j.PatternLayout");
        log4jprops.put("log4j.appender.A1.layout.ConversionPattern",
                "%-4r [%t] %-5p %c %x - %m%n");

        org.apache.log4j.PropertyConfigurator.configure(log4jprops);

        final String url = "http://www.ign.es/wfs-inspire/transportes-btn100?SERVICE=WFS&VERSION=2.0.0&REQUEST=DescribeFeatureType&OUTPUTFORMAT=application%2Fgml%2Bxml%3B+version%3D3.2&TYPENAME=tn-ro:RoadLink&NAMESPACES=xmlns(tn-ro,urn%3Ax-inspire%3Aspecification%3Agmlas%3ARoadTransportNetwork%3A3.0)";

        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");
        
        final String feature = "RoadLink";
        final String packageName = "fi.nls.oskari.eu.inspire.";
        final String subPackage = "roadtransportnetwork";
        final String classname = "INSPIRE_tnro_RoadLink";

        final String targetNS = "urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0";

        roastSchema(packageName, subPackage, classname, feature, targetNS, url);

        
        
        
    }
    
    @Test
    public void testELFTnRoRoadLink()
            throws MalformedURLException, IOException {
        Properties log4jprops = new Properties();
        log4jprops.put("log4jprops.log4j.rootLogger", "DEBUG, A1");
        log4jprops.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        log4jprops.put("log4j.appender.A1.layout",
                "org.apache.log4j.PatternLayout");
        log4jprops.put("log4j.appender.A1.layout.ConversionPattern",
                "%-4r [%t] %-5p %c %x - %m%n");

        org.apache.log4j.PropertyConfigurator.configure(log4jprops);

        final String url = "http://elf-wfs.maanmittauslaitos.fi/elf-wfs/services/elf-lod1rdtn?SERVICE=WFS&VERSION=2.0.0&REQUEST=DescribeFeatureType&TYPENAME=elf_lod1rtn:RoadLink&NAMESPACES=xmlns(elf_lod1rtn,http%3A%2F%2Fwww.locationframework.eu%2Fschemas%2FRoadTransportNetwork%2FMasterLoD1%2F1.0)";

        logger.setLevel(Level.DEBUG);

        logger.debug(url);

        logger.debug("OUTPUT");
        
        final String feature = "RoadLink";
        final String packageName = "fi.nls.oskari.eu.elf.";
        final String subPackage = "roadtransportnetwork";
        final String classname = "ELF_TNRO_RoadLink";

        final String targetNS = "http://www.locationframework.eu/schemas/RoadTransportNetwork/MasterLoD1/1.0";

        roastSchema(packageName, subPackage, classname, feature, targetNS, url);

        
        
        
    }
    
    
}
