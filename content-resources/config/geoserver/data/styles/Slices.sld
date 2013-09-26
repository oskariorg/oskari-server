<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd">
  <NamedLayer>
    <Name>raster</Name>
    <UserStyle>
      <Name>slices</Name>
      <Title>Slices</Title>
      <Abstract>ColorMap-esitys Slices-rasterille</Abstract>
      <FeatureTypeStyle>
        <FeatureTypeName>Feature</FeatureTypeName>
        <Rule>
          <RasterSymbolizer>
            <Opacity>1.0</Opacity>
            <ColorMap type="values">
       <ColorMapEntry color="#BF00BF" quantity="11" opacity="1.0" label="Kerrostaloalueet"/>
       <ColorMapEntry color="#E595E5" quantity="13" opacity="1.0" label="Rivitaloalueet"/>
       <ColorMapEntry color="#FEC2FE" quantity="14" opacity="1.0" label="Erillispientalot"/>
       <ColorMapEntry color="#FEBFBF" quantity="21" opacity="1.0" label="Loma-asuntoalueet"/>
       <ColorMapEntry color="#EAC7A9" quantity="32" opacity="1.0" label="Urheilu ja virkistys"/>
       <ColorMapEntry color="#09BD09" quantity="33" opacity="1.0" label="Puistot"/>
       <ColorMapEntry color="#000000" quantity="61" opacity="1.0" label="Tieliikennealueet"/>
       <ColorMapEntry color="#FE0000" quantity="62" opacity="1.0" label="Yleiset tiet"/>
            </ColorMap>
          </RasterSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>
