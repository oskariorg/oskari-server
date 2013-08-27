package fi.nls.oskari.control.data;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.util.ResponseHelper;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

/**
 * Data request of Sotkanet and response to Oskari
 * 
 * - action_route=GetSotkaData + version={1.0|1.1} +
 * action={indicators|indicator_metadata|regions|data}
 * 
 * Extra parameters:
 * - action=indicator_meta + indicator=
 * - action=data + indicator= + years= + years= + gender=
 * 
 * eg.
 * <oskari url>&action_route=GetSotkaData&action=indicators&version=1.1
 * <oskari url>&action_route=GetSotkaData&action=regions&version=1.1
 * <oskari
 * url>&action_route=GetSotkaData&action=indicator_meta&indicator=127&version
 * =1.1
 * <oskari url>&action_route=GetSotkaData&action=data&version=1.1&indicator=127
 * &years=2010&years=2011&gender=female
 * <oskari url>action_route=GetSotkaData&action=data&version=1.0&indicator=127&years=2011&years=2010&genders=female
 * Sotkanet response - only json
 * 
 */
@OskariActionRoute("GetSotkaData")
public class GetSotkaDataHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetSotkaDataHandler.class);

    private static final String PARM_ACTION = "action";
    private static final String PARM_VERSION = "version";
    private static final String PARM_INDICATOR = "indicator";
    private static final String PARM_YEARS = "years"; // many
    private static final String PARM_GENDERS = "genders"; // total | male | female
    private static final String PARM_SOTKA_URL_PART = "sotka_url_part";

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

    private static final String OSKARI_ENCODING = "UTF-8";
    private static final String SOTKA_ENCODING = "ISO-8859-1";

    private static final List<String> ACCEPTED_ACTIONS = new ArrayList<String>();
    private static final List<String> ACCEPTED_VERSIONS = new ArrayList<String>();
    private static final List<String> ACCEPTED_GENDERS = new ArrayList<String>();
    private static final List<String> INDICATOR_ACTIONS = new ArrayList<String>();

