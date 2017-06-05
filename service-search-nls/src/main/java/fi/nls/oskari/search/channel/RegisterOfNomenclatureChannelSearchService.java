package fi.nls.oskari.search.channel;

import fi.mml.nameregister.FeatureCollectionDocument;
import fi.mml.nameregister.FeaturePropertyType;
import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.mml.portti.service.search.SearchService;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.util.QueryParser;
import fi.nls.oskari.search.util.SearchUtil;
import fi.nls.oskari.search.util.VillageSearchUtil;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.xmlbeans.XmlObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.net.URLEncoder;

@Oskari(RegisterOfNomenclatureChannelSearchService.ID)
public class RegisterOfNomenclatureChannelSearchService extends SearchChannel {
    /** logger */
    private Logger log = LogFactory.getLogger(this.getClass());

    public static final String ID = "REGISTER_OF_NOMENCLATURE_CHANNEL";
    private static final String PROPERTY_SERVICE_URL = "search.channel.REGISTER_OF_NOMENCLATURE_CHANNEL.service.url";
    private static final String STR_UNDERSCORE = "_";

    private String serviceURL = null;

    @Override
    public void init() {
        super.init();
        serviceURL = PropertyUtil.getOptional(PROPERTY_SERVICE_URL);
        log.debug("ServiceURL set to " + serviceURL);
    }

    private String getLocaleCode(String locale) {
        final String currentLocaleCode =  SearchUtil.getLocaleCode(locale);
        if("eng".equals(currentLocaleCode)) {
            return "fin";
        }
        return currentLocaleCode;
    }

