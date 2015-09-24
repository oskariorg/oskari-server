<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:wfs="http://www.opengis.net/wfs"
     xmlns:gml="http://www.opengis.net/gml" 
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     xmlns:oskari="http://www.oskari.org"
     xsi:schemaLocation="http://www.oskari.org
     http://www.paikkatietoikkuna.fi/dataset/analysis/service/ows?service=WFS&amp;version=1.1.0&amp;request=DescribeFeatureType&amp;typeName=oskari%3Amy_places_categories
     http://www.opengis.net/wfs 
     http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd">
    <xsl:output method="text" omit-xml-declaration="yes" />
    <xsl:strip-space elements="*" />
    <!-- ignore boundedBy tag in case "Return bounding box with every feature" is enabled in geoserver -->
    <xsl:template match="gml:boundedBy"></xsl:template>
    <xsl:template match="/wfs:FeatureCollection/gml:featureMembers">
     {"parsed": {
       "layer" : "<xsl:value-of select="normalize-space(oskari:my_places_categories/oskari:category_name/.)"/>",
       "publisher" : "<xsl:value-of select="normalize-space(oskari:my_places_categories/oskari:publisher_name/.)"/>",
       places: [<xsl:for-each select="oskari:my_places_categories">
         <xsl:if test="position() != 1">,</xsl:if> {
             "name" : "<xsl:value-of select="normalize-space(gml:name/.)"/>",
             "description" : "<xsl:value-of select="normalize-space(oskari:place_desc/.)"/>",
             "link" : "<xsl:value-of select="normalize-space(oskari:link/.)"/>",
             "imageUrl" : "<xsl:value-of select="normalize-space(oskari:image_url/.)"/>"
           }</xsl:for-each>]
     }}
    </xsl:template>
</xsl:stylesheet>