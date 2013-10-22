package fi.nls.oskari.search.ktjkiiwfs;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

public interface KTJkiiWFSSearchChannel {

    public interface RegisterUnitId {
        public String getValue();
    }

    public RegisterUnitId convertRequestStringToRegisterUnitID(
            String requestString);

    /**
     * @param request
     * @return
     * @throws IOException
     * @throws XPathExpressionException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws TransformerException
     */
    public List<RegisterUnitParcelSearchResult> searchByRegisterUnitId(
            RegisterUnitId request) throws IOException,
            XPathExpressionException, ParserConfigurationException,
            SAXException, TransformerException;
}