    public ChannelSearchResult doSearch(SearchCriteria searchCriteria) {
        if(serviceURL == null) {
            log.warn("ServiceURL not configured. Add property with key", PROPERTY_SERVICE_URL);
            return null;
        }
    	
    	boolean villageFound = false;
    	String villageName = null;
    	
        ChannelSearchResult searchResultList = new ChannelSearchResult();
        
        String searchString = searchCriteria.getSearchString();
        
        QueryParser queryParser = new QueryParser(searchString);
        try{
	        queryParser.parse();
	        log.debug(queryParser.toString());
        
	        if(queryParser.getVillageName() != null && queryParser.getStreetName() != null && !queryParser.getStreetName().equals("")){
        		villageName = queryParser.getVillageName();
    	        searchString = queryParser.getStreetName();
    	        villageFound = VillageSearchUtil.isVillage(villageName);
	        }
        
	    } catch(Exception e){
	    	log.warn("Address parser failed");
	    }
        

        try {
        	final String url = getWFSUrl(searchString, getMaxResults(searchCriteria.getMaxResults()));
            final String data = IOHelper.readString(getConnection(url));
            
            final String currentLocaleCode =  getLocaleCode(searchCriteria.getLocale());
            final FeatureCollectionDocument fDoc =  FeatureCollectionDocument.Factory.parse(data);
            final FeaturePropertyType[] fMembersArray = fDoc.getFeatureCollection().getFeatureMemberArray();

            for (FeaturePropertyType  fpt : fMembersArray) {

                XmlObject[] paikka = fpt.selectChildren(SearchUtil.pnrPaikka);
                XmlObject[] paikkaSijainti = paikka[0].selectChildren(SearchUtil.pnrPaikkaSijaintiName);
                XmlObject[] point = paikkaSijainti[0].selectChildren(SearchUtil.gmlPoint);
                XmlObject[] pos = point[0].selectChildren(SearchUtil.gmlPos);

                XmlObject[] nimi = paikka[0].selectChildren(SearchUtil.pnrNimi);
                XmlObject[] paikanNimi = nimi[0].selectChildren(SearchUtil.pnrPaikanNimi);

                XmlObject[] kirjoitusAsu = paikanNimi[0].selectChildren(SearchUtil.pnrKirjoitusAsu);
                XmlObject[] kieliKoodi = paikanNimi[0].selectChildren(SearchUtil.pnrkieliKoodi);

                XmlObject[] paikanNimi2;
                XmlObject[] kirjoitusAsu2;
                XmlObject[] kieliKoodi2;

                String paikanKirjoitusasu = kirjoitusAsu[0].newCursor().getTextValue();
                String kieli = kieliKoodi[0].newCursor().getTextValue();

                if (nimi.length > 1 && !currentLocaleCode.equals(kieli)) {
                    // FIXME: loop around some values, break if we got the correct language, otherwise populate
                    // variables with "random" values? seems legit
                    for (int i = 1; i < nimi.length; i++) {
                        paikanNimi2 = nimi[i].selectChildren(SearchUtil.pnrPaikanNimi);
                        kirjoitusAsu2 = paikanNimi2[0].selectChildren(SearchUtil.pnrKirjoitusAsu);
                        kieliKoodi2 = paikanNimi2[0].selectChildren(SearchUtil.pnrkieliKoodi);

                        paikanKirjoitusasu = kirjoitusAsu2[0].newCursor().getTextValue();
                        kieli = kieliKoodi2[0].newCursor().getTextValue();

                        if (currentLocaleCode.equals(kieli)) {
                            break;
                        }
                    }
                }

                String kuntaKoodi = paikka[0].selectChildren(SearchUtil.pnrkuntaKoodi)[0].newCursor().getTextValue();
                String paikkatyyppiKoodi = paikka[0].selectChildren(SearchUtil.pnrPaikkatyyppiKoodi)[0].newCursor().getTextValue();

                String sijainti = pos[0].newCursor().getTextValue();

                String[] lonLat = sijainti.split("\\s");

                SearchResultItem item = new SearchResultItem();
                item.setRank(SearchUtil.getRank(paikkatyyppiKoodi));
                item.setTitle(paikanKirjoitusasu);
                item.setContentURL(lonLat[0] + "_" + lonLat[1]);
                item.setDescription(kieli);
                item.setActionURL(paikkatyyppiKoodi);
                item.setLocationTypeCode(paikkatyyppiKoodi);
                item.setType(getType(searchCriteria.getLocale(), paikkatyyppiKoodi));
                item.setLocationName(SearchUtil.getLocationType(paikkatyyppiKoodi+"_"+ currentLocaleCode));
                log.debug("kuntaKoodi _ currentLocaleCode " + kuntaKoodi+"_"+ currentLocaleCode);
                item.setRegion(VillageSearchUtil.getVillageName(kuntaKoodi+"_"+ currentLocaleCode));
                log.debug("item.getVillage: " + item.getRegion());
                item.setLon(lonLat[0]);
                item.setLat(lonLat[1]);
                item.setMapURL(SearchUtil.getMapURL(searchCriteria.getLocale()));
                if(villageFound){
                	//log.debug("verrataan: " + villageName + "-" + SearchUtil.getVillageName(kuntaKoodi+"_"+ currentLocaleCode));
                	if(villageName.equals(VillageSearchUtil.getVillageName(kuntaKoodi+"_"+ currentLocaleCode))){
                        searchResultList.addItem(item);
                	}
                }else{
                	searchResultList.addItem(item);
                }

            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to search Locations from register of nomenclature", e);
        }

        return searchResultList;
    }

    @Override
    public void calculateCommonFields(final SearchResultItem item) {
        super.calculateCommonFields(item);
        // override basic zoomscale and configure by locationTypeCode instead of type.
        item.setZoomScale(getZoomScale(item.getLocationTypeCode()));
        // item.setZoomLevel(SearchUtil.getZoomLevel(item.getLocationTypeCode()));
    }

    private String getType(final String language, final String paikkatyyppiKoodi) {
        // Type
        String localeCode = SearchUtil.getLocaleCode(language);
        String locationUrl = paikkatyyppiKoodi + STR_UNDERSCORE + localeCode;
        String type = ConversionHelper.getString(SearchUtil.getLocationType(locationUrl), "");
        return Jsoup.clean(type, Whitelist.none());
    }

    /**
     * Returns the searchcriterial String. 
     *
     * @param filter
     * @return url with url-encoded filter
     * @throws Exception
     */
    private String getWFSUrl(String filter, int maxResults) throws Exception {


        String filterXml = "<Filter>" +
                        "   <PropertyIsLike wildCard='*' matchCase='false' singleChar='?' escapeChar='!'>" +
                        "       <PropertyName>pnr:nimi/pnr:PaikanNimi/pnr:kirjoitusasu</PropertyName>" +
                        "       <Literal>" + filter + "</Literal>" +
                        "   </PropertyIsLike>" +
                        "</Filter>";
        filterXml = URLEncoder.encode(filterXml, "UTF-8");

        String wfsUrl = serviceURL +
                "?SERVICE=WFS&VERSION=1.1.0" +
                "&maxFeatures=" +  (maxResults + 1) +  // added 1 to maxCount because need to know if there are more then maxCount
                "&REQUEST=GetFeature&TYPENAME=pnr:Paikka" +
                "&NAMESPACE=xmlns%28pnr=http://xml.nls.fi/Nimisto/Nimistorekisteri/2009/02%29" +
                "&filter=" + filterXml + "&SortBy=pnr:mittakaavarelevanssiKoodi+D";

        return wfsUrl;
    }
}
