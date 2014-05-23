<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor version="1.1.0" xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:sld="http://www.opengis.net/sld" xmlns:se="http://www.opengis.net/se">       
<sld:NamedLayer>
    <se:Name>HY.ManMadeObject</se:Name>
    <sld:UserStyle>
      <se:Name> HY.PhysicalWaters.ManMadeObject.Default</se:Name>
      <sld:IsDefault>1</sld:IsDefault>
      <se:FeatureTypeStyle version="1.1.0">
        <se:Description>
          <se:Title> Man-made objects default style</se:Title>
          <se:Abstract> There are only depicted the fully functional objects. Punctual objects are depicted with symbols; if the geometry is a curve they are depicted in solid or dashed lines with different stroke width and different colours depending on the feature type; if the geometry is a surface it will be a filled polygon of solid colour adding or not some marks, depending on the feature type.</se:Abstract>
        </se:Description>
        <se:FeatureTypeName>Crossing</se:FeatureTypeName>
        <se:Rule>
          <ogc:Filter>
            <!--FULLY FUNCTIONAL Bridge-->
            <ogc:and>
             <se:PropertyIsEqualTo>
               <ogc:PropertyName>condition</ogc:PropertyName>
               <ogc:Literal>functional</ogc:Literal>
             </se:PropertyIsEqualTo>
             <se:PropertyIsEqualTo>
               <ogc:PropertyName>type</ogc:PropertyName>
               <ogc:Literal>bridge</ogc:Literal>
             </se:PropertyIsEqualTo>
            </ogc:and>
          </ogc:Filter>
          <se:PointSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Graphic>
              <se:ExternalGraphic>
                 <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"     xlink:type="simple" 
                        xlink:href="http://.../bridge.png"/>
                 <Format>image/png</Format> 
              </ExternalGraphic> 
              <se:Size>
		          <se:SvgParameter name="size">10.0</se:SvgParameter>
              </se:Size>
            </se:Graphic>
          </se:PointSymbolizer>
          <se:LineSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Stroke>
					<se:SvgParameter name="stroke">#999999</se:SvgParameter>
					<se:SvgParameter name="stroke-width">2</se:SvgParameter>
            </se:Stroke>
          </se:LineSymbolizer>
          <se:PolygonSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Fill>
				     <se:SvgParameter name="fill">#CCCCCC</se:SvgParameter>
            </se:Fill>
            <se:Stroke>
					<se:SvgParameter name="stroke">#999999</se:SvgParameter>
               <se:SvgParameter name="stroke-width">2</se:SvgParameter>
            </se:Stroke>
          </se:PolygonSymbolizer>
        </se:Rule>
        <se:FeatureTypeName>DamOrWeir</se:FeatureTypeName>
        <se:Rule>
          <ogc:Filter>
            <!--FULLY FUNCTIONAL-->
             <se:PropertyIsEqualTo>
               <ogc:PropertyName>condition</ogc:PropertyName>
               <ogc:Literal>functional</ogc:Literal>
             </se:PropertyIsEqualTo>
          </ogc:Filter>
          <se:PointSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Graphic>
              <se:Mark>
                <se:WellKnownName>X</se:WellKnownName>
                <se:Fill>
			         <se:SvgParameter name="fill">#666666</se:SvgParameter>
                </se:Fill>           
              </se:Mark>
              <se:Size>
	             <se:SvgParameter name="size">12.0</se:SvgParameter>
              </se:Size>
            </se:Graphic>
          </se:PointSymbolizer>
          <se:LineSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Stroke>
					<se:SvgParameter name="stroke">#666666</se:SvgParameter>
					<se:SvgParameter name="stroke-width">3</se:SvgParameter>
            </se:Stroke>
          </se:LineSymbolizer>
          <se:PolygonSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Fill>
				     <se:SvgParameter name="fill">#999999</se:SvgParameter>
            </se:Fill>
            <se:Stroke>
					<se:SvgParameter name="stroke">#666666</se:SvgParameter>
               <se:SvgParameter name="stroke-width">3</se:SvgParameter>
            </se:Stroke>
          </se:PolygonSymbolizer>
        </se:Rule>
        <se:FeatureTypeName>ShorelineConstruction</se:FeatureTypeName>
        <se:Rule>
          <ogc:Filter>
            <!--FULLY FUNCTIONAL-->
             <se:PropertyIsEqualTo>
               <ogc:PropertyName>condition</ogc:PropertyName>
               <ogc:Literal>functional</ogc:Literal>
             </se:PropertyIsEqualTo>
          </ogc:Filter>
          <se:PointSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Graphic>
              <se:Mark>
                <se:WellKnownName>triangle</se:WellKnownName>
                  <se:Fill>
      			    <se:SvgParameter name="fill">#666666</se:SvgParameter>
                  </se:Fill>
              </se:Mark>
              <se:Size>
			         <se:SvgParameter name="size">10</se:SvgParameter>
              </se:Size>
            </se:Graphic>
          </se:PointSymbolizer>
          <se:LineSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Stroke>
					<se:SvgParameter name="stroke">#666666</se:SvgParameter>
					<se:SvgParameter name="stroke-width">2</se:SvgParameter>
            </se:Stroke>
          </se:LineSymbolizer>
          <se:PolygonSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Fill>
				     <se:SvgParameter name="fill">#999999</se:SvgParameter>
            </se:Fill>
            <se:Stroke>
					<se:SvgParameter name="stroke">#666666</se:SvgParameter>
               <se:SvgParameter name="stroke-width">2</se:SvgParameter>
            </se:Stroke>
          </se:PolygonSymbolizer>
        </se:Rule>
        <se:FeatureTypeName>Lock</se:FeatureTypeName>
        <se:Rule>
          <ogc:Filter>
            <!--FULLY FUNCTIONAL-->
             <se:PropertyIsEqualTo>
               <ogc:PropertyName>condition</ogc:PropertyName>
               <ogc:Literal>functional</ogc:Literal>
             </se:PropertyIsEqualTo>
          </ogc:Filter>
          <se:PointSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
                 <se:Graphic>
                   <se:Mark>
                    <se:WellKnownName>X</se:WellKnownName>
                     <se:Fill>
	  			          <se:SvgParameter name="fill">#666666</se:SvgParameter>
                     </se:Fill>
                   </se:Mark>
                   <se:Size>
	                 <se:SvgParameter name="size">8.0</se:SvgParameter>
                   </se:Size>
                 </se:Graphic>
          </se:PointSymbolizer>
          <se:LineSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Stroke>
					<se:SvgParameter name="stroke">#666666</se:SvgParameter>
					<se:SvgParameter name="stroke-width">1</se:SvgParameter>
            </se:Stroke>
          </se:LineSymbolizer>
          <se:PolygonSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Fill>
				     <se:SvgParameter name="fill">#999999</se:SvgParameter>
            </se:Fill>
            <se:Stroke>
					<se:SvgParameter name="stroke">#666666</se:SvgParameter>
               <se:SvgParameter name="stroke-width">1</se:SvgParameter>
            </se:Stroke>
          </se:PolygonSymbolizer>
        </se:Rule>
        <se:FeatureTypeName>Ford</se:FeatureTypeName>
        <se:Rule>
          <ogc:Filter>
            <!--FULLY FUNCTIONAL-->
             <se:PropertyIsEqualTo>
               <ogc:PropertyName>condition</ogc:PropertyName>
               <ogc:Literal>functional</ogc:Literal>
             </se:PropertyIsEqualTo>
          </ogc:Filter>
          <se:PointSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Graphic>
              <se:Mark>
                <se:WellKnownName>square</se:WellKnownName>
                <se:Fill>
				     <se:SvgParameter name="fill">#FFCCCC</se:SvgParameter>
                </se:Fill>
               <se:Stroke>
					<se:SvgParameter name="stroke">#CCFFFF</se:SvgParameter>
                </se:Stroke>
              </se:Mark>
              <se:Opacity>
                <se:SvgParameter name="opacity">0.5</se:SvgParameter>
              </se:Opacity>
              <se:Size>
			       <se:SvgParameter name="size">3</se:SvgParameter>
              </se:Size>
            </se:Graphic>
          </se:PointSymbolizer>
          <se:LineSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Stroke>
					<se:SvgParameter name="stroke">#FFCCCC</se:SvgParameter>
					<se:SvgParameter name="stroke-width">1</se:SvgParameter>
            </se:Stroke>
          </se:LineSymbolizer>
          <se:PolygonSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Fill>
					<se:SvgParameter name="fill">#FFCCCC</se:SvgParameter>
               <se:SvgParameter name="fill-opacity">0.5</se:SvgParameter>
            </se:Fill>
          </se:PolygonSymbolizer>
        </se:Rule>
      </se:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:NamedLayer>
</StyledLayerDescriptor>
