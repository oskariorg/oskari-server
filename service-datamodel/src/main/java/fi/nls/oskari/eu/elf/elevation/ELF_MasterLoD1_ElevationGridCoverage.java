package fi.nls.oskari.eu.elf.elevation;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fi.nls.oskari.eu.inspire.schemas.base.Identifier;
import fi.nls.oskari.fe.gml.util.BoundingProperty;
import fi.nls.oskari.fe.gml.util.GeometryProperty;
import fi.nls.oskari.fe.gml.util.LocationProperty;
import fi.nls.oskari.fe.xml.util.Nillable;
import fi.nls.oskari.fe.xml.util.NillableType;
import fi.nls.oskari.fe.xml.util.Reference;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;
import java.net.URI;

/**
 * 
- URL http://elfserver.kartverket.no/schemas/elf1.0/LoD1_Elevation.xsd
- timestamp Wed Dec 17 10:56:46 EET 2014
 */
public class ELF_MasterLoD1_ElevationGridCoverage
{

   public static final String TIMESTAMP = "Wed Dec 17 10:56:46 EET 2014";
   public static final String SCHEMASOURCE = "http://elfserver.kartverket.no/schemas/elf1.0/LoD1_Elevation.xsd";

   @JacksonXmlRootElement(namespace = "http://www.locationframework.eu/schemas/Elevation/MasterLoD1/1.0")
   public static class ElevationGridCoverage extends Nillable
   {
      public static final String NS = "http://www.locationframework.eu/schemas/Elevation/MasterLoD1/1.0";
      public static final QName QN = new QName(NS, "ElevationGridCoverage");
      @XmlAttribute(required = true, name = "id")
      public String id;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "boundedBy")
      @XmlElement(required = false)
      public BoundingProperty boundedBy;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "location")
      @XmlElement(required = false)
      public LocationProperty location;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "domainSet")
      @XmlElement(required = false)
      public _domainSet domainSet;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "rangeSet")
      @XmlElement(required = false)
      public _rangeSet rangeSet;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "coverageFunction")
      @XmlElement(required = false)
      public _coverageFunction coverageFunction;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gmlcov/1.0", localName = "rangeType")
      @XmlElement(required = false)
      public _rangeType rangeType;
      @XmlElement(required = false)
      public java.util.List<A_2_A_2> metadata = new java.util.ArrayList<A_2_A_2>();
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Elevation/MasterLoD1/1.0", localName = "beginLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> beginLifespanVersion;
      @XmlElement(required = false)
      public java.util.List<A_3_domainExtent> domainExtent = new java.util.ArrayList<A_3_domainExtent>();
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Elevation/MasterLoD1/1.0", localName = "endLifespanVersion")
      @XmlElement(required = false)
      public NillableType<String> endLifespanVersion;
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Elevation/MasterLoD1/1.0", localName = "inspireId")
      @XmlElement(required = false)
      public Identifier inspireId;
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Elevation/MasterLoD1/1.0", localName = "propertyType")
      @XmlElement(required = false)
      public String propertyType;
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Elevation/MasterLoD1/1.0", localName = "surfaceType")
      @XmlElement(required = false)
      public String surfaceType;
      @XmlElement(required = false)
      public java.util.List<A_5_contributingElevationGridCoverage> contributingElevationGridCoverage = new java.util.ArrayList<A_5_contributingElevationGridCoverage>();

      @JacksonXmlProperty(namespace = "http://www.opengis.net/gmlcov/1.0", localName = "metadata")
      public void setMetadata(final A_2_A_2 obj)
      {
         if (obj != null)
         {
            metadata.add(obj);
         }
      }

      java.util.List<A_2_A_2> getMetadata()
      {
         return metadata;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Elevation/MasterLoD1/1.0", localName = "domainExtent")
      public void setDomainExtent(final A_3_domainExtent obj)
      {
         if (obj != null)
         {
            domainExtent.add(obj);
         }
      }

      java.util.List<A_3_domainExtent> getDomainExtent()
      {
         return domainExtent;
      }

      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Elevation/MasterLoD1/1.0", localName = "contributingElevationGridCoverage")
      public void setContributingElevationGridCoverage(
            final A_5_contributingElevationGridCoverage obj)
      {
         if (obj != null)
         {
            contributingElevationGridCoverage.add(obj);
         }
      }

      java.util.List<A_5_contributingElevationGridCoverage> getContributingElevationGridCoverage()
      {
         return contributingElevationGridCoverage;
      }
   }

   public static class _domainSet extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "domainSet");
      @XmlAttribute(required = false, name = "owns")
      public String owns;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "AbstractGeometry")
      @XmlElement(required = false)
      public GeometryProperty AbstractGeometry;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "AbstractTimeObject")
      @XmlElement(required = false)
      public AbstractTimeObject AbstractTimeObject;
   }

   public static class AbstractTimeObject extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "AbstractTimeObject");
      @XmlAttribute(required = true, name = "id")
      public String id;
   }

   public static class _rangeSet extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "rangeSet");
      @XmlElement(required = false)
      public java.util.List<ValueArray> ValueArray = new java.util.ArrayList<ValueArray>();
      @XmlElement(required = false)
      public java.util.List<AbstractScalarValueList> AbstractScalarValueList = new java.util.ArrayList<AbstractScalarValueList>();
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "DataBlock")
      @XmlElement(required = false)
      public DataBlock DataBlock;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "File")
      @XmlElement(required = false)
      public File File;

      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "ValueArray")
      public void setValueArray(final ValueArray obj)
      {
         if (obj != null)
         {
            ValueArray.add(obj);
         }
      }

      java.util.List<ValueArray> getValueArray()
      {
         return ValueArray;
      }

      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "AbstractScalarValueList")
      public void setAbstractScalarValueList(final AbstractScalarValueList obj)
      {
         if (obj != null)
         {
            AbstractScalarValueList.add(obj);
         }
      }

      java.util.List<AbstractScalarValueList> getAbstractScalarValueList()
      {
         return AbstractScalarValueList;
      }
   }

   public static class ValueArray extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "ValueArray");
      @XmlAttribute(required = true, name = "id")
      public String id;
      @XmlElement(required = false)
      public java.util.List<_valueComponent> valueComponent = new java.util.ArrayList<_valueComponent>();
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "valueComponents")
      @XmlElement(required = false)
      public _valueComponents valueComponents;

      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "valueComponent")
      public void setValueComponent(final _valueComponent obj)
      {
         if (obj != null)
         {
            valueComponent.add(obj);
         }
      }

      java.util.List<_valueComponent> getValueComponent()
      {
         return valueComponent;
      }
   }

   public static class _valueComponent extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "valueComponent");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @XmlAttribute(required = false, name = "owns")
      public String owns;
   }

   public static class _valueComponents extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "valueComponents");
      @XmlAttribute(required = false, name = "owns")
      public String owns;
   }

   public static class AbstractScalarValueList extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "AbstractScalarValueList");
   }

   public static class DataBlock extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "DataBlock");
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "rangeParameters")
      @XmlElement(required = false)
      public _rangeParameters rangeParameters;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "tupleList")
      @XmlElement(required = false)
      public _tupleList tupleList;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "doubleOrNilReasonTupleList")
      @XmlElement(required = false)
      public _doubleOrNilReasonTupleList doubleOrNilReasonTupleList;
   }

   public static class _rangeParameters extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "rangeParameters");
      @XmlAttribute(required = false, name = "owns")
      public String owns;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
   }

   public static class _tupleList extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "tupleList");
   }

   public static class _doubleOrNilReasonTupleList extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS,
            "doubleOrNilReasonTupleList");
   }

   public static class File extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "File");
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "rangeParameters")
      @XmlElement(required = false)
      public _rangeParameters rangeParameters;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "fileName")
      @XmlElement(required = false)
      public URI fileName;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "fileReference")
      @XmlElement(required = false)
      public URI fileReference;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "fileStructure")
      @XmlElement(required = false)
      public String fileStructure;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "mimeType")
      @XmlElement(required = false)
      public URI mimeType;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "compression")
      @XmlElement(required = false)
      public URI compression;
   }

   public static class _coverageFunction extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "coverageFunction");
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "MappingRule")
      @XmlElement(required = false)
      public MappingRule MappingRule;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "CoverageMappingRule")
      @XmlElement(required = false)
      public CoverageMappingRule CoverageMappingRule;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "GridFunction")
      @XmlElement(required = false)
      public GridFunction GridFunction;
   }

   public static class MappingRule extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "MappingRule");
   }

   public static class CoverageMappingRule extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "CoverageMappingRule");
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "ruleDefinition")
      @XmlElement(required = false)
      public String ruleDefinition;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "ruleReference")
      @XmlElement(required = false)
      public Reference ruleReference;
   }

   public static class GridFunction extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "GridFunction");
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "sequenceRule")
      @XmlElement(required = false)
      public String sequenceRule;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "startPoint")
      @XmlElement(required = false)
      public _startPoint startPoint;
   }

   public static class _startPoint extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "startPoint");
   }

   public static class _rangeType extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gmlcov/1.0";
      public static final QName QN = new QName(NS, "rangeType");
      @JacksonXmlProperty(namespace = "http://www.opengis.net/swe/2.0", localName = "DataRecord")
      @XmlElement(required = false)
      public DataRecord DataRecord;
   }

   public static class DataRecord extends Nillable
   {
      public static final String NS = "http://www.opengis.net/swe/2.0";
      public static final QName QN = new QName(NS, "DataRecord");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @XmlElement(required = false)
      public java.util.List<java.lang.Object> extension = new java.util.ArrayList<java.lang.Object>();
      @JacksonXmlProperty(namespace = "http://www.opengis.net/swe/2.0", localName = "identifier")
      @XmlElement(required = false)
      public URI identifier;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/swe/2.0", localName = "label")
      @XmlElement(required = false)
      public String label;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/swe/2.0", localName = "description")
      @XmlElement(required = false)
      public String description;
      @XmlElement(required = false)
      public java.util.List<A_1_field> field = new java.util.ArrayList<A_1_field>();

      @JacksonXmlProperty(namespace = "http://www.opengis.net/swe/2.0", localName = "extension")
      public void setExtension(final java.lang.Object obj)
      {
         if (obj != null)
         {
            extension.add(obj);
         }
      }

      java.util.List<java.lang.Object> getExtension()
      {
         return extension;
      }

      @JacksonXmlProperty(namespace = "http://www.opengis.net/swe/2.0", localName = "field")
      public void setField(final A_1_field obj)
      {
         if (obj != null)
         {
            field.add(obj);
         }
      }

      java.util.List<A_1_field> getField()
      {
         return field;
      }
   }

   public static class A_1_field extends Nillable
   {
      public static final String NS = "http://www.opengis.net/swe/2.0";
      public static final QName QN = new QName(NS, "field");
      @JacksonXmlProperty(namespace = "http://www.opengis.net/swe/2.0", localName = "AbstractDataComponent")
      @XmlElement(required = false)
      public AbstractDataComponent AbstractDataComponent;
   }

   public static class AbstractDataComponent extends Nillable
   {
      public static final String NS = "http://www.opengis.net/swe/2.0";
      public static final QName QN = new QName(NS, "AbstractDataComponent");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @XmlElement(required = false)
      public java.util.List<java.lang.Object> extension = new java.util.ArrayList<java.lang.Object>();
      @JacksonXmlProperty(namespace = "http://www.opengis.net/swe/2.0", localName = "identifier")
      @XmlElement(required = false)
      public URI identifier;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/swe/2.0", localName = "label")
      @XmlElement(required = false)
      public String label;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/swe/2.0", localName = "description")
      @XmlElement(required = false)
      public String description;

      @JacksonXmlProperty(namespace = "http://www.opengis.net/swe/2.0", localName = "extension")
      public void setExtension(final java.lang.Object obj)
      {
         if (obj != null)
         {
            extension.add(obj);
         }
      }

      java.util.List<java.lang.Object> getExtension()
      {
         return extension;
      }
   }

   public static class A_2_A_2 extends Nillable
   {
   }

   public static class A_3_domainExtent extends Nillable
   {
      public static final String NS = "http://www.locationframework.eu/schemas/Elevation/MasterLoD1/1.0";
      public static final QName QN = new QName(NS, "domainExtent");
      @XmlAttribute(required = false, name = "owns")
      public String owns;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "EX_Extent")
      @XmlElement(required = false)
      public EX_Extent EX_Extent;
   }

   public static class EX_Extent extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "EX_Extent");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @XmlAttribute(required = false, name = "uuid")
      public String uuid;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "description")
      @XmlElement(required = false)
      public _description description;
      @XmlElement(required = false)
      public java.util.List<_geographicElement> geographicElement = new java.util.ArrayList<_geographicElement>();
      @XmlElement(required = false)
      public java.util.List<_temporalElement> temporalElement = new java.util.ArrayList<_temporalElement>();
      @XmlElement(required = false)
      public java.util.List<_verticalElement> verticalElement = new java.util.ArrayList<_verticalElement>();

      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "geographicElement")
      public void setGeographicElement(final _geographicElement obj)
      {
         if (obj != null)
         {
            geographicElement.add(obj);
         }
      }

      java.util.List<_geographicElement> getGeographicElement()
      {
         return geographicElement;
      }

      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "temporalElement")
      public void setTemporalElement(final _temporalElement obj)
      {
         if (obj != null)
         {
            temporalElement.add(obj);
         }
      }

      java.util.List<_temporalElement> getTemporalElement()
      {
         return temporalElement;
      }

      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "verticalElement")
      public void setVerticalElement(final _verticalElement obj)
      {
         if (obj != null)
         {
            verticalElement.add(obj);
         }
      }

      java.util.List<_verticalElement> getVerticalElement()
      {
         return verticalElement;
      }
   }

   public static class _description extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "description");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "CharacterString")
      @XmlElement(required = false)
      public CharacterString CharacterString;
   }

   public static class CharacterString extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gco";
      public static final QName QN = new QName(NS, "CharacterString");
   }

   public static class _geographicElement extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "geographicElement");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "AbstractEX_GeographicExtent")
      @XmlElement(required = false)
      public AbstractEX_GeographicExtent AbstractEX_GeographicExtent;
   }

   public static class AbstractEX_GeographicExtent extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS,
            "AbstractEX_GeographicExtent");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @XmlAttribute(required = false, name = "uuid")
      public String uuid;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "extentTypeCode")
      @XmlElement(required = false)
      public _extentTypeCode extentTypeCode;
   }

   public static class _extentTypeCode extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "extentTypeCode");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "Boolean")
      @XmlElement(required = false)
      public Boolean Boolean;
   }

   public static class Boolean extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gco";
      public static final QName QN = new QName(NS, "Boolean");
   }

   public static class _temporalElement extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "temporalElement");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "EX_TemporalExtent")
      @XmlElement(required = false)
      public EX_TemporalExtent EX_TemporalExtent;
   }

   public static class EX_TemporalExtent extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "EX_TemporalExtent");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @XmlAttribute(required = false, name = "uuid")
      public String uuid;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "extent")
      @XmlElement(required = false)
      public _extent extent;
   }

   public static class _extent extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "extent");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "AbstractTimePrimitive")
      @XmlElement(required = false)
      public AbstractTimePrimitive AbstractTimePrimitive;
   }

   public static class AbstractTimePrimitive extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "AbstractTimePrimitive");
      @XmlAttribute(required = true, name = "id")
      public String id;
      @XmlElement(required = false)
      public java.util.List<_relatedTime> relatedTime = new java.util.ArrayList<_relatedTime>();

      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "relatedTime")
      public void setRelatedTime(final _relatedTime obj)
      {
         if (obj != null)
         {
            relatedTime.add(obj);
         }
      }

      java.util.List<_relatedTime> getRelatedTime()
      {
         return relatedTime;
      }
   }

   public static class _relatedTime extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "relatedTime");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @XmlAttribute(required = false, name = "remoteSchema")
      public String remoteSchema;
      @XmlAttribute(required = false, name = "owns")
      public String owns;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "AbstractTimePrimitive")
      @XmlElement(required = false)
      public AbstractTimePrimitive AbstractTimePrimitive;
   }

   public static class _verticalElement extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "verticalElement");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "EX_VerticalExtent")
      @XmlElement(required = false)
      public EX_VerticalExtent EX_VerticalExtent;
   }

   public static class EX_VerticalExtent extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "EX_VerticalExtent");
      @XmlAttribute(required = false, name = "id")
      public String id;
      @XmlAttribute(required = false, name = "uuid")
      public String uuid;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "minimumValue")
      @XmlElement(required = false)
      public _minimumValue minimumValue;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "maximumValue")
      @XmlElement(required = false)
      public _maximumValue maximumValue;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gmd", localName = "verticalCRS")
      @XmlElement(required = false)
      public _verticalCRS verticalCRS;
   }

   public static class _minimumValue extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "minimumValue");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "Real")
      @XmlElement(required = false)
      public Real Real;
   }

   public static class Real extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gco";
      public static final QName QN = new QName(NS, "Real");
   }

   public static class _maximumValue extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "maximumValue");
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.isotc211.org/2005/gco", localName = "Real")
      @XmlElement(required = false)
      public Real Real;
   }

   public static class _verticalCRS extends Nillable
   {
      public static final String NS = "http://www.isotc211.org/2005/gmd";
      public static final QName QN = new QName(NS, "verticalCRS");
      @XmlAttribute(required = false, name = "uuidref")
      public String uuidref;
      @XmlAttribute(required = false, name = "nilReason")
      public String nilReason;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "AbstractCRS")
      @XmlElement(required = false)
      public AbstractCRS AbstractCRS;
   }

   public static class AbstractCRS extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "AbstractCRS");
      @XmlAttribute(required = true, name = "id")
      public String id;
      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "remarks")
      @XmlElement(required = false)
      public _remarks remarks;
      @XmlElement(required = false)
      public java.util.List<A_4_A_4> domainOfValidity = new java.util.ArrayList<A_4_A_4>();
      @XmlElement(required = false)
      public java.util.List<_scope> scope = new java.util.ArrayList<_scope>();

      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "domainOfValidity")
      public void setDomainOfValidity(final A_4_A_4 obj)
      {
         if (obj != null)
         {
            domainOfValidity.add(obj);
         }
      }

      java.util.List<A_4_A_4> getDomainOfValidity()
      {
         return domainOfValidity;
      }

      @JacksonXmlProperty(namespace = "http://www.opengis.net/gml/3.2", localName = "scope")
      public void setScope(final _scope obj)
      {
         if (obj != null)
         {
            scope.add(obj);
         }
      }

      java.util.List<_scope> getScope()
      {
         return scope;
      }
   }

   public static class _remarks extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "remarks");
   }

   public static class A_4_A_4 extends Nillable
   {
   }

   public static class _scope extends Nillable
   {
      public static final String NS = "http://www.opengis.net/gml/3.2";
      public static final QName QN = new QName(NS, "scope");
   }

   public static class A_5_contributingElevationGridCoverage extends Nillable
   {
      public static final String NS = "http://www.locationframework.eu/schemas/Elevation/MasterLoD1/1.0";
      public static final QName QN = new QName(NS,
            "contributingElevationGridCoverage");
      @XmlAttribute(required = false, name = "owns")
      public String owns;
      @JacksonXmlProperty(namespace = "http://www.locationframework.eu/schemas/Elevation/MasterLoD1/1.0", localName = "ElevationGridCoverage")
      @XmlElement(required = false)
      public ElevationGridCoverage ElevationGridCoverage;
   }
}
