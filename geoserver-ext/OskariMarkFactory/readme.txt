Geoserver extension for dynamic point symbolizers

Install

Run "mvn install" and copy the resulting jar file to the server's WEB-INF/lib/ directory, e.g. /opt/liferay-portal-6.0.6/tomcat-6.0.29/webapps/geoserver/WEB-INF/lib/. 

Introduction

This custom GeoServer extension generates dynamic point symbols from True Type Font files. TTF files can be created and edited by most font editors, e.g. FontForge. When finished, the TTF file needs to be copied to GeoServer style directory, e.g. /var/opt/geoserver/data/styles/. Also the front end preview renderer needs identical data in the file Oskari/bundles/framework/bundle/divmanazer/component/visualization-form/PointForm.js. Conversion from ttf to js format is achieved by cufon at http://cufon.shoqolate.com/generate/.

The correct shape, color, size and optional boundary color of the point symbol can be defined in the SLD file as follows:

<PointSymbolizer>
  <Graphic>
	<Mark>
	  <WellKnownName>oskari://dot-markers#0xe001</WellKnownName>
	  <Fill>
		<CssParameter name="fill">#00FF00</CssParameter>
	  </Fill>
	  <Stroke>
		<CssParameter name="stroke">#B4B4B4</CssParameter>
	  </Stroke>
	</Mark>
	<Size>30</Size>
  </Graphic>
</PointSymbolizer>

References:

GeoServer documentation of graphic symbology
http://docs.geoserver.org/stable/en/user/styling/sld-extensions/pointsymbols.html

Free font editor
http://fontforge.org/
