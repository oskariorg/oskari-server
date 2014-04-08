<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0"
    xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd"
    xmlns="http://www.opengis.net/sld"
    xmlns:ogc="http://www.opengis.net/ogc"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <NamedLayer>
    <Name>user_layer_data</Name>
    <UserStyle>
      <Title>user_layer</Title>
      <FeatureTypeStyle>
      <Rule>
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
        
          <PointSymbolizer>
            <Graphic>
              <Mark>
                <WellKnownName>triangle</WellKnownName>
                <Fill>
                   <CssParameter name="stroke">#000000</CssParameter>
                   <CssParameter name="fill">#0000FF</CssParameter>
                </Fill>
              </Mark>
              <Size>10</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
        <Rule>
          <ogc:Filter>      
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
          </ogc:Filter>
          
      <LineSymbolizer>
 <Stroke>
           <CssParameter name="stroke">#0000FF</CssParameter>
           <CssParameter name="stroke-width">1</CssParameter>
           <CssParameter name="stroke-linecap">round</CssParameter>
         </Stroke>
          </LineSymbolizer>
          <PointSymbolizer>
            <Geometry><ogc:Function name="vertices"><ogc:PropertyName>geometry</ogc:PropertyName></ogc:Function></Geometry>
            <Graphic>
              <Mark>
                <WellKnownName>square</WellKnownName>
                <Fill>
                  <CssParameter name="fill">#FF0000</CssParameter>
                </Fill>
              </Mark>
              <Size>6</Size>
            </Graphic>
          </PointSymbolizer>
        </Rule>
                
       <Rule>  <ogc:Filter>
     <ogc:PropertyIsEqualTo>
       <ogc:Function name="in3">
          <ogc:Function name="geometryType">
              <ogc:PropertyName>geometry</ogc:PropertyName>
          </ogc:Function>
          <ogc:Literal>Polygon</ogc:Literal>
          <ogc:Literal>MultiPolygon</ogc:Literal>
           <ogc:Literal>MultiSurface</ogc:Literal>
       </ogc:Function>
       <ogc:Literal>true</ogc:Literal>
     </ogc:PropertyIsEqualTo>
   </ogc:Filter>
       <PolygonSymbolizer>
         <Fill>
           <CssParameter name="fill">#FF0000</CssParameter>
         </Fill>
         <Stroke>
           <CssParameter name="stroke">#0000FF</CssParameter>
           <CssParameter name="stroke-width">1</CssParameter>
         </Stroke>
       </PolygonSymbolizer>
     </Rule>
   </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>