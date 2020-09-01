package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.SelectItem;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.OskariRuntimeException;
import fi.nls.oskari.wfs.WFSSearchChannelsConfiguration;
import org.json.JSONArray;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Simple address parser handling/filter creation.
 * Assumes users know to select first parameter as streetname and second for street number
 */
@Oskari("SimpleAddress")
public class SimpleAddressWFSSearchHandler extends WFSChannelHandler {
    private Logger log = LogFactory.getLogger(this.getClass());

    public String createFilter(SearchCriteria sc, WFSSearchChannelsConfiguration config) {
        // custom filter handling
        String searchStr = sc.getSearchString();
        JSONArray params = config.getParamsForSearch();
        if(params.length() < 2) {
            // default to simple handler if less than 2 params or all numbers
            log.warn("Tried to use address handler, but it needs 2 parameters. Using default handler instead.");
            return super.createFilter(sc, config);
        }

        String streetName = searchStr;
        String streetNumber = "";
        // find last word and if it is number then it must be street number?
        String lastWord = searchStr.substring(searchStr.lastIndexOf(" ") + 1);

        if (isStreetNumber(lastWord)) {
            // override streetName without, street number
            streetName = searchStr.substring(0, searchStr.lastIndexOf(" "));
            streetNumber = lastWord;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            XMLStreamWriter xsw = XOF.createXMLStreamWriter(baos);
            xsw.writeStartDocument();
            xsw.writeStartElement("Filter");
            xsw.writeStartElement("And");

            writePropertyIsLike(xsw, params.optString(0), streetName);
            writePropertyIsLike(xsw, params.optString(1), streetNumber);

            // /And
            xsw.writeEndElement();
            // /Filter
            xsw.writeEndElement();
            xsw.writeEndDocument();
            xsw.close();

        } catch (XMLStreamException e) {
            throw new OskariRuntimeException("Unable to write filter", e);
        }
        return baos.toString();
    }

    /**
     * Returns the true if test contains numbers and/or a/b.
     *
     * @param test Search criteria.
     * @return true if string can be set to street number field in wfs query.
     */
    private boolean isStreetNumber(String test) {
        log.debug("street number candidate: " + test);
        return test.matches("[0-9-a-b]+");
    }

    public String getTitle(List<SelectItem> list) {
        return getTitle(list, " ");
    }
}
