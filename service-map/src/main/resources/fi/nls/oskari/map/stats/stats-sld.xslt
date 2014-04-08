<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:ogc="http://www.opengis.net/ogc">
    <xsl:output method="xml" omit-xml-declaration="no" />
    <xsl:strip-space elements="*" />
    
    <xsl:template match="/Visualization">
        <StyledLayerDescriptor version="1.0.0"
            xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd"
            xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
            xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
            <NamedLayer>
                <Name><xsl:value-of select="@layer" /></Name>
                <UserStyle>
                    <Title><xsl:value-of select="@name" /></Title>
                    <FeatureTypeStyle>
                        <xsl:apply-templates select="Range" />
                    </FeatureTypeStyle>
                </UserStyle>
            </NamedLayer>
        </StyledLayerDescriptor>
    </xsl:template>

    <xsl:template match="Range">
        <Rule>
            <LineSymbolizer>
                <Stroke>
                    <CssParameter name="stroke"><xsl:value-of select="@line-color" /></CssParameter>
                    <CssParameter name="stroke-width"><xsl:value-of select="@line-width" /></CssParameter>
                </Stroke>
            </LineSymbolizer>
        </Rule>
        <Rule>
            <Name><xsl:value-of select="@name" /></Name>
            <Title><xsl:value-of select="@title" /></Title>
            <Abstract><xsl:value-of select="@abstract" /></Abstract>
            <ogc:Filter>
            <ogc:Or>
                <xsl:for-each select="Property">
                    <ogc:PropertyIsEqualTo>
                        <ogc:PropertyName><xsl:value-of select="../@property" /></ogc:PropertyName>
                        <ogc:Literal><xsl:value-of select="@value" /></ogc:Literal>
                    </ogc:PropertyIsEqualTo>
                </xsl:for-each>
            </ogc:Or>
            </ogc:Filter>
            <PolygonSymbolizer>
                <Fill>
                    <CssParameter name="fill">#<xsl:value-of select="@color" /></CssParameter>
                </Fill>
            </PolygonSymbolizer>
        </Rule>
    </xsl:template>
</xsl:stylesheet>