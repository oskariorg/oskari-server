<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor version="1.1.0" xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:sld="http://www.opengis.net/sld" xmlns:se="http://www.opengis.net/se">
  <sld:NamedLayer>
    <se:Name>TN.CableTransportNetwork.CablewayLink</se:Name>
    <sld:UserStyle>
      <se:Name> TN.CableTransportNetwork.CablewayLink.Default</se:Name>
      <sld:IsDefault>1</sld:IsDefault>
      <se:FeatureTypeStyle version="1.1.0">
        <se:Description>
          <se:Title>CableTransportNetworkDefaultStyle</se:Title>
          <se:Abstract> The geometry is rendered as a solid Black line with a stroke width of 3 pixel (#B10787).  Ends are rounded and have a 2 pixel black casing (#000000).</se:Abstract>
        </se:Description>
        <se:FeatureTypeName>CablewayLink</se:FeatureTypeName>
        <se:Rule>
          <se:LineSymbolizer>
            <se:Geometry>
              <ogc:PropertyName>Network:centerlineGeometry</ogc:PropertyName>
            </se:Geometry>
            <se:Stroke/>
          </se:LineSymbolizer>
        </se:Rule>
      </se:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:NamedLayer>
</StyledLayerDescriptor>

