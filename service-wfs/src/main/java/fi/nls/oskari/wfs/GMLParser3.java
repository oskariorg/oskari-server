package fi.nls.oskari.wfs;

import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import org.eclipse.xsd.util.XSDSchemaLocator;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Parser;
import org.picocontainer.MutablePicoContainer;

import javax.xml.namespace.QName;

/**
 * Geotools Parser with GML 3 configuration
 */
public class GMLParser3 {
	/**
	 * Creates Geotools parser with GML configuration with schema handling
	 * 
	 * @param layer
	 * @return parser
	 */
	public static Parser getParser(final WFSLayerStore layer) {
		GMLConfiguration configuration = new GMLConfiguration() {
		    public void configureContext(final MutablePicoContainer container) {
		        super.configureContext(container);
		        XSDSchemaLocator locator = new CachingSchemaLocator(layer.getUsername(), layer.getPassword());
				QName key = new QName("portti", "schemaLocator"); // ?!??!
		        container.registerComponentInstance(key, locator);
		    }
		};

        // Arc and surface support
        configuration.setExtendedArcSurfaceSupport(true);

		Parser parser = new Parser(configuration);
		parser.setValidating(false);
		parser.setFailOnValidationError(false);
		parser.setStrict(false);
		
		return parser;
	}

	/**
	 * Creates and returns a GML Parser
	 *
	 * @return GML parser
	 */
	public static Parser getParserWithoutSchemaLocator() {
		GMLConfiguration configuration = new GMLConfiguration();

        // Arc and surface support
        configuration.setExtendedArcSurfaceSupport(true);
        
        Parser parser = new Parser(configuration);
        parser.setValidating(false);
        parser.setFailOnValidationError(false);
        parser.setStrict(false);

		return parser;
	}
}
