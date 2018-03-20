package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.IllegalSearchCriteriaException;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.util.QueryParser;
import fi.nls.oskari.search.util.SearchUtil;
import fi.nls.oskari.search.util.StreetNameComparator;
import fi.nls.oskari.search.util.WFSOsoitenimiFilterMaker;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

public abstract class BaseWfsAddressChannelSearchService extends SearchChannel {
    private Logger log = LogFactory.getLogger(this.getClass());
        
    private static final String BOUNDED_BY              = "gml:boundedBy";
    private static final String OSOITE_NIMI     = "oso:Osoitenimi";
    private static final String GML_ID                  = "gml:id";
    private static final String SIJAINTI                = "oso:sijainti";
    private static final String KATUNIMI                = "oso:katunimi";
    private static final String KATUNUMERO              = "oso:katunumero";
    private static final String KUNTANIMI   = "oso:kuntanimi";

    private static String getUcIso3LangString(String locale) {
        String lang = locale.split("_")[0];
        Locale loc = new Locale(lang);
        String iso3Lang = loc.getISO3Language();
        return Character.toUpperCase(iso3Lang.charAt(0)) + iso3Lang.substring(1);
    }

    public boolean isValidSearchTerm(SearchCriteria criteria) {
        QueryParser parser = new QueryParser(criteria.getSearchString());
        try {
            parser.parse();
            return true;
        } catch (IllegalSearchCriteriaException e) {
            return false;
        }
    }
        
    public ChannelSearchResult doSearch(SearchCriteria searchCriteria) {
        ChannelSearchResult searchResultList = new ChannelSearchResult();
        String queryUrl = null;
        try {
                        
            QueryParser queryParser = createQueryParser(searchCriteria.getSearchString());
            queryParser.parse();
                        
            WFSOsoitenimiFilterMaker wfsoFM = new WFSOsoitenimiFilterMaker(queryParser);
            String filterXml = URLEncoder.encode(wfsoFM.getFilter(), "UTF-8");
            queryUrl = this.getQueryUrl(filterXml, getMaxResults(searchCriteria.getMaxResults()));
            if(queryUrl == null) {
                return null;
            }
            URLConnection conn = getConnection(queryUrl);
            InputStream ins = conn.getInputStream();

            DocumentBuilderFactory dbf = XmlHelper.newDocumentBuilderFactory();

            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            Document dom = db.parse(ins);
                        
            NodeList addressList  = dom.getElementsByTagName(OSOITE_NIMI);
                        
                        
            for (int i = 0; i < addressList.getLength(); i++) {
                Node currentNode = addressList.item(i);
                                
                String id=currentNode.getAttributes().getNamedItem(GML_ID).getNodeValue();
                                
                Map<String, String> values = new HashMap<String,String>();
                NodeList childList  = currentNode.getChildNodes();
                                
                String lon = "";
                String lat = "";
                                
                for (int ii = 0; ii < childList.getLength(); ii++) {
                    String nodeName = childList.item(ii).getNodeName().toString();
                                        
                    if (SIJAINTI.equals(nodeName)) {
                                                
                        Node gmlPoint = childList.item(ii).getChildNodes().item(0);
                                                
                        Node node = gmlPoint.getLastChild();
                        String[] lonlat = node.getFirstChild().getNodeValue().split("\\s");
                        lon = lonlat[0];
                        lat = lonlat[1];
                                                
                    } else if (!BOUNDED_BY.equals(nodeName)) {
                        String nodeValue = childList.item(ii).getFirstChild().getNodeValue().toString();
                        values.put(nodeName, nodeValue);
                    } 
                }
                                
                String addressName=values.get(KATUNIMI);
                                
                if (values.get(KATUNUMERO)!= null) {
                    addressName += " " + values.get(KATUNUMERO);
                }

                // get village name in default locale
                final String village = parseVillage(values, queryParser.getVillageName(), searchCriteria.getLocale());

                String language = searchCriteria.getLocale();
                String languageCode=SearchUtil.getLocaleCode(language);
                                
                SearchResultItem item = new SearchResultItem();
                item.setRank(SearchUtil.RANK_OTHER);
                item.setTitle(addressName);
                item.setContentURL(lon + "_" + lat);

                String type = getType();
                item.setType(SearchUtil.getLocationType(type + "_" + languageCode));
                item.setActionURL(type);
                                
                item.setLon(String.valueOf(lon));
                item.setLat(String.valueOf(lat));

                                
                item.setDescription("Id=" + id+ ", Kieli=" 
                                    + language + ", Kielikoodi=" + languageCode 
                                    + ", Kunta=" + village);
                item.setMapURL(getMapUrl(searchCriteria));
                item.setRegion(village);
                searchResultList.addItem(item);
                                
            }
                        
            this.filterResultsAfterQuery(searchResultList);
            List<SearchResultItem> sr = searchResultList.getSearchResultItems();
                        
            Collections.sort(sr, new StreetNameComparator());
                        
            searchResultList.setSearchResultItems(sr);
                        
            return searchResultList;
                                                
        }
        catch (IllegalSearchCriteriaException e) {
            searchResultList.setException(e);
            searchResultList.setQueryFailed(true);
            return searchResultList;
        }
        catch (IOException e) {
            log.warn("Error connecting to service/reading response:", e.getMessage(), "- URL:", queryUrl);
            searchResultList.setException(e);
            searchResultList.setQueryFailed(true);
            return searchResultList;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to search address from '" + this.getClass().getName() + "'", e);
        }
    }

    /**
     * Parses village info from values with sensible default value if not found
     * @param values from xml
     * @param queriedVillage what the user requested
     * @param queryLocale user locale
     * @return
     */
    private String parseVillage(final Map<String, String> values, final String queriedVillage, final String queryLocale) {

        final String[] supportedLocales = PropertyUtil.getSupportedLocales();
        // TODO: check if this logic makes sense
        // fallback to default locale if loop doesn't find a better match
        String localizedValue = values.get(KUNTANIMI + getUcIso3LangString(PropertyUtil.getDefaultLocale()));
        for (int j = 1; j < supportedLocales.length; j++) {
            final String lang = getUcIso3LangString(supportedLocales[j]);
            final String value = values.get(KUNTANIMI + lang);
            if (value == null) {
                // no value for lang -> skip to next round
                continue;
            }
            if (queriedVillage != null && queriedVillage.equalsIgnoreCase(value)) {
                // if matching queried village -> return straight away
                return value;
            }
            if (supportedLocales[j].split("_")[0].equals(queryLocale)) {
                // if matching queried locale -> set as possible return value
                localizedValue = value;
            }
        }
        if(localizedValue == null) {
            // just making sure
            localizedValue = "";
        }
        return localizedValue;
    }
        
    protected QueryParser createQueryParser(String searchCriteriaString) {
        QueryParser queryParser = new QueryParser(searchCriteriaString);
        return queryParser;
    }
        
    /**
     * Returns map url in portal
     * 
     * @param sc
     * @return
     */
    protected String getMapUrl(SearchCriteria sc) throws Exception {
        return SearchUtil.getMapURL(sc.getLocale());
    }
        
    /**
     * Methods must return url where to send the query
     */
    protected abstract String getQueryUrl(String filter, int maxResults);
        
    protected abstract String getType();
        
    /**
     * This method will be called after query has been made.
     * Channel has ability to do some filtering for results.
     * 
     * @return ChannelSearchResult
     */
    protected abstract ChannelSearchResult filterResultsAfterQuery(ChannelSearchResult csr);
}
