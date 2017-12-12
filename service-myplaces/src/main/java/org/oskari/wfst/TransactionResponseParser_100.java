package org.oskari.wfst;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.oskari.utils.xml.XML;
import org.oskari.wfst.TransactionResponse_100.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class TransactionResponseParser_100 {

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

        Element result = XML.getChild(transactionResponse, "TransactionResult")
                .orElseThrow(() -> new IllegalArgumentException("Missing required element 'TransactionResult'"));
        Element status = XML.getChild(result, "Status")
                .orElseThrow(() -> new IllegalArgumentException("Missing required element 'Status'"));
        Element statusElement = XML.getChildren(status).get(0);
        Status statusCode = TransactionResponse_100.Status.valueOf(statusElement.getLocalName());
        String locator = XML.getChildValue(result, "Locator");
        String message = XML.getChildValue(result, "Message");

        final List<Element> insertResults = XML.getChildren(transactionResponse, "InsertResult");
        String[] fids = new String[insertResults.size()];
        for (int i = 0; i < insertResults.size(); i++) {
            Element insertResult = insertResults.get(i);
            String fid = XML.getChild(insertResult, "FeatureId")
                .map(FeatureId -> FeatureId.getAttribute("fid"))
                .orElseThrow(() -> new IllegalArgumentException("Expected ogc:FeatureId element inside InsertResult"));
            fids[i] = fid;
        }

        return new TransactionResponse_100(statusCode, locator, message, fids);
    }

}
