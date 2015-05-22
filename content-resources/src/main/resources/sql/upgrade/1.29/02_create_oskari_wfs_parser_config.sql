
-- WFS-layer seed parser configs for wfs 2.0.0 parsers
-- Stored WFS-layer parser configs are in portti_wfs_template_model table
-- These seed configs are spesific for ELF and INSPIRE WFS services
-- DO only FIRST INSERT for oskari application
-- All inserts are for ELF

DROP TABLE IF EXISTS oskari_wfs_parser_config;

CREATE TABLE oskari_wfs_parser_config
(
  id serial NOT NULL,
  name character varying(128),
  type character varying(64),
  request_template text,
  response_template text,
  parse_config text,
  sld_style text,
  CONSTRAINT oskari_wfs_parser_config_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);


INSERT INTO oskari_wfs_parser_config ( name, type, request_template, response_template, parse_config) VALUES ( 'default', 'Default Path', '/fi/nls/oskari/fe/input/request/wfs/generic/ELF_generic_wfs_template.xml', 'fi.nls.oskari.eu.elf.recipe.universal.ELF_wfs_DefaultParser',
 '{
        "scan": {
          "scanNS": "http://www.opengis.net/wfs/2.0",
          "name": "member"
        },
        "root": {
          "rootNS": "[via application]",
          "name": "[via application]"
        },
        "paths": [
          {
            "path": "[via application]",
            "type": "String",
            "label": "id"
          }
        ]
      }');

INSERT INTO oskari_wfs_parser_config ( name, type, request_template, response_template, sld_style) VALUES ( 'elf-lod1gn:NamedPlace', 'Groovy', '/fi/nls/oskari/fe/input/format/gml/inspire/gn/fgi_cascade_wfs_template.xml', '/fi/nls/oskari/fe/input/format/gml/gn/ELF_generic_GN.groovy', '/fi/nls/oskari/fe/output/style/inspire/gn/fgi_cascade.xml');
INSERT INTO oskari_wfs_parser_config ( name, type, request_template, response_template, sld_style) VALUES ( 'elf-lod1gn:NamedPlace', 'Pull', '/fi/nls/oskari/fe/input/format/gml/inspire/gn/fgi_cascade_wfs_template.xml', 'fi.nls.oskari.eu.elf.recipe.geographicalnames.ELF_MasterLoD1_NamedPlace_Parser', '/fi/nls/oskari/fe/output/style/inspire/gn/fgi_cascade.xml');
  
