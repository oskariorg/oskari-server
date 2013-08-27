<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:wfs="http://www.opengis.net/wfs" 
     xmlns:tampere_ora="http://www.navici.com/ns/tampere_ora" 
     xmlns:gml="http://www.opengis.net/gml" 
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     xmlns:ows="http://www.paikkatietoikkuna.fi" 
     xsi:schemaLocation="http://www.paikkatietoikkuna.fi 
     http://nipsuke01.nls.fi:8080/geoserver/ows/wfs?service=WFS&amp;version=1.1.0&amp;request=DescribeFeatureType&amp;typeName=ows%3Amy_places_categories 
     http://www.opengis.net/wfs 
     http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd">
 <xsl:output method="text" omit-xml-declaration="yes" />
 <xsl:strip-space elements="*" />
   <xsl:template match="/wfs:FeatureCollection/gml:featureMembers">
     {"parsed": {
       "layer" : "<xsl:value-of select="normalize-space(ows:my_places_categories/ows:category_name/.)"/>",
       "publisher" : "<xsl:value-of select="normalize-space(ows:my_places_categories/ows:publisher_name/.)"/>",
       places: [<xsl:for-each select="ows:my_places_categories">
         <xsl:if test="position() != 1">,</xsl:if> {
             "name" : "<xsl:value-of select="normalize-space(gml:name/.)"/>",
             "description" : "<xsl:value-of select="normalize-space(ows:place_desc/.)"/>",
             "link" : "<xsl:value-of select="normalize-space(ows:link/.)"/>"
           }</xsl:for-each>]
     }}
   </xsl:template>
</xsl:stylesheet>