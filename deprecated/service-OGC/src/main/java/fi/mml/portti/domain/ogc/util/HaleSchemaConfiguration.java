package fi.mml.portti.domain.ogc.util;

import fi.nls.oskari.log.LogFactory;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xs.XSConfiguration;

import fi.mml.portti.domain.ogc.util.GmlHelper.ConfigurationType;
import fi.nls.oskari.log.Logger;

/**
 * Parser configuration based on {@link TypeDefinition}s
 *
 * @author Simon Templer
 * @partner 01 / Fraunhofer Institute for Computer Graphics Research
 * @version $Id$ 
 */
public class HaleSchemaConfiguration extends Configuration {
        
        private static final Logger log = LogFactory.getLogger(HaleSchemaConfiguration.class);
        
        /**
         * Constructor
         * 
         * @param type the configuration type 
         * @param namespace the schema namespace
         * @param schemaLocation the schema location
         * @param elements the schema elements
         */
        public HaleSchemaConfiguration(ConfigurationType type, String namespace, String schemaLocation) {
        super(new HaleSchemaXSD(type, namespace, schemaLocation));
        
        addDependency(new XSConfiguration());
        
        // add GML dependency
        switch (type) {
                case GML2:
                        addDependency(new org.geotools.gml2.GMLConfiguration());
                        break;
                case GML3_2:
                        addDependency(new org.geotools.gml3.v3_2.GMLConfiguration());
                        break;
                case GML3:
                        // fall through
                default:
                        addDependency(new GMLConfiguration());
                        break;
                }
        
        
    }


        
}

