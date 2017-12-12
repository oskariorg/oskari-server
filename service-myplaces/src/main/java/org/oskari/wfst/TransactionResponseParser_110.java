package org.oskari.wfst;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.oskari.utils.xml.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class TransactionResponseParser_110 {

    public static TransactionResponse_110 parse(InputStream in)
            throws ParserConfigurationException, SAXException, IOException {
        Document doc = XML.readDocument(in);
        Element transactionResponse = doc.getDocumentElement();
        if (!"TransactionResponse".equals(transactionResponse.getLocalName())) {
            throw new IllegalArgumentException("Root node must be 'TransactionResponse'");
        }
        if (!"1.1.0".equals(transactionResponse.getAttribute("version"))) {
            throw new IllegalArgumentException("Attribute 'version' must be '1.1.0'");
        }

        Element summary = XML.getChild(transactionResponse, "TransactionSummary")
                .orElseThrow(() -> new IllegalArgumentException(
                        "Missing required element 'TransactionSummary'"));
        int totalInserted = getRequiredIntFromChild(summary, "totalInserted");
        int totalUpdated = getRequiredIntFromChild(summary, "totalUpdated");
        int totalDeleted = getRequiredIntFromChild(summary, "totalDeleted");

        String[] fids;
        Optional<Element> insertResults = XML.getChild(transactionResponse, "InsertResults");
        if (insertResults.isPresent()) {
            List<Element> insertedFeatures = XML.getChildren(insertResults.get(), "Feature");
            fids = new String[insertedFeatures.size()];
            for (int i = 0; i < insertedFeatures.size(); i++) {
                Element feature = insertedFeatures.get(i);
                fids[i] = XML.getChild(feature, "FeatureId")
                        .map(FeatureId -> FeatureId.getAttribute("fid"))
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Expected ogc:FeatureId element inside Feature"));
            }
        } else {
            fids = new String[0];
        }

        return new TransactionResponse_110(totalInserted, totalUpdated, totalDeleted, fids);
    }

    private static int getRequiredIntFromChild(Element parent, String element) {
        return XML.getChildText(parent, element)
                .map(Integer::parseInt)
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Missing required element '%s'", element)));
    }

}
