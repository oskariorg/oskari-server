package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.SelectItem;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.util.OskariRuntimeException;
import fi.nls.oskari.wfs.WFSSearchChannelsConfiguration;
import org.json.JSONArray;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default handler for WFS Search channel filter and title
 */
@Oskari(WFSChannelHandler.ID)
public class WFSChannelHandler extends OskariComponent {
    public static final String ID = "DEFAULT";
    protected static final XMLOutputFactory XOF = XMLOutputFactory.newInstance();

    protected void writePropertyIsLike(XMLStreamWriter xsw, String name, String value, Map<String, String> toggles) throws XMLStreamException {
        xsw.writeStartElement("PropertyIsLike");
        for(Map.Entry<String, String> entry: toggles.entrySet()) {
            xsw.writeAttribute(entry.getKey(), entry.getValue());
        }
        xsw.writeStartElement("PropertyName");
        xsw.writeCharacters(name);
        xsw.writeEndElement();
        xsw.writeStartElement("Literal");
        xsw.writeCharacters(value);
        xsw.writeEndElement();
        xsw.writeEndElement();
    }

    protected void writePropertyIsLike(XMLStreamWriter xsw, String name, String value) throws XMLStreamException {
        Map<String, String> toggles = new HashMap<>();
        toggles.put("wildCard", "*");
        toggles.put("singleChar", ".");
        toggles.put("escape", "!");
        toggles.put("matchCase", "false");
        writePropertyIsLike(xsw, name, value, toggles);
    }

    public String createFilter(SearchCriteria sc, WFSSearchChannelsConfiguration config) {
        // override to implement custom filter handling
        String searchStr = sc.getSearchString();
        JSONArray params = config.getParamsForSearch();
        boolean hasMultipleParams = params.length() > 1;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            XMLStreamWriter xsw = XOF.createXMLStreamWriter(baos);
            // don't write start document as it is the <?zml ?> that we don't want here
            xsw.writeStartElement("Filter");

            if (hasMultipleParams){
                xsw.writeStartElement("Or");
            }

            for(int j = 0; j < params.length(); j++) {
                String param = params.optString(j);
                writePropertyIsLike(xsw, param, "*" + searchStr + "*");
            }

            if (hasMultipleParams){
                xsw.writeEndElement();
            }

            xsw.writeEndElement();

            xsw.writeEndDocument();
            xsw.close();

        } catch (XMLStreamException e) {
            throw new OskariRuntimeException("Unable to write filter", e);
        }

        return baos.toString();
    }

    public String getTitle(List<SelectItem> list) {
        final String separator = ", ";
        return getTitle(list, separator);
    }

    public String getTitle(List<SelectItem> list, String separator) {
        return list.stream()
                .map(SelectItem::getValue)
                .collect(Collectors.joining(separator));
    }
}
