<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:wfs="http://www.opengis.net/wfs"
                xmlns:luonto="https://something.fi/ns/luonto"
                xmlns:gml="http://www.opengis.net/gml">
    <xsl:output method="text" omit-xml-declaration="yes" ></xsl:output>
    <xsl:strip-space elements="*" ></xsl:strip-space>
    <xsl:template match="/"><xsl:apply-templates select="/wfs:FeatureCollection/gml:featureMembers/luonto:generatedLayerName" ></xsl:apply-templates>
    </xsl:template>
    <xsl:template match="wfs:FeatureCollection/gml:featureMembers/luonto:generatedLayerName">

        <xsl:text>{parsed: {</xsl:text>
            "nameForTesting": <xsl:value-of select="luonto:ID"/>
        <xsl:text>}}</xsl:text>
    </xsl:template>
</xsl:stylesheet>