//    private static final String VALUE_ACTION_DATA = "data";
    
    private static final String VALUE_CONTENT_TYPE_CSV = "text/csv";
    private static final String HEADER_CONTENT_DISPOSITION_INLINE = "inline";
    private static final String HEADER_CONTENT_DISPOSITION_CSV_SUFIX = "csv";
    private static final String VALUE_CONTENT_TYPE_JSON = "application/json";
    
    private static final String DATA_SEPARATOR = ";";
    private static final String ROW_SEPARATOR = "\r\n";
    
    private static String sotkaBaseURL;

    public void init() {
        // "http://www.sotkanet.fi/rest";
        sotkaBaseURL = PropertyUtil.get("sotka.baseurl");

        ACCEPTED_VERSIONS.add("1.0");
        ACCEPTED_VERSIONS.add("1.1");

        ACCEPTED_ACTIONS.add("indicators");
        ACCEPTED_ACTIONS.add("indicator_metadata");
        ACCEPTED_ACTIONS.add("regions");
        ACCEPTED_ACTIONS.add("data");
        ACCEPTED_ACTIONS.add("sotka_request");

        // actions which must have INDICATOR param
        INDICATOR_ACTIONS.add("indicator_metadata");
        INDICATOR_ACTIONS.add("data");

        ACCEPTED_GENDERS.add("total");
        ACCEPTED_GENDERS.add("male");
        ACCEPTED_GENDERS.add("female");
        ACCEPTED_GENDERS.add("");
    }

    public void handleAction(final ActionParameters params) throws ActionException {

        final HttpServletResponse response = params.getResponse();
//        final HttpServletRequest httpRequest = params.getRequest();

        final HttpURLConnection con = getConnection(params);

        try {
            // Only Get method
            boolean isCSV = handleHeaders(con, response);
            
            // Response encoding
            final String enco = getCharset(con, SOTKA_ENCODING);
            response.setCharacterEncoding(OSKARI_ENCODING);
            
            String json = "";
            
            if (enco.toUpperCase().equals(SOTKA_ENCODING)) {
                json = IOHelper.readString(con.getInputStream(), SOTKA_ENCODING);
            } else {
                json = IOHelper.readString(con.getInputStream());
            }
            
            
            if (isCSV) {
                json = getJsonFromCSV(json);
            }

            ResponseHelper.writeResponse(params, json);
            
        } catch (Exception e) {
            throw new ActionException("Couldn't proxy request Sotkanet server",
                    e);
        } finally {
            con.disconnect();
        }
    }
    
    /** This method returning json from csv data getJsonFromCSV(String csv)
     * 
     * @param csv intput csv data
     * @return json  
     */
    
    private String getJsonFromCSV(final String csv) {
        
        final ArrayList<Map<String, String>> al = new ArrayList<Map<String, String>>();
        
        final String[] rows = csv.split(ROW_SEPARATOR);
        
        final String[] headers = rows[0].split(DATA_SEPARATOR);
        
        for (int i = 1; i < rows.length; i++) {
            final Map<String, String> rowMap = new HashMap<String, String>();
            String[] datas = rows[i].split(DATA_SEPARATOR);
            for(int j = 0; j < datas.length; j++) {
                if (headers.length > j) {
                    rowMap.put(headers[j], datas[j]);
                }
            }
            al.add(rowMap);
        }
                
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(al);
        } catch (JsonGenerationException e) {
            log.error("Error when trying do JSON from objecti to String", e);
            return "{error:" + e.toString() + "}"; 
        } catch (JsonMappingException e) {
            log.error("Error when trying do JSON from objecti to String", e);
            return "{error:" + e.toString() + "}";
        } catch (IOException e) {
            log.error("Error when trying do JSON from objecti to String", e);
            return "{error:" + e.toString() + "}";
        }
    }
    

    private HttpURLConnection getConnection(final ActionParameters params)
            throws ActionException {

        try {
            // Sotkanet action must be in params
            final String action = getAction(params
                    .getHttpParam(PARM_ACTION, ""));

            // If action=sotka_url_part, then only PARM_SOTKA_URL_PART is in use
            final String sotka_url_part = params.getHttpParam(
                    PARM_SOTKA_URL_PART, "");

            final String version = getVersion(params.getHttpParam(PARM_VERSION,
                    "1.1"));
            final String gender = getGender(params.getHttpParam(PARM_GENDERS,
                    ""));
            final String indicator = getIndicator(params, action);
            final List<String> years = getYears(params);

            return IOHelper.getConnection(getSotkaUrl(action, version, gender,
                    indicator, sotka_url_part, years));
        } catch (Exception e) {
            throw new ActionException(
                    "Couldnt get connection to Sotkanet service", e);
        }
    }


    private boolean handleHeaders(final HttpURLConnection con,
            final HttpServletResponse response) {
        final Set<String> headers = con.getHeaderFields().keySet();
        boolean isCSV = false;
        for (String key : headers) {
            try {
                String value = con.getHeaderField(key);
                // only copy content-type header for now
                // problems with other headers f. ex. Transfer-encoding
                // "chunked" etc
                // so maybe just copy the ones we need
                if (value != null
                        && key != null
                        && (HEADER_CONTENT_TYPE.equals(key) || HEADER_CONTENT_DISPOSITION
                                .equals(key))) {
                    
                    if (HEADER_CONTENT_TYPE.equals(key) && !value.startsWith(VALUE_CONTENT_TYPE_CSV)) {
                        response.addHeader(key, value);
                    } else if (HEADER_CONTENT_DISPOSITION.equals(key) && value.endsWith(HEADER_CONTENT_DISPOSITION_CSV_SUFIX)) {
                        response.addHeader(key, HEADER_CONTENT_DISPOSITION_INLINE);
                        isCSV = true;
                    } else {
                        response.addHeader(key, VALUE_CONTENT_TYPE_JSON);
                    }
                    
                    
                }
            } catch (Exception e) {
                log.error(e, "Error copying headers");
            }
        }
        return isCSV;
    }

    private String getAction(final String action) throws ActionException {
        final String lowerCaseAction = action.toLowerCase();
        if (!ACCEPTED_ACTIONS.contains(lowerCaseAction)) {
            throw new ActionException("Unknown Sotkanet action requested "
                    + action);
        }
        return lowerCaseAction;
    }

    private String getVersion(final String version) throws ActionException {
        final String lowerCaseVersion = version.toLowerCase();
        if (!ACCEPTED_VERSIONS.contains(lowerCaseVersion)) {
            throw new ActionException("Unknown Sotkanet version requested "
                    + version);
        }
        return lowerCaseVersion;
    }

    private String getGender(final String gender) throws ActionException {
        final String lowerCaseGender = gender.toLowerCase();
        if (!ACCEPTED_GENDERS.contains(lowerCaseGender)) {
            throw new ActionException("Unknown Sotkanet gender requested "
                    + gender);
        }
        return lowerCaseGender;
    }

    private String getIndicator(final ActionParameters params, final String action)
            throws ActionException {
        final String indicator = params.getHttpParam(PARM_INDICATOR, "");
        if (indicator.isEmpty() == true & INDICATOR_ACTIONS.contains(action)) {
            throw new ActionException(
                    "Indicator param must be in request with action: " + action);
        }
        return indicator;
    }

    private List<String> getYears(final ActionParameters params) {
        final List<String> years = new ArrayList<String>();
        final String year = params.getHttpParam(PARM_YEARS, "");
        Map<String, String[]> map = params.getRequest().getParameterMap();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            if (PARM_YEARS.equals(entry.getKey())) {
                for (String valu : entry.getValue()) {
                    years.add(valu);
                }
            }
        }
        if (years.size() == 0)
            years.add(year);
        return years;
    }

    private String getSotkaUrl(final String action, final String version,
            final String gender, final String indicator,
            final String sotka_url_part, List<String> years)
            throws ActionException {
        String myUrl = "";
        if (action.equals("data")) {
            myUrl = sotkaBaseURL + "/" + version + "/" + "data/csv?"
                    + "indicator=" + indicator;
            for (String year : years) {
                if (!year.isEmpty())
                    myUrl = myUrl + "&years=" + year;
            }
            if (!gender.isEmpty())
                myUrl = myUrl + "&genders=" + gender;
        } else if (action.equals("indicator_metadata")) {
            myUrl = sotkaBaseURL + "/" + version + "/" + "indicators/"
                    + indicator;
        } else if (action.equals("indicators")) {
            myUrl = sotkaBaseURL + "/" + version + "/" + "indicators";
        } else if (action.equals("regions")) {
            myUrl = sotkaBaseURL + "/" + version + "/" + "regions";
        } else if (action.equals("sotka_request")) {
            myUrl = sotkaBaseURL + "/" + sotka_url_part;
            if (sotka_url_part.isEmpty()) {
                throw new ActionException(
                        "Sotka_url_part param is undefined -action: " + action);
            }
        }

        return myUrl;
    }

    private String getCharset(final HttpURLConnection con, final String defaultCharset) {
        final String contentType = con.getContentType();
        final String[] values = contentType.split(";");

        for (String value : values) {
            value = value.trim();

            if (value.toLowerCase().startsWith("charset=")) {
                return value.substring("charset=".length());
            }
        }
        return defaultCharset;
    }

}
