package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.service.ServiceRuntimeException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.junit.Test;
import org.oskari.xml.XmlHelper;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class MetadataCatalogueResultParserTest {

    @Test
    public void parseResult() throws Exception {
        Element root = XmlHelper.parseXML(getClass().getResourceAsStream("GetRecords-response.xml"));
        MetadataCatalogueResultParser RESULT_PARSER = new MetadataCatalogueResultParser();

        AtomicBoolean noException = new AtomicBoolean(true);
        getResults(root).forEach(metadata -> {
            try {
                final SearchResultItem item = RESULT_PARSER.parseResult(metadata);
                System.out.println(item.getTitle());
            } catch (Exception e) {
                noException.set(false);
                e.printStackTrace();
            }
        });
        assertTrue("Didn't get exception", noException.get());
    }
    protected Stream<Element> getResults(Element root) {
        if (!"GetRecordsResponse".equals(XmlHelper.getLocalName(root))) {
            throw new ServiceRuntimeException("Unexpected response. Expected root element 'GetRecordsResponse'");
        }
        Element results = XmlHelper.getFirstChild(root, "SearchResults");
        if (results == null) {
            throw new ServiceRuntimeException(XmlHelper.generateUnexpectedElementMessage(root));
        }
        return XmlHelper.getChildElements(results, "MD_Metadata");
    }

}