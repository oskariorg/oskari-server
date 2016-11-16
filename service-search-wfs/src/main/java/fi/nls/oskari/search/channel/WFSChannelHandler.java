package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.SelectItem;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.wfs.WFSSearchChannelsConfiguration;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;

import java.util.List;

/**
 * Default handler for WFS Search channel filter and title
 */
@Oskari(WFSChannelHandler.ID)
public class WFSChannelHandler extends OskariComponent {
    public static final String ID = "DEFAULT";

    public String createFilter(SearchCriteria sc, WFSSearchChannelsConfiguration config) {
        // override to implement custom filter handling
        String searchStr = sc.getSearchString();

        StringBuffer filter = new StringBuffer("<Filter>");
        JSONArray params = config.getParamsForSearch();
        boolean hasMultipleParams = params.length()>1;

        if(hasMultipleParams){
            filter.append("<Or>");
        }

        for(int j=0;j<params.length();j++){
            String param = params.optString(j);
            filter.append("<PropertyIsLike wildCard='*' singleChar='.' escape='!' matchCase='false'>" +
                    "<PropertyName>" + StringEscapeUtils.escapeXml(param) + "</PropertyName><Literal>*" +
                    StringEscapeUtils.escapeXml(searchStr) + "*</Literal></PropertyIsLike>"
            );
        }

        if(hasMultipleParams){
            filter.append("</Or>");
        }

        filter.append("</Filter>");
        return filter.toString().trim();
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
