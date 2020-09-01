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
import java.util.List;

/**
 * Default handler for WFS Search channel filter and title
 */
@Oskari(WFSChannelHandler.ID)
public class WFSChannelHandler extends OskariComponent {
    public static final String ID = "DEFAULT";
    protected static final XMLOutputFactory XOF = XMLOutputFactory.newInstance();

    protected void writePropertyIsLike(XMLStreamWriter xsw, String name, String value) throws XMLStreamException {
        xsw.writeStartElement("PropertyIsLike");
        xsw.writeAttribute("wildCard", "*");
        xsw.writeAttribute("singleChar", ".");
        xsw.writeAttribute("escape", "!");
        xsw.writeAttribute("matchCase", "false");
        xsw.writeStartElement("PropertyName");
        xsw.writeCharacters(name + "*");
        xsw.writeEndElement();
        xsw.writeStartElement("Literal");
        xsw.writeCharacters("*" + value + "*");
        xsw.writeEndElement();
        xsw.writeEndElement();
    }

    public String createFilter(SearchCriteria sc, WFSSearchChannelsConfiguration config) {
        // override to implement custom filter handling
        String searchStr = sc.getSearchString();
        JSONArray params = config.getParamsForSearch();
        boolean hasMultipleParams = params.length() > 1;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            XMLStreamWriter xsw = XOF.createXMLStreamWriter(baos);
            xsw.writeStartDocument();
            xsw.writeStartElement("Filter");

            if (hasMultipleParams){
                xsw.writeStartElement("Or");
            }

            for(int j = 0; j < params.length(); j++) {
                String param = params.optString(j);
                writePropertyIsLike(xsw, param, searchStr);
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
        StringBuilder buf = new StringBuilder();
        for(SelectItem item : list) {
            buf.append(item.getValue());
            buf.append(separator);
        }
        // drop last separator (', ')
        return buf.substring(0, buf.length()-separator.length());
    }
}
