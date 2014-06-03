<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor version="1.1.0" xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:sld="http://www.opengis.net/sld" xmlns:se="http://www.opengis.net/se">     
<sld:NamedLayer>
    <se:Name>HY.HydroPointOfInterest</se:Name>
    <sld:UserStyle>
      <se:Name> HY.PhysicalWaters.HydroPointOfInterest.Default</se:Name>
      <sld:IsDefault>1</sld:IsDefault>
      <se:FeatureTypeStyle version="1.1.0">
        <se:Description>
          <se:Title> Hydrographic points of interest default style</se:Title>
          <se:Abstract> Fluvial points as rapids or falls are depicted with symbols; if the geometry is a curve they are depicted in aligned blue (#0066FF) marks (stars for Falls and crosses for Rapids); if the geometry is a surface it will be an area with blue (#0066FF) marks (stars for Falls and crosses for Rapids).</se:Abstract>
        </se:Description>
        <se:FeatureTypeName>Rapids</se:FeatureTypeName>
        <se:Rule>
          <se:PointSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
                 <se:Graphic>
                    <se:ExternalGraphic>
                      <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"     xlink:type="simple" 
                        xlink:href="http://.../rapids.png"/> 
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
              <se:GraphicStroke>
               <se:Graphic>
                 <se:Mark>
                   <se:WellKnownName>cross</se:WellKnownName>
                   <se:Fill>
		      		    <se:SvgParameter name="fill">#0066FF</se:SvgParameter>
                   </se:Fill>
                 </se:Mark>
                <se:Size>
	              <se:SvgParameter name="size">5.0</se:SvgParameter>
                </se:Size>
               </se:Graphic>
              </se:GraphicStroke>
            </se:Stroke>
          </se:LineSymbolizer>
          <se:PolygonSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Fill>
              <se:GraphicFill>
               <se:Graphic>
                 <se:Mark>
                   <se:WellKnownName>cross</se:WellKnownName>
                   <se:Fill>
				         <se:SvgParameter name="fill">#0066FF</se:SvgParameter>
                   </se:Fill>
                 </se:Mark>
                <se:Size>
	              <se:SvgParameter name="size">5.0</se:SvgParameter>
                </se:Size>
               </se:Graphic>
              </se:GraphicFill>
            </se:Fill>
          </se:PolygonSymbolizer>
        </se:Rule>
        <se:FeatureTypeName>Falls</se:FeatureTypeName>
        <se:Rule>
          <se:PointSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
                 <se:Graphic>
                    <se:ExternalGraphic>
                      <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"     xlink:type="simple" 
                        xlink:href="http://.../falls.png"/> 
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
              <se:GraphicStroke>
               <se:Graphic>
                 <se:Mark>
                   <se:WellKnownName>star</se:WellKnownName>
                   <se:Fill>
				         <se:SvgParameter name="fill">#0066FF</se:SvgParameter>
                   </se:Fill>
                 </se:Mark>
                <se:Size>
	              <se:SvgParameter name="size">5.0</se:SvgParameter>
                </se:Size>
               </se:Graphic>
              </se:GraphicStroke>
            </se:Stroke>
          </se:LineSymbolizer>
          <se:PolygonSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Fill>
               <se:GraphicFill>
                <se:Graphic>
                 <se:Mark>
                   <se:WellKnownName>star</se:WellKnownName>
                   <se:Fill>
				         <se:SvgParameter name="fill">#0066FF</se:SvgParameter>
                   </se:Fill>
                 </se:Mark>
                <se:Size>
	              <se:SvgParameter name="size">5.0</se:SvgParameter>
                </se:Size>
               </se:Graphic>
              </se:GraphicFill>
            </se:Fill>
          </se:PolygonSymbolizer>
        </se:Rule>
      </se:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:NamedLayer>
</StyledLayerDescriptor>
