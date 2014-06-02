<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor version="1.1.0" xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:sld="http://www.opengis.net/sld" xmlns:se="http://www.opengis.net/se">     
<sld:NamedLayer>
    <se:Name>HY.Network</se:Name>
    <sld:UserStyle>
      <se:Name>HY.Network.Default</se:Name>
      <sld:IsDefault>1</sld:IsDefault>
      <se:FeatureTypeStyle version="1.1.0">
        <se:Description>
          <se:Title>Hydrographic network default style</se:Title>
          <se:Abstract>Hydrographic network is rendered by solid blue (#33CCFF) lines with stroke width of 1 pixel and 3 pixel size filled circles with black (#000000) border.</se:Abstract>
        </se:Description>
        <se:FeatureTypeName>Network.WatercourseLink</se:FeatureTypeName>
        <se:Rule>
          <se:LineSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Stroke>
					<se:SvgParameter name="stroke">#33CCFF</se:SvgParameter>
					<se:SvgParameter name="stroke-width">1</se:SvgParameter>
            </se:Stroke>
          </se:LineSymbolizer>
        </se:Rule>
        <se:FeatureTypeName>Network.HydroNode</se:FeatureTypeName>
        <se:Rule>
          <ogc:Filter>
            <ogc:Or>
              <ogc:PropertyIsEqualTo>
                 <ogc:PropertyName>hydroNodeCategory</ogc:PropertyName>
                    <ogc:Literal>outlet</ogc:Literal>
              </ogc:PropertyIsEqualTo>
              <ogc:PropertyIsEqualTo>
                 <ogc:PropertyName>hydroNodeCategory</ogc:PropertyName>
                    <ogc:Literal>junction</ogc:Literal>
              </ogc:PropertyIsEqualTo>
              <ogc:PropertyIsEqualTo>
                 <ogc:PropertyName>hydroNodeCategory</ogc:PropertyName>
                    <ogc:Literal>source</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
           </ogc:Filter>
          <se:PointSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Graphic>
              <se:Mark>
                <se:WellKnownName>circle</se:WellKnownName>
                <se:Fill>
				     <se:SvgParameter name="fill">#CCFFFF</se:SvgParameter>
                </se:Fill>
                <se:Stroke>
					<se:SvgParameter name="stroke">#000000</se:SvgParameter>
                </se:Stroke>
              </se:Mark>
              <se:Size>
			       <se:SvgParameter name="size">3</se:SvgParameter>
              </se:Size>
            </se:Graphic>
          </se:PointSymbolizer>
          <ogc:Filter>
            <ogc:Or>
              <ogc:PropertyIsEqualTo>
                 <ogc:PropertyName>hydroNodeCategory</ogc:PropertyName>
                    <ogc:Literal>flowConstriction</ogc:Literal>
              </ogc:PropertyIsEqualTo>
              <ogc:PropertyIsEqualTo>
                 <ogc:PropertyName>hydroNodeCategory</ogc:PropertyName>
                    <ogc:Literal>regulation</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Or>
           </ogc:Filter>
          <se:PointSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Graphic>
              <se:Mark>
                <se:WellKnownName>circle</se:WellKnownName>
                <se:Fill>
				     <se:SvgParameter name="fill">#000000</se:SvgParameter>
                </se:Fill>
              </se:Mark>
              <se:Size>
			       <se:SvgParameter name="size">3</se:SvgParameter>
              </se:Size>
            </se:Graphic>
          </se:PointSymbolizer>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
               <ogc:PropertyName>hydroNodeCategory</ogc:PropertyName>
                  <ogc:Literal>boundary</ogc:Literal>
              </ogc:PropertyIsEqualTo>
           </ogc:Filter>
          <se:PointSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Graphic>
              <se:Mark>
                <se:WellKnownName>circle</se:WellKnownName>
                <se:Fill>
				     <se:SvgParameter name="fill">#FF0000</se:SvgParameter>
                </se:Fill>
                <se:Stroke>
					<se:SvgParameter name="stroke">#000000</se:SvgParameter>
                </se:Stroke>
              </se:Mark>
              <se:Size>
			       <se:SvgParameter name="size">3</se:SvgParameter>
              </se:Size>
            </se:Graphic>
          </se:PointSymbolizer>
        </se:Rule>
      </se:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:NamedLayer>
</StyledLayerDescriptor>
