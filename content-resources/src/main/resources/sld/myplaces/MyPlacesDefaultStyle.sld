<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0"
                       xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd"
                       xmlns="http://www.opengis.net/sld"
                       xmlns:ogc="http://www.opengis.net/ogc"
                       xmlns:xlink="http://www.w3.org/1999/xlink"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <NamedLayer>
        <Name>MyPlaces</Name>
        <UserStyle>
            <Title>MyPlaces</Title>
            <FeatureTypeStyle>
                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>-1</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>-1</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>-1</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsNotEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsNotEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-dasharray">5 2</CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>-1</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsNotEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsNotEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-dasharray">5 2</CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <GraphicFill>
                                <Graphic>
                                    <Mark>
                                        <WellKnownName>shape://slash</WellKnownName>
                                        <Stroke>
                                            <CssParameter name="stroke"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                                            <CssParameter name="stroke-width">1</CssParameter>
                                        </Stroke>
                                    </Mark>
                                    <Size>4</Size>
                                </Graphic>
                            </GraphicFill>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <GraphicFill>
                                <Graphic>
                                    <Mark>
                                        <WellKnownName>shape://slash</WellKnownName>
                                        <Stroke>
                                            <CssParameter name="stroke"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                                            <CssParameter name="stroke-width">1</CssParameter>
                                        </Stroke>
                                    </Mark>
                                    <Size>4</Size>
                                </Graphic>
                            </GraphicFill>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsNotEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsNotEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <GraphicFill>
                                <Graphic>
                                    <Mark>
                                        <WellKnownName>shape://slash</WellKnownName>
                                        <Stroke>
                                            <CssParameter name="stroke"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                                            <CssParameter name="stroke-width">1</CssParameter>
                                        </Stroke>
                                    </Mark>
                                    <Size>4</Size>
                                </Graphic>
                            </GraphicFill>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-dasharray">5 2</CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsNotEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsNotEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <GraphicFill>
                                <Graphic>
                                    <Mark>
                                        <WellKnownName>shape://slash</WellKnownName>
                                        <Stroke>
                                            <CssParameter name="stroke"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                                            <CssParameter name="stroke-width">1</CssParameter>
                                        </Stroke>
                                    </Mark>
                                    <Size>4</Size>
                                </Graphic>
                            </GraphicFill>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-dasharray">5 2</CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <GraphicFill>
                                <Graphic>
                                    <Mark>
                                        <WellKnownName>shape://slash</WellKnownName>
                                        <Stroke>
                                            <CssParameter name="stroke"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                                            <CssParameter name="stroke-width">2</CssParameter>
                                        </Stroke>
                                    </Mark>
                                    <Size>6</Size>
                                </Graphic>
                            </GraphicFill>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <GraphicFill>
                                <Graphic>
                                    <Mark>
                                        <WellKnownName>shape://slash</WellKnownName>
                                        <Stroke>
                                            <CssParameter name="stroke"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                                            <CssParameter name="stroke-width">2</CssParameter>
                                        </Stroke>
                                    </Mark>
                                    <Size>6</Size>
                                </Graphic>
                            </GraphicFill>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsNotEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsNotEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <GraphicFill>
                                <Graphic>
                                    <Mark>
                                        <WellKnownName>shape://slash</WellKnownName>
                                        <Stroke>
                                            <CssParameter name="stroke"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                                            <CssParameter name="stroke-width">2</CssParameter>
                                        </Stroke>
                                    </Mark>
                                    <Size>6</Size>
                                </Graphic>
                            </GraphicFill>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-dasharray">5 2</CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsNotEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsNotEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <GraphicFill>
                                <Graphic>
                                    <Mark>
                                        <WellKnownName>shape://slash</WellKnownName>
                                        <Stroke>
                                            <CssParameter name="stroke"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                                            <CssParameter name="stroke-width">2</CssParameter>
                                        </Stroke>
                                    </Mark>
                                    <Size>6</Size>
                                </Graphic>
                            </GraphicFill>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-dasharray">5 2</CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>2</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <GraphicFill>
                                <Graphic>
                                    <Mark>
                                        <WellKnownName>shape://horline</WellKnownName>
                                        <Stroke>
                                            <CssParameter name="stroke"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                                            <CssParameter name="stroke-width">0.4</CssParameter>
                                        </Stroke>
                                    </Mark>
                                    <Size>4.5</Size>
                                </Graphic>
                            </GraphicFill>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>2</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <GraphicFill>
                                <Graphic>
                                    <Mark>
                                        <WellKnownName>shape://horline</WellKnownName>
                                        <Stroke>
                                            <CssParameter name="stroke"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                                            <CssParameter name="stroke-width">0.4</CssParameter>
                                        </Stroke>
                                    </Mark>
                                    <Size>4.5</Size>
                                </Graphic>
                            </GraphicFill>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>2</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsNotEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsNotEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <GraphicFill>
                                <Graphic>
                                    <Mark>
                                        <WellKnownName>shape://horline</WellKnownName>
                                        <Stroke>
                                            <CssParameter name="stroke"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                                            <CssParameter name="stroke-width">0.4</CssParameter>
                                        </Stroke>
                                    </Mark>
                                    <Size>4.5</Size>
                                </Graphic>
                            </GraphicFill>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-dasharray">5 2</CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>2</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsNotEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsNotEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <GraphicFill>
                                <Graphic>
                                    <Mark>
                                        <WellKnownName>shape://horline</WellKnownName>
                                        <Stroke>
                                            <CssParameter name="stroke"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                                            <CssParameter name="stroke-width">0.4</CssParameter>
                                        </Stroke>
                                    </Mark>
                                    <Size>4.5</Size>
                                </Graphic>
                            </GraphicFill>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-dasharray">5 2</CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>3</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <GraphicFill>
                                <Graphic>
                                    <Mark>
                                        <WellKnownName>shape://horline</WellKnownName>
                                        <Stroke>
                                            <CssParameter name="stroke"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                                            <CssParameter name="stroke-width">2</CssParameter>
                                        </Stroke>
                                    </Mark>
                                    <Size>5.5</Size>
                                </Graphic>
                            </GraphicFill>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>3</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <GraphicFill>
                                <Graphic>
                                    <Mark>
                                        <WellKnownName>shape://horline</WellKnownName>
                                        <Stroke>
                                            <CssParameter name="stroke"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                                            <CssParameter name="stroke-width">2</CssParameter>
                                        </Stroke>
                                    </Mark>
                                    <Size>5.5</Size>
                                </Graphic>
                            </GraphicFill>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>3</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsNotEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsNotEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <GraphicFill>
                                <Graphic>
                                    <Mark>
                                        <WellKnownName>shape://horline</WellKnownName>
                                        <Stroke>
                                            <CssParameter name="stroke"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                                            <CssParameter name="stroke-width">2</CssParameter>
                                        </Stroke>
                                    </Mark>
                                    <Size>5.5</Size>
                                </Graphic>
                            </GraphicFill>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-dasharray">5 2</CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>Polygon</ogc:Literal>
                                    <ogc:Literal>MultiPolygon</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>fill_pattern</ogc:PropertyName>
                                <ogc:Literal>3</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsNotEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>border_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsNotEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <GraphicFill>
                                <Graphic>
                                    <Mark>
                                        <WellKnownName>shape://horline</WellKnownName>
                                        <Stroke>
                                            <CssParameter name="stroke"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                                            <CssParameter name="stroke-width">2</CssParameter>
                                        </Stroke>
                                    </Mark>
                                    <Size>5.5</Size>
                                </Graphic>
                            </GraphicFill>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>border_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>border_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-dasharray">5 2</CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>border_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.5</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>LineString</ogc:Literal>
                                    <ogc:Literal>MultiLineString</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>stroke_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <LineSymbolizer>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>stroke_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>stroke_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>stroke_linejoin</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-linecap"><ogc:PropertyName>stroke_linecap</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </LineSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>10</DisplacementX>
                                    <DisplacementY>0</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>LineString</ogc:Literal>
                                    <ogc:Literal>MultiLineString</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>stroke_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                        </ogc:And>
                    </ogc:Filter>
                    <LineSymbolizer>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>stroke_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>stroke_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>stroke_linejoin</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-linecap"><ogc:PropertyName>stroke_linecap</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </LineSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>10</DisplacementX>
                                    <DisplacementY>0</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>LineString</ogc:Literal>
                                    <ogc:Literal>MultiLineString</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsNotEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>stroke_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsNotEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <LineSymbolizer>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>stroke_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>stroke_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-dasharray">5 2</CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>stroke_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </LineSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>10</DisplacementX>
                                    <DisplacementY>0</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:Function name="geometryType">
                                        <ogc:PropertyName>geometry</ogc:PropertyName>
                                    </ogc:Function>
                                    <ogc:Literal>LineString</ogc:Literal>
                                    <ogc:Literal>MultiLineString</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsNotEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>stroke_dasharray</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsNotEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                        </ogc:And>
                    </ogc:Filter>
                    <LineSymbolizer>
                        <Stroke>
                            <CssParameter name="stroke"><ogc:PropertyName>stroke_color</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-width"><ogc:PropertyName>stroke_width</ogc:PropertyName></CssParameter>
                            <CssParameter name="stroke-dasharray">5 2</CssParameter>
                            <CssParameter name="stroke-linejoin"><ogc:PropertyName>stroke_linejoin</ogc:PropertyName></CssParameter>
                        </Stroke>
                    </LineSymbolizer>
                    <TextSymbolizer>
                        <Geometry>
                            <ogc:Function name="centroid">
                                <ogc:PropertyName>geometry</ogc:PropertyName>
                            </ogc:Function>
                        </Geometry>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>10</DisplacementX>
                                    <DisplacementY>0</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>
            </FeatureTypeStyle>


            <FeatureTypeStyle>
                <Transformation>
                    <ogc:Function name="gs:OskariPointStacker">
                        <ogc:Function name="parameter">
                            <ogc:Literal>data</ogc:Literal>
                        </ogc:Function>
                        <ogc:Function name="parameter">
                            <ogc:Literal>cellSize</ogc:Literal>
                            <ogc:Literal>30</ogc:Literal>
                        </ogc:Function>
                        <ogc:Function name="parameter">
                            <ogc:Literal>outputBBOX</ogc:Literal>
                            <ogc:Function name="env">
                                <ogc:Literal>wms_bbox</ogc:Literal>
                            </ogc:Function>
                        </ogc:Function>
                        <ogc:Function name="parameter">
                            <ogc:Literal>outputWidth</ogc:Literal>
                            <ogc:Function name="env">
                                <ogc:Literal>wms_width</ogc:Literal>
                            </ogc:Function>
                        </ogc:Function>
                        <ogc:Function name="parameter">
                            <ogc:Literal>outputHeight</ogc:Literal>
                            <ogc:Function name="env">
                                <ogc:Literal>wms_height</ogc:Literal>
                            </ogc:Function>
                        </ogc:Function>
                    </ogc:Function>
                </Transformation>
                <Rule>
                    <Name>PointScale1Name</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MaxScaleDenominator>1000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>42</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale1Attention</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MaxScaleDenominator>1000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>42</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale2Name</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>1000</MinScaleDenominator>
                    <MaxScaleDenominator>3000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>40</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale2Attention</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>1000</MinScaleDenominator>
                    <MaxScaleDenominator>3000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>40</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale3Name</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>3000</MinScaleDenominator>
                    <MaxScaleDenominator>6000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>38</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale3Attention</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>3000</MinScaleDenominator>
                    <MaxScaleDenominator>6000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>38</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale4Name</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>6000</MinScaleDenominator>
                    <MaxScaleDenominator>12000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>36</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale4Attention</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>6000</MinScaleDenominator>
                    <MaxScaleDenominator>12000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>36</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale5Name</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>12000</MinScaleDenominator>
                    <MaxScaleDenominator>25000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>34</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale5Attention</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>12000</MinScaleDenominator>
                    <MaxScaleDenominator>25000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>34</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale6Name</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>25000</MinScaleDenominator>
                    <MaxScaleDenominator>50000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>32</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale6Attention</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>25000</MinScaleDenominator>
                    <MaxScaleDenominator>50000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>32</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale7Name</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>50000</MinScaleDenominator>
                    <MaxScaleDenominator>100000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>30</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale7Attention</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>50000</MinScaleDenominator>
                    <MaxScaleDenominator>100000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>30</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale8Name</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>100000</MinScaleDenominator>
                    <MaxScaleDenominator>300000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>28</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale8Attention</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>100000</MinScaleDenominator>
                    <MaxScaleDenominator>300000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>28</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale9Name</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>300000</MinScaleDenominator>
                    <MaxScaleDenominator>600000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>26</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale9Attention</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>300000</MinScaleDenominator>
                    <MaxScaleDenominator>600000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>26</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale10Name</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>600000</MinScaleDenominator>
                    <MaxScaleDenominator>1200000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>24</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale10Attention</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>600000</MinScaleDenominator>
                    <MaxScaleDenominator>1200000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>24</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale11Name</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>1200000</MinScaleDenominator>
                    <MaxScaleDenominator>2500000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>22</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale11Attention</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>1200000</MinScaleDenominator>
                    <MaxScaleDenominator>2500000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>22</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale12Name</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>2500000</MinScaleDenominator>
                    <MaxScaleDenominator>5000000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>20</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale12Attention</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>2500000</MinScaleDenominator>
                    <MaxScaleDenominator>5000000</MaxScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>20</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale13Name</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>5000000</MinScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>18</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>PointScale13Attention</Name>
                    <ogc:Filter>
                        <ogc:And>
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="in2">
                                    <ogc:PropertyName>geometryType</ogc:PropertyName>
                                    <ogc:Literal>Point</ogc:Literal>
                                    <ogc:Literal>MultiPoint</ogc:Literal>
                                </ogc:Function>
                                <ogc:Literal>true</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                            <ogc:PropertyIsGreaterThan>
                                <ogc:Function name="strLength">
                                    <ogc:PropertyName>attention_text</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>0</ogc:Literal>
                            </ogc:PropertyIsGreaterThan>
                            <ogc:PropertyIsLessThanOrEqualTo>
                                <ogc:PropertyName>count</ogc:PropertyName>
                                <ogc:Literal>1</ogc:Literal>
                            </ogc:PropertyIsLessThanOrEqualTo>
                        </ogc:And>
                    </ogc:Filter>
                    <MinScaleDenominator>5000000</MinScaleDenominator>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>oskari://dot-markers#0xe00<ogc:PropertyName>dot_shape</ogc:PropertyName></WellKnownName>
                                <Fill>
                                    <CssParameter name="fill"><ogc:PropertyName>dot_color</ogc:PropertyName></CssParameter>
                                </Fill>
                                <Stroke>
                                    <CssParameter name="stroke">#B4B4B4</CssParameter>
                                </Stroke>
                            </Mark>
                            <Size>
                                <ogc:Mul>
                                    <ogc:PropertyName>dot_size</ogc:PropertyName>
                                    <ogc:Literal>18</ogc:Literal>
                                </ogc:Mul>
                            </Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Label><ogc:PropertyName>attention_text</ogc:PropertyName></Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">14</CssParameter>
                            <CssParameter name="font-style">normal</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0.5</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>
                                        <ogc:Add>
                                            <ogc:Mul>
                                                <ogc:PropertyName>dot_size</ogc:PropertyName>
                                                <ogc:Literal>2</ogc:Literal>
                                            </ogc:Mul>
                                            <ogc:Literal>10</ogc:Literal>
                                        </ogc:Add>
                                    </DisplacementX>
                                    <DisplacementY>1</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Fill>
                            <CssParameter name="fill">F000000</CssParameter>
                        </Fill>
                        <Halo>
                            <Radius>1</Radius>
                            <Fill>
                                <CssParameter name="fill">#FFFFFF</CssParameter>
                            </Fill>
                        </Halo>
                        <VendorOption name="autoWrap">60</VendorOption>
                        <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer>
                </Rule>

                <Rule>
                    <Name>rule29</Name>
                    <Title>2-9 Places</Title>
                    <ogc:Filter>
                        <ogc:PropertyIsBetween>
                            <ogc:PropertyName>count</ogc:PropertyName>
                            <ogc:LowerBoundary>
                                <ogc:Literal>2</ogc:Literal>
                            </ogc:LowerBoundary>
                            <ogc:UpperBoundary>
                                <ogc:Literal>9</ogc:Literal>
                            </ogc:UpperBoundary>
                        </ogc:PropertyIsBetween>
                    </ogc:Filter>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>circle</WellKnownName>
                                <Fill>
                                    <CssParameter name="fill">#3182bd</CssParameter>
                                </Fill>
                            </Mark>
                            <Size>20</Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Priority>10000</Priority>
                        <Label>
                            <ogc:PropertyName>count</ogc:PropertyName>
                        </Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">12</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0.0</AnchorPointX>
                                    <AnchorPointY>0.0</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>-4</DisplacementX>
                                    <DisplacementY>-6.5</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Halo>
                            <Radius>2</Radius>
                            <Fill>
                                <CssParameter name="fill">#3182bd</CssParameter>
                                <CssParameter name="fill-opacity">0.9</CssParameter>
                            </Fill>
                        </Halo>
                        <Fill>
                            <CssParameter name="fill">#FFFFFF</CssParameter>
                            <CssParameter name="fill-opacity">1.0</CssParameter>
                        </Fill>
                    </TextSymbolizer>
                </Rule>
                <Rule>
                    <Name>rule10</Name>
                    <Title>10 Places</Title>
                    <ogc:Filter>
                        <ogc:PropertyIsGreaterThan>
                            <ogc:PropertyName>count</ogc:PropertyName>
                            <ogc:Literal>9</ogc:Literal>
                        </ogc:PropertyIsGreaterThan>
                    </ogc:Filter>
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>circle</WellKnownName>
                                <Fill>
                                    <CssParameter name="fill">#08519c</CssParameter>
                                </Fill>
                            </Mark>
                            <Size>25</Size>
                        </Graphic>
                    </PointSymbolizer>
                    <TextSymbolizer>
                        <Priority>10000</Priority>
                        <Label>
                            <ogc:PropertyName>count</ogc:PropertyName>
                        </Label>
                        <Font>
                            <CssParameter name="font-family">SansSerif.plain</CssParameter>
                            <CssParameter name="font-size">12</CssParameter>
                            <CssParameter name="font-weight">bold</CssParameter>
                        </Font>
                        <LabelPlacement>
                            <PointPlacement>
                                <AnchorPoint>
                                    <AnchorPointX>0</AnchorPointX>
                                    <AnchorPointY>0</AnchorPointY>
                                </AnchorPoint>
                                <Displacement>
                                    <DisplacementX>-9</DisplacementX>
                                    <DisplacementY>-7</DisplacementY>
                                </Displacement>
                            </PointPlacement>
                        </LabelPlacement>
                        <Halo>
                            <Radius>2</Radius>
                            <Fill>
                                <CssParameter name="fill">#08519c</CssParameter>
                                <CssParameter name="fill-opacity">0.9</CssParameter>
                            </Fill>
                        </Halo>
                        <Fill>
                            <CssParameter name="fill">#FFFFFF</CssParameter>
                            <CssParameter name="fill-opacity">1.0</CssParameter>
                        </Fill>
                    </TextSymbolizer>
                </Rule>
            </FeatureTypeStyle>
        </UserStyle>
    </NamedLayer>
</StyledLayerDescriptor>
