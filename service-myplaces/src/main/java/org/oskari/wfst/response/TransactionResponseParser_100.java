package org.oskari.wfst.response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.oskari.utils.xml.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Parses WFS-T TransactionResponse
 */
public class TransactionResponseParser_100 {

    public static TransactionResponse_100 parse(byte[] b)
            throws ParserConfigurationException, SAXException, IOException {
        return parse(new ByteArrayInputStream(b));
    }

    public static TransactionResponse_100 parse(InputStream in)
            throws ParserConfigurationException, SAXException, IOException {
        Document doc = XML.readDocument(in);
        Element transactionResponse = doc.getDocumentElement();
        if (!"WFS_TransactionResponse".equals(transactionResponse.getLocalName())) {
            throw new IllegalArgumentException("Root node must be 'WFS_TransactionResponse'");
        }
        if (!"1.0.0".equals(transactionResponse.getAttribute("version"))) {
            throw new IllegalArgumentException("Attribute 'version' must be '1.0.0'");
        }

        List<Element> insertResults = XML.getChildren(transactionResponse, "InsertResult");
        List<InsertedFeature> insertedFeatures = new ArrayList<>(insertResults.size());
        for (Element insertResult : insertResults) {
            insertedFeatures.add(parseInsertedFeature(insertResult));
        }

        Element transactionResult = XML.getChild(transactionResponse, "TransactionResult")
                .orElseThrow(() -> new IllegalArgumentException(
                        "Missing required element 'TransactionResult'"));

        Element statusElem = XML.getChild(transactionResult, "Status")
                .orElseThrow(() -> new IllegalArgumentException(
                        "Missing required element 'Status'"));
        String statusStr = XML.getFirstChildElement(statusElem)
                .orElseThrow(() -> new IllegalArgumentException(
                        "'Status' element did not have any children"))
                .getLocalName();
        TransactionResponse_100.Status status = TransactionResponse_100.Status.valueOf(statusStr);

        String locator = XML.getChildText(transactionResult, "Locator").orElse(null);
        String message = XML.getChildText(transactionResult, "Message").orElse(null);

        return new TransactionResponse_100(insertedFeatures, status, locator, message);
    }

    private static InsertedFeature parseInsertedFeature(Element insertResult)
            throws IllegalArgumentException {
        String fid = XML.getChild(insertResult, "FeatureId")
                .map(FeatureId -> FeatureId.getAttribute("fid"))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Expected ogc:FeatureId element inside Feature"));
        String handle = insertResult.getAttribute("handle");
        return new InsertedFeature(fid, handle);
    }

}