INSERT INTO oskari_wfs_parser_config ( name, type, request_template, response_template, sld_style) VALUES ( 'au:AdministrativeUnit', 'Groovy', 'oskari-feature-engine:QueryArgsBuilder_WFS_GET', '/fi/nls/oskari/fe/input/format/gml/au/INSPIRE_generic_AU.groovy', '/fi/nls/oskari/fe/output/style/INSPIRE_SLD/AU.AdministrativeUnit.Default.xml');
INSERT INTO oskari_wfs_parser_config ( name, type, request_template, response_template, sld_style) VALUES ( 'elf-lod1au:AdministrativeBoundary', 'Pull', '/fi/nls/oskari/fe/input/format/gml/inspire/au/fgi_fi_elf_wfs_template.xml', 'fi.nls.oskari.eu.elf.recipe.administrativeunits.ELF_MasterLoD0_AdministrativeUnit_nls_fi_wfs_Parser', '/fi/nls/oskari/fe/output/style/INSPIRE_SLD/AU.AdministrativeBoundary.Default.xml');
INSERT INTO oskari_wfs_parser_config ( name, type, request_template, response_template, sld_style) VALUES ( 'tn-ro:RoadLink', 'Pull', '/fi/nls/oskari/fe/input/format/gml/inspire/tn/fgi_fi_wfs_template.xml', 'fi.nls.oskari.eu.elf.recipe.roadtransportnetwork.ELF_MasterLoD1_RoadLink_Parser', '/fi/nls/oskari/fe/output/style/INSPIRE_SLD/TN.RoadTransportNetwork.RoadLink.Default.sld');
INSERT INTO oskari_wfs_parser_config ( name, type, request_template, response_template, sld_style) VALUES ( 'tns:AdministrativeUnit', 'Groovy', '/fi/nls/oskari/fe/input/format/gml/inspire/au/lantmateriet_se_wfs_template.xml', '/fi/nls/oskari/fe/input/format/gml/au/ELF_generic_AU.groovy', '/fi/nls/oskari/fe/output/style/INSPIRE_SLD/AU.AdministrativeUnit.Default.xml');
INSERT INTO oskari_wfs_parser_config ( name, type, request_template, response_template, sld_style) VALUES ( 'elf-lod1hn:WatercourseLink', 'Groovy', '/fi/nls/oskari/fe/input/format/gml/inspire/hy/fgi_fi_WatercourseLink_wfs_template.xml', '/fi/nls/oskari/fe/input/format/gml/hy/ELF_generic_HY.groovy', '/fi/nls/oskari/fe/output/style/inspire/hy/fgi_fi_WatercourseLink.xml');
INSERT INTO oskari_wfs_parser_config ( name, type, request_template, response_template, sld_style) VALUES ( 'elf-lod0bu:Building', 'Pull', '/fi/nls/oskari/fe/input/format/gml/inspire/bu/nls_fi_wfs_template.xml', 'fi.nls.oskari.eu.elf.recipe.buildings.ELF_MasterLoD0_Building_nls_fi_wfs_Parser', '/fi/nls/oskari/fe/output/style/inspire/bu/nls_fi.xml');
INSERT INTO oskari_wfs_parser_config ( name, type, request_template, response_template, parse_config) VALUES ( 'hy-p:StandingWater', 'Path', '/fi/nls/oskari/fe/input/request/wfs/generic/ELF_generic_wfs_template.xml', 'fi.nls.oskari.eu.elf.recipe.universal.ELF_wfs_Parser', '{"paths":[{"path":"/hy-p:StandingWater/@gml:id","label":"id","type":"String"}],"root":{"rootNS":"urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0","name":"StandingWater"},"scan":{"scanNS":"http://www.opengis.net/wfs/2.0","name":"member"}}');
INSERT INTO oskari_wfs_parser_config ( name, type, request_template, response_template, parse_config) VALUES ('hy-p:LandWaterBoundary', 'Path', '/fi/nls/oskari/fe/input/request/wfs/generic/ELF_generic_wfs_template.xml', 'fi.nls.oskari.eu.elf.recipe.universal.ELF_wfs_Parser', '{"paths":[{"path":"/hy-p:LandWaterBoundary/@gml:id","label":"id","type":"String"},{"path":"/hy-p:LandWaterBoundary/hy-p:inspireId/base:Identifier/base:localId","label":"InspireLocalId","type":"String"},{"path":"/hy-p:LandWaterBoundary/hy-p:beginLifespanVersion","label":"Lifespan","type":"String"},{"path":"/hy-p:LandWaterBoundary/hy-p:geometry","label":"geom","type":"Geometry"}],"root":{"rootNS":"urn:x-inspire:specification:gmlas:HydroPhysicalWaters:3.0","name":"LandWaterBoundary"},"scan":{"scanNS":"http://www.opengis.net/wfs/2.0","name":"member"}}');
INSERT INTO oskari_wfs_parser_config ( name, type, request_template, response_template, parse_config) VALUES ('elf-gn:NamedPlace', 'Path', '/fi/nls/oskari/fe/input/request/wfs/generic/ELF_generic_wfs_template.xml', 'fi.nls.oskari.eu.elf.recipe.universal.ELF_wfs_Parser',
 '{
    "scan": {
        "scanNS": "http://www.opengis.net/wfs/2.0",
        "name": "member"
    },
    "root": {
        "rootNS": "http://www.locationframework.eu/schemas/GeographicalNames/0.2",
        "name": "NamedPlace"
    },
    "paths": [ {
        "path": "/elf-gn:NamedPlace/@gml:id",
        "type": "String",
        "label": "id"
    }, {
        "path": "/elf-gn:NamedPlace/gn:inspireId/base:Identifier/base:localId",
        "type": "String",
        "label": "InspireLocalId"
    }, {
        "path": "/elf-gn:NamedPlace/gn:inspireId/base:Identifier/base:versionId",
        "type": "String",
        "label": "InspireVersionId"
    }, {
        "path": "/elf-gn:NamedPlace/gn:geometry",
        "type": "Geometry",
        "label": "geom"
    }, {
        "path": "/elf-gn:NamedPlace/gn:localType/gmd:LocalisedCharacterString",
        "type": "String",
        "label": "type"
    }, {
        "path": "/elf-gn:NamedPlace/gn:name/elf-gn:GeographicalName/gn:spelling/gn:SpellingOfName/gn:text",
        "type": "String",
        "label": "name"
    }]
}');
INSERT INTO portti_wfs_template_model ( name, type, request_template, response_template, parse_config) VALUES ('elf-lod0ad:Address', 'Path', '/fi/nls/oskari/fe/input/request/wfs/generic/ELF_generic_wfs_template.xml', 'fi.nls.oskari.eu.elf.recipe.universal.ELF_wfs_Parser',
'{
    "scan": {
        "scanNS": "http://www.opengis.net/wfs/2.0",
        "name": "member"
    },
    "root": {
        "rootNS": "http://www.locationframework.eu/schemas/Addresses/MasterLoD0/1.0",
        "name": "Address"
    },
    "paths": [{
        "rootNS": "http://www.locationframework.eu/schemas/Addresses/MasterLoD0/1.0",
        "rootName": "Address",
        "scanNS": "http://www.opengis.net/wfs/2.0",
        "scanName": "member"
    }, {
        "path": "/elf-lod0ad:Address/@gml:id",
        "type": "String",
        "label": "id"
    }, {
        "path": "/elf-lod0ad:Address/ad:inspireId/base:Identifier/base:localId",
        "type": "String",
        "label": "InspireLocalId"
    }, {
        "path": "/elf-lod0ad:Address/ad:inspireId/base:Identifier/base:versionId",
        "type": "String",
        "label": "InspireVersionId"
    }, {
        "path": "/elf-lod0ad:Address/ad:position/ad:GeographicPosition/ad:geometry",
        "type": "Geometry",
        "label": "geom"
    }, {
        "path": "/elf-lod0ad:Address/ad:locator/ad:AddressLocator/ad:designator/ad:LocatorDesignator",
        "type": "Object",
        "label": "addressLocatorDesignators"
    }, {
        "path": "/elf-lod0ad:Address/ad:validFrom",
        "type": "String",
        "label": "validFrom"
    }, {
        "path": "/elf-lod0ad:Address/ad:validTo",
        "type": "String",
        "label": "validTo"
    }, {
        "path": "/elf-lod0ad:Address/ad:beginLifespanVersion",
        "type": "String",
        "label": "beginLifespanVersion"
    }, {
        "path": "/elf-lod0ad:Address/ad:endLifespanVersion",
        "type": "String",
        "label": "endLifespanVersion"
    }, {
        "path": "/elf-lod0ad:Address/ad:component/@xlink:href",
        "type": "Href",
        "label": "components",
        "hrefPath": [{
            "path": "/wfs:SimpleFeatureCollection/wfs:member/elf-lod0ad:AdminUnitName/@gml:id",
            "type": "String",
            "label": "id"
        }, {
            "path": "/wfs:SimpleFeatureCollection/wfs:member/elf-lod0ad:AdminUnitName/ad:inspireId/base:Identifier/base:namespace",
            "type": "String",
            "label": "type"
        }, {
            "path": "/wfs:SimpleFeatureCollection/wfs:member/elf-lod0ad:AdminUnitName/ad:name/gn:GeographicalName/gn:language",
            "type": "String",
            "label": "language"
        }, {
            "path": "/wfs:SimpleFeatureCollection/wfs:member/elf-lod0ad:AdminUnitName/ad:name/gn:GeographicalName/gn:spelling/gn:SpellingOfName/gn:text",
            "type": "String",
            "label": "name"
        }, {
            "path": "/wfs:SimpleFeatureCollection/wfs:member/elf-lod0ad:ThoroughfareName/@gml:id",
            "type": "String",
            "label": "id"
        }, {
            "path": "/wfs:SimpleFeatureCollection/wfs:member/elf-lod0ad:ThoroughfareName/ad:inspireId/base:Identifier/base:namespace",
            "type": "String",
            "label": "type"
        }, {
            "path": "/wfs:SimpleFeatureCollection/wfs:member/elf-lod0ad:ThoroughfareName/ad:name/gn:GeographicalName/gn:language",
            "type": "String",
            "label": "language"
        }, {
            "path": "/wfs:SimpleFeatureCollection/wfs:member/elf-lod0ad:ThoroughfareName/ad:name/gn:GeographicalName/gn:spelling/gn:SpellingOfName/gn:text",
            "type": "String",
            "label": "name"
        }]
    }]
}');
