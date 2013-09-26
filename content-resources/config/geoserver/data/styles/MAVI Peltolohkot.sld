<?xml version="1.0" encoding="ISO-8859-1"?>
	<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
	 xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	 xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd">
	    <NamedLayer>
	    <Name>peltolohkot</Name>
	    <UserStyle>
	      <Title>Peltolohkot</Title>
	      <Abstract>Kuvaustekniikka Maaseutuviraston Peltolohkoille</Abstract>
	      <FeatureTypeStyle>
	     <Rule>
	       <Title>Polygonit</Title>
			<PolygonSymbolizer>
				<Fill> 
					<CssParameter name="fill">#00CED1</CssParameter>
					<CssParameter name="fill-opacity">0</CssParameter>
				</Fill>
	       	<Stroke>
	       		<CssParameter name="stroke">#FF00FF</CssParameter>
	           	<CssParameter name="stroke-width">1</CssParameter>
	      		</Stroke>
	      </PolygonSymbolizer> 
	 	   </Rule>
	      </FeatureTypeStyle>
	    </UserStyle>
	  </NamedLayer>
	</StyledLayerDescriptor>