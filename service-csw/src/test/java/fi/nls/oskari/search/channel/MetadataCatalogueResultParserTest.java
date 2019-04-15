package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.util.IOHelper;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class MetadataCatalogueResultParserTest {

    @Test
    public void parseResult() throws Exception {
        final StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(getClass().getResourceAsStream("GetRecords-response.xml"));
        MetadataCatalogueResultParser RESULT_PARSER = new MetadataCatalogueResultParser();

        final Iterator<OMElement> results = getResultsElement(stAXOMBuilder).getChildrenWithLocalName("MD_Metadata");
        final long start = System.currentTimeMillis();
        while(results.hasNext()) {
            final SearchResultItem item = RESULT_PARSER.parseResult(results.next(), "fi");
            //System.out.println(item.getTitle());
        }
        assertTrue("Didn't get exception", true);
    }

    private OMElement getResultsElement(final StAXOMBuilder builder) {
        final Iterator<OMElement> resultIt = builder.getDocumentElement().getChildrenWithLocalName("SearchResults");
        if(resultIt.hasNext()) {
            return resultIt.next();
        }
        return null;
    }
}