package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.SearchResultItem;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class MetadataCatalogueResultParserTest {

    @Test
    public void parseResult() throws Exception {
        final OMXMLParserWrapper stAXOMBuilder = OMXMLBuilderFactory.createOMBuilder(getClass().getResourceAsStream("GetRecords-response.xml"));
        MetadataCatalogueResultParser RESULT_PARSER = new MetadataCatalogueResultParser();

        final Iterator<OMElement> results = getResultsElement(stAXOMBuilder).getChildrenWithLocalName("MD_Metadata");
        while(results.hasNext()) {
            final SearchResultItem item = RESULT_PARSER.parseResult(results.next(), "fi");
            // System.out.println(item.getTitle());
        }
        assertTrue("Didn't get exception", true);
    }

    private OMElement getResultsElement(final OMXMLParserWrapper builder) {
        final Iterator<OMElement> resultIt = builder.getDocumentElement().getChildrenWithLocalName("SearchResults");
        if(resultIt.hasNext()) {
            return resultIt.next();
        }
        return null;
    }
}