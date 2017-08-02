<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0"
                       xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd"
                       xmlns="http://www.opengis.net/sld"
                       xmlns:ogc="http://www.opengis.net/ogc"
                       xmlns:xlink="http://www.w3.org/1999/xlink"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <NamedLayer>
        <Name>user_data_style</Name>
        <UserStyle>
            <Title>user_data_style</Title>
            <FeatureTypeStyle>
                <Rule>
                    <Name>PointScale1</Name>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:Function name="in2">
                                <ogc:Function name="geometryType">
                                    <ogc:PropertyName>geometry</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>Point</ogc:Literal>
                                <ogc:Literal>MultiPoint</ogc:Literal>
                            </ogc:Function>
                            <ogc:Literal>true</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
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
                </Rule>

                <Rule>
                    <Name>PointScale2</Name>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:Function name="in2">
                                <ogc:Function name="geometryType">
                                    <ogc:PropertyName>geometry</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>Point</ogc:Literal>
                                <ogc:Literal>MultiPoint</ogc:Literal>
                            </ogc:Function>
                            <ogc:Literal>true</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
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
                </Rule>

                <Rule>
                    <Name>PointScale3</Name>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:Function name="in2">
                                <ogc:Function name="geometryType">
                                    <ogc:PropertyName>geometry</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>Point</ogc:Literal>
                                <ogc:Literal>MultiPoint</ogc:Literal>
                            </ogc:Function>
                            <ogc:Literal>true</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
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
                </Rule>

                <Rule>
                    <Name>PointScale4</Name>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:Function name="in2">
                                <ogc:Function name="geometryType">
                                    <ogc:PropertyName>geometry</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>Point</ogc:Literal>
                                <ogc:Literal>MultiPoint</ogc:Literal>
                            </ogc:Function>
                            <ogc:Literal>true</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
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
                </Rule>

                <Rule>
                    <Name>PointScale5</Name>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:Function name="in2">
                                <ogc:Function name="geometryType">
                                    <ogc:PropertyName>geometry</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>Point</ogc:Literal>
                                <ogc:Literal>MultiPoint</ogc:Literal>
                            </ogc:Function>
                            <ogc:Literal>true</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
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
                </Rule>

                <Rule>
                    <Name>PointScale6</Name>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:Function name="in2">
                                <ogc:Function name="geometryType">
                                    <ogc:PropertyName>geometry</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>Point</ogc:Literal>
                                <ogc:Literal>MultiPoint</ogc:Literal>
                            </ogc:Function>
                            <ogc:Literal>true</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
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
                </Rule>

                <Rule>
                    <Name>PointScale7</Name>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:Function name="in2">
                                <ogc:Function name="geometryType">
                                    <ogc:PropertyName>geometry</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>Point</ogc:Literal>
                                <ogc:Literal>MultiPoint</ogc:Literal>
                            </ogc:Function>
                            <ogc:Literal>true</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
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
                </Rule>

                <Rule>
                    <Name>PointScale8</Name>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:Function name="in2">
                                <ogc:Function name="geometryType">
                                    <ogc:PropertyName>geometry</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>Point</ogc:Literal>
                                <ogc:Literal>MultiPoint</ogc:Literal>
                            </ogc:Function>
                            <ogc:Literal>true</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
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
                </Rule>

                <Rule>
                    <Name>PointScale9</Name>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:Function name="in2">
                                <ogc:Function name="geometryType">
                                    <ogc:PropertyName>geometry</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>Point</ogc:Literal>
                                <ogc:Literal>MultiPoint</ogc:Literal>
                            </ogc:Function>
                            <ogc:Literal>true</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
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
                </Rule>

                <Rule>
                    <Name>PointScale10</Name>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:Function name="in2">
                                <ogc:Function name="geometryType">
                                    <ogc:PropertyName>geometry</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>Point</ogc:Literal>
                                <ogc:Literal>MultiPoint</ogc:Literal>
                            </ogc:Function>
                            <ogc:Literal>true</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
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
                </Rule>

                <Rule>
                    <Name>PointScale11</Name>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:Function name="in2">
                                <ogc:Function name="geometryType">
                                    <ogc:PropertyName>geometry</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>Point</ogc:Literal>
                                <ogc:Literal>MultiPoint</ogc:Literal>
                            </ogc:Function>
                            <ogc:Literal>true</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
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
                </Rule>

                <Rule>
                    <Name>PointScale12</Name>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:Function name="in2">
                                <ogc:Function name="geometryType">
                                    <ogc:PropertyName>geometry</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>Point</ogc:Literal>
                                <ogc:Literal>MultiPoint</ogc:Literal>
                            </ogc:Function>
                            <ogc:Literal>true</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
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
                </Rule>

                <Rule>
                    <Name>PointScale13</Name>
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:Function name="in2">
                                <ogc:Function name="geometryType">
                                    <ogc:PropertyName>geometry</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>Point</ogc:Literal>
                                <ogc:Literal>MultiPoint</ogc:Literal>
                            </ogc:Function>
                            <ogc:Literal>true</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
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
                    <!--PointSymbolizer>
                      <Geometry><ogc:Function name="vertices"><ogc:PropertyName>geometry</ogc:PropertyName></ogc:Function></Geometry>
                      <Graphic>
                        <Mark>
                          <WellKnownName>square</WellKnownName>
                          <Fill>
                            <CssParameter name="fill"><ogc:PropertyName>stroke_color</ogc:PropertyName></CssParameter>
                          </Fill>
                        </Mark>
                        <Size>6</Size>
                      </Graphic>
                    </PointSymbolizer-->
                    <!--TextSymbolizer>
                      <Label>
                        <ogc:PropertyName>name</ogc:PropertyName>
                      </Label>
                      <Font>
                        <CssParameter name="font-family">Arial</CssParameter>
                        <CssParameter name="font-size">11</CssParameter>
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
                        <CssParameter name="fill"><ogc:PropertyName>stroke_color</ogc:PropertyName></CssParameter>
                      </Fill>
                      <VendorOption name="autoWrap">60</VendorOption>
                      <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer -->
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
                    <!--PointSymbolizer>
                      <Geometry><ogc:Function name="vertices"><ogc:PropertyName>geometry</ogc:PropertyName></ogc:Function></Geometry>
                      <Graphic>
                        <Mark>
                          <WellKnownName>square</WellKnownName>
                          <Fill>
                            <CssParameter name="fill"><ogc:PropertyName>stroke_color</ogc:PropertyName></CssParameter>
                          </Fill>
                        </Mark>
                        <Size>6</Size>
                      </Graphic>
                    </PointSymbolizer-->
                    <!--TextSymbolizer>
                      <Label>
                        <ogc:PropertyName>name</ogc:PropertyName>
                      </Label>
                      <Font>
                        <CssParameter name="font-family">Arial</CssParameter>
                        <CssParameter name="font-size">11</CssParameter>
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
                        <CssParameter name="fill"><ogc:PropertyName>stroke_color</ogc:PropertyName></CssParameter>
                      </Fill>
                      <VendorOption name="autoWrap">60</VendorOption>
                      <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer -->
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
                            <ogc:PropertyIsEqualTo>
                                <ogc:Function name="isNull">
                                    <ogc:PropertyName>border_color</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>false</ogc:Literal>
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
                    <!--TextSymbolizer>
                      <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                      <Font>
                        <CssParameter name="font-family">Arial</CssParameter>
                        <CssParameter name="font-size">20</CssParameter>
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
                        <CssParameter name="fill"><ogc:PropertyName>stroke_color</ogc:PropertyName></CssParameter>
                      </Fill>
                      <VendorOption name="autoWrap">60</VendorOption>
                      <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer -->
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
                                <ogc:Function name="isNull">
                                    <ogc:PropertyName>border_color</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>false</ogc:Literal>
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
                    <!--TextSymbolizer>
                      <Label><ogc:PropertyName>name</ogc:PropertyName></Label>
                      <Font>
                        <CssParameter name="font-family">Arial</CssParameter>
                        <CssParameter name="font-size">20</CssParameter>
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
                        <CssParameter name="fill"><ogc:PropertyName>stroke_color</ogc:PropertyName></CssParameter>
                      </Fill>
                      <VendorOption name="autoWrap">60</VendorOption>
                      <VendorOption name="maxDisplacement">150</VendorOption>
                    </TextSymbolizer -->
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
                                <ogc:Function name="isNull">
                                    <ogc:PropertyName>border_color</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>false</ogc:Literal>
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
                                <ogc:Function name="isNull">
                                    <ogc:PropertyName>border_color</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>false</ogc:Literal>
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
                                <ogc:Function name="isNull">
                                    <ogc:PropertyName>border_color</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>false</ogc:Literal>
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
                                <ogc:Function name="isNull">
                                    <ogc:PropertyName>border_color</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>false</ogc:Literal>
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
                                <ogc:Function name="isNull">
                                    <ogc:PropertyName>border_color</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>false</ogc:Literal>
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
                                <ogc:Function name="isNull">
                                    <ogc:PropertyName>border_color</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>false</ogc:Literal>
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
                                <ogc:Function name="isNull">
                                    <ogc:PropertyName>border_color</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>false</ogc:Literal>
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
                                <ogc:Function name="isNull">
                                    <ogc:PropertyName>border_color</ogc:PropertyName>
                                </ogc:Function>
                                <ogc:Literal>false</ogc:Literal>
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
                            <ogc:PropertyIsNull>
                                <ogc:PropertyName>border_color</ogc:PropertyName>
                            </ogc:PropertyIsNull>
                        </ogc:And>
                    </ogc:Filter>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill"><ogc:PropertyName>fill_color</ogc:PropertyName></CssParameter>
                        </Fill>
                    </PolygonSymbolizer>
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
                            <ogc:PropertyIsNull>
                                <ogc:PropertyName>border_color</ogc:PropertyName>
                            </ogc:PropertyIsNull>
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
                    </PolygonSymbolizer>
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
                            <ogc:PropertyIsNull>
                                <ogc:PropertyName>border_color</ogc:PropertyName>
                            </ogc:PropertyIsNull>
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
                    </PolygonSymbolizer>
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
                            <ogc:PropertyIsNull>
                                <ogc:PropertyName>border_color</ogc:PropertyName>
                            </ogc:PropertyIsNull>
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
                    </PolygonSymbolizer>
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
                            <ogc:PropertyIsNull>
                                <ogc:PropertyName>border_color</ogc:PropertyName>
                            </ogc:PropertyIsNull>
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
                    </PolygonSymbolizer>
                </Rule>
            </FeatureTypeStyle>
        </UserStyle>
    </NamedLayer>
</StyledLayerDescriptor>