package fi.nls.oskari.map.stats;

import fi.nls.oskari.domain.map.stats.StatsVisualization;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.db.BaseIbatisService;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.XmlHelper;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class VisualizationService extends BaseIbatisService<StatsVisualization> {
    
    private static final Logger log = LogFactory.getLogger(VisualizationService.class);

    private final static String DEFAULT_LINE_COLOR = "#000000"; 
    private final static String DEFAULT_LINE_WIDTH = "0";
    
    private final static String DEFAULT_RULE_NAME = "Kunta";
    private final static String DEFAULT_RULE_TITLE = "Eriväri";
    private final static String DEFAULT_RULE_ABSTRACT = "Kuvaus";

    public final static String XSLT_FILE = "stats-sld.xslt";

    final private static OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();

    @Override
    protected String getNameSpace() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<StatsVisualization> findForLayerId(final int layerId) {
        // FIXME/TODO: Parse this information from oskari_maplayer attributes field.
        return new ArrayList<>();
    };

    public StatsVisualization getVisualization(
            final int visId,
            final String classes,
            final String layerName,
            final String filterProperty,
            final String vis
    ) {
        if (visId != -1) {
            // got id -> find from DB
            return find(visId);
        }

        // else we are expecting parameters to construct a visualization
        final StatsVisualization visualization = new StatsVisualization();
        visualization.setClasses(classes);
        visualization.setLayername(layerName);
        visualization.setFilterproperty(filterProperty);
        // vis=<visualization>:<colors>
        final String[] colors = vis.split(":");
        if (colors.length > 1) {
            visualization.setVisualization(colors[0]);
            visualization.setColors(colors[1]);
        }
        // validate that we got all needed params
        if (visualization.isValid()) {
            return visualization;
        }

        log.debug("Tried to create StatsVisualization but param values were not valid:", visualization);
        return null;
    }
    /**
     * Creates an XML presentation of the visualization parameters so we can run an XSLT transform to get an SLD
     * @param visualization params
     * @param lang language to get visualization name to the xml (not really needed since users wont see this)
     * @return XML document containing visualization parameters
     */
    public OMElement getXML(final StatsVisualization visualization, final String lang) {

        // root element
        final OMElement root = OM_FACTORY.createOMElement("Visualization", null);
        final OMAttribute name = OM_FACTORY.createOMAttribute("name", null, visualization.getName(lang));
        root.addAttribute(name);
        final OMAttribute layer = OM_FACTORY.createOMAttribute("layer", null, visualization.getLayername());
        root.addAttribute(layer);
        final OMAttribute linecolor = OM_FACTORY.createOMAttribute("line-color", null, DEFAULT_LINE_COLOR);
        // root.addAttribute(linecolor);
        final OMAttribute linewidth = OM_FACTORY.createOMAttribute("line-width", null, DEFAULT_LINE_WIDTH);
        // root.addAttribute(linewidth);
        
        final String[] classes = visualization.getClassGroups(); 
        final String[] colors = visualization.getGroupColors();
        if(classes.length != colors.length) {
            log.error("Colors and classes lengths are different. Classes:", classes, "Colors:", colors);
            return root;
        }
        
        for(int i = 0; i < classes.length; ++i) {

            final OMElement rule = OM_FACTORY.createOMElement("Range", null);
            final OMAttribute color = OM_FACTORY.createOMAttribute("color", null, colors[i]);
            rule.addAttribute(color);
            rule.addAttribute(linecolor);
            rule.addAttribute(linewidth);

            final OMAttribute ruleName = OM_FACTORY.createOMAttribute("name", null, DEFAULT_RULE_NAME);
            rule.addAttribute(ruleName);
            final OMAttribute ruleTitle = OM_FACTORY.createOMAttribute("title", null, DEFAULT_RULE_TITLE);
            rule.addAttribute(ruleTitle);
            final OMAttribute ruleAbstract = OM_FACTORY.createOMAttribute("abstract", null, DEFAULT_RULE_ABSTRACT);
            rule.addAttribute(ruleAbstract);
            
            final OMAttribute filterProperty = OM_FACTORY.createOMAttribute("property", null, visualization.getFilterproperty());
            rule.addAttribute(filterProperty);
            final String c = classes[i];
            final String[] features = c.split(",");
            for(String feature: features) {
                final OMElement f = OM_FACTORY.createOMElement("Property", null);
                final OMAttribute value = OM_FACTORY.createOMAttribute("value", null, feature);
                f.addAttribute(value);
                rule.addChild(f);
            }
            root.addChild(rule);
        }

        return root;
    }

    /**
     * Transforms an XML document with given XSLT. Used here to get the SLD.
     * @param xml Document to transform
     * @param xslt XSLT for the transformation
     * @return String presentation of the transformation output
     * @throws Exception
     */
    public String transform(final OMElement xml, final OMElement xslt) throws Exception {

        final TransformerFactory factory = XmlHelper.newTransformerFactory();
        final Source xsltSource = xslt.getSAXSource(false);
        final Transformer transformer = factory.newTransformer(xsltSource);
        
        final Source xmlSource = xml.getSAXSource(false);
        StreamResult result = new StreamResult(new ByteArrayOutputStream());
        transformer.transform(xmlSource, result);
        return result.getOutputStream().toString();
    }

    /**
     * Returns a default XSLT. Reads it as a resource through classloader
     * @Todo always building from file. Change so that we load it once to memory
     * and use the reference. Don't just take buildOMElement() result into variable since
     * that was tried and didn't work. Maybe find another builder that creates
     * concrete OMElement if its an inputstream streaming issue?
     * @return
     * @see #XSLT_FILE
     * @throws Exception
     */
    public OMElement getDefaultXSLT() throws Exception {
        return buildOMElement(getClass().getResourceAsStream(XSLT_FILE));
    }

    /**
     * @param inputStream Reads input stream that use to build OMElement
     * @return  OMElement that generated from input stream
     * @throws Exception at error in generating parser
     */
    private OMElement buildOMElement(final InputStream inputStream) throws Exception {
        try {
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            return builder.getDocumentElement();
        }
        catch (XMLStreamException e) {
            String msg = "Error in initializing the parser to build the OMElement.";
            log.error(e, msg);
            throw new Exception(msg, e);
        }
        finally {
            log.info("Reading data from configuration file");
            IOHelper.close(inputStream);
        }
    }
}