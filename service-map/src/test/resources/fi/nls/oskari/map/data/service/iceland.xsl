<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:wfs="http://www.opengis.net/wfs"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:LMI_vektor="http://www.lmi.is/lmi_vektor">
    <xsl:output method="text" omit-xml-declaration="yes" />
    <xsl:strip-space elements="*"></xsl:strip-space>
    <xsl:template match="/wfs:FeatureCollection/gml:boundedBy">
    </xsl:template>
    <xsl:template match="/wfs:FeatureCollection/gml:boundedBy/gml:null">
        { "parsed": {}}
    </xsl:template>
    <xsl:template match="/wfs:FeatureCollection/gml:featureMember/LMI_vektor:donsku_teikningar">
        <xsl:text>{ "parsed": {</xsl:text>
        "Sta\u00f0ur" : "<xsl:value-of select="LMI_vektor:stadur"></xsl:value-of>",
        "Mynd" : <xsl:text><![CDATA["<a target='_blank' href=']]></xsl:text><xsl:value-of select="LMI_vektor:link"></xsl:value-of><xsl:text><![CDATA['>link</a>"]]></xsl:text>,
        "Athugasemd" : "<xsl:value-of select="LMI_vektor:athugasemd"></xsl:value-of>"
        <xsl:text>}}</xsl:text>
    </xsl:template>
</xsl:stylesheet>
