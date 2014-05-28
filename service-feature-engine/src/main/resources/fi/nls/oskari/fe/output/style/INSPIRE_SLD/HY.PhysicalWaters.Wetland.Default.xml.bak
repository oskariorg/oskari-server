<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor version="1.1.0" xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:sld="http://www.opengis.net/sld" xmlns:se="http://www.opengis.net/se">           
<sld:NamedLayer>
    <se:Name>LC.HydroObject</se:Name>
    <sld:UserStyle>
      <se:Name> LC.HydroObject.Default</se:Name>
      <sld:IsDefault>1</sld:IsDefault>
      <se:FeatureTypeStyle version="1.1.0">
        <se:Description>
          <se:Title>Hydrographic land cover default style</se:Title>
          <se:Abstract>Shore areas are portrayed as pale yellow (#FFFFCC) surfaces; wetlands are depicted with blue-green (#00CCCC) surfaces and glaciers and snowfields are rendered filled white (#FFFFFF) polygons with a solid blue (#3333CC) border with stroke width of 1 pixel.</se:Abstract>
        </se:Description>
        <se:FeatureTypeName>Shore</se:FeatureTypeName>
        <se:Rule>
          <se:PolygonSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Fill>
					<se:SvgParameter name="fill">#FFFFCC</se:SvgParameter>
            </se:Fill>
          </se:PolygonSymbolizer>
        </se:Rule>
        <se:FeatureTypeName>Wetland</se:FeatureTypeName>
        <se:Rule>
          <se:PolygonSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Fill>
					<se:SvgParameter name="fill">#00CCCC</se:SvgParameter>
            </se:Fill>
          </se:PolygonSymbolizer>
        </se:Rule>
        <se:FeatureTypeName>GlacierSnowfield</se:FeatureTypeName>
        <se:Rule>
          <se:PolygonSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>geometry</ogc:PropertyName>
            </se:Geometry>
            <se:Fill>
					<se:SvgParameter name="Fill">#FFFFFF</se:SvgParameter>
            </se:Fill>
            <se:Stroke>
					<se:SvgParameter name="stroke">#3333CC</se:SvgParameter>
					<se:SvgParameter name="stroke-width">1</se:SvgParameter>
            </se:Stroke>
          </se:PolygonSymbolizer>
        </se:Rule>
      </se:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:NamedLayer>
</StyledLayerDescriptor>
