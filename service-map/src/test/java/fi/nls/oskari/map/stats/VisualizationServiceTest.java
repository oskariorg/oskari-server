package fi.nls.oskari.map.stats;

import fi.nls.oskari.domain.map.stats.StatsVisualization;
import org.apache.axiom.om.OMElement;
import org.junit.Test;

import static junit.framework.Assert.*;

public class VisualizationServiceTest {

    private VisualizationService service = new VisualizationService();

    @Test
    public void testGetXML() throws Exception {
        final OMElement xml = service.getXML(getDummyVisualization(), "fi");
        assertNotNull("XML shouldn't be null", xml);
        assertEquals("XML root element should be 'Visualization'", "Visualization", xml.getLocalName());
    }


    @Test
    public void testGetDefaultXSLT() throws Exception {
        final OMElement xslt = service.getDefaultXSLT();
        assertNotNull("XSLT shouldnt be null", xslt);
        assertEquals("XSLT root element should be 'stylesheet'", "stylesheet", xslt.getLocalName());
    }

    @Test
    public void testTransform() throws Exception {

        final OMElement xml = service.getXML(getDummyVisualization(), "fi");
        final String sld = service.transform(xml, service.getDefaultXSLT());
        assertNotNull("SLD shouldn't be null", sld);
        assertTrue("SLD should have rules", sld.indexOf("<Rule>") > 0);
    }

    private StatsVisualization getDummyVisualization() {
        final StatsVisualization visualization = new StatsVisualization();
        return visualization;
    }
}

