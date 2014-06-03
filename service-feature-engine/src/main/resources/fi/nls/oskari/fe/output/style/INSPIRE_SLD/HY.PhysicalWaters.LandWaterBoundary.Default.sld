<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor version="1.1.0" xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:sld="http://www.opengis.net/sld" xmlns:se="http://www.opengis.net/se"> 
 <sld:NamedLayer>
    <se:Name>HY.PhysicalWaters.Waterbodies</se:Name>
    <sld:UserStyle>
      <se:Name>HY.PhysicalWaters.LandWaterBoundary.Default</se:Name>
      <sld:IsDefault>1</sld:IsDefault>
      <se:FeatureTypeStyle version="1.1.0">
        <se:Description>
          <se:Title>Land water boundary default style</se:Title>
          <se:Abstract>The contact line between a land mass and a water body is portrayed by a solid blue (#33CCFF) line with stroke width of 1 pixel.</se:Abstract>
        </se:Description>
        <se:FeatureTypeName>PhysicalWaters.LandWaterBoundary</se:FeatureTypeName>
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
      </se:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:NamedLayer>
</StyledLayerDescriptor>
