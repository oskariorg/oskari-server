<?xml version="1.0" encoding="ISO-8859-1" ?>
<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd">
    <NamedLayer>
        <Name>sld_label_t1</Name>
        <UserStyle>
            <Title>Sector styling</Title>
            <FeatureTypeStyle>
                <Rule>
                    <Title>Sector styling</Title>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill">#CCCCFF</CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke">#6A5ACD</CssParameter>
                            <CssParameter name="stroke-width">1</CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                    <Label>
                       <ogc:PropertyName>t1</ogc:PropertyName>
                    </Label>
                    <Font>
                       <CssParameter name="font-family">Arial</CssParameter>
                       <CssParameter name="font-size">12</CssParameter>
                       <CssParameter name="font-style">normal</CssParameter>
                       <CssParameter name="font-weight">bold</CssParameter>
                    </Font>
                    </TextSymbolizer>
                </Rule>
            </FeatureTypeStyle>
        </UserStyle>
    </NamedLayer>
</StyledLayerDescriptor>