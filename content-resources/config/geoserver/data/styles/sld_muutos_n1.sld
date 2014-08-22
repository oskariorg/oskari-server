<?xml version="1.0" encoding="ISO-8859-1" ?>
<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd">
    <NamedLayer>
        <Name>sld_muutos_n1</Name>
        <UserStyle>
            <Title>Analysis delta</Title>
            <FeatureTypeStyle>
             <Rule>
                    <Title>Polygons  n1 equalto 0</Title>
                    <ogc:Filter>

                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>n1</ogc:PropertyName>
                                <ogc:Literal>0.0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
  
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill">#FFFFFF</CssParameter>
                            <CssParameter name="fill-opacity">0.5</CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke">#326CA6</CssParameter>
                            <CssParameter name="stroke-width">1</CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                </Rule>
                <Rule>
                    <Title>Polygons n1 lt 0 </Title>
                    <ogc:Filter>
 
                            <ogc:PropertyIsLessThan>
                                <ogc:PropertyName>n1</ogc:PropertyName>
                                <ogc:Literal>0.0</ogc:Literal>
                            </ogc:PropertyIsLessThan>
      
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill">#CA0020</CssParameter>
                            <CssParameter name="fill-opacity">0.2</CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke">#326CA6</CssParameter>
                            <CssParameter name="stroke-width">1</CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                </Rule>
                <Rule>
                    <Title>Polygons gt 0</Title>
                    <ogc:Filter>
            
                            <ogc:PropertyIsGreaterThan>
                                <ogc:PropertyName>n1</ogc:PropertyName>
                                <ogc:Literal>0.0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
          
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill">#0571B0</CssParameter>
                            <CssParameter name="fill-opacity">0.2</CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke">#326CA6</CssParameter>
                            <CssParameter name="stroke-width">1</CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                </Rule>
              
            </FeatureTypeStyle>
        </UserStyle>
    </NamedLayer>
</StyledLayerDescriptor>