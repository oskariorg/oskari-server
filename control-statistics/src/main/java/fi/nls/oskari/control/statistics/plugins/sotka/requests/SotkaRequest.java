package fi.nls.oskari.control.statistics.plugins.sotka.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for SotkaNET statistics queries.
 * Actual implementations are defined in subclasses and registered for getInstance with
 * registerAction(SotkaRequestImpl.class).
 * @author SMAKINEN
 */
public class SotkaRequest {

    private static final Logger log = LogFactory.getLogger(SotkaRequest.class);

    private static final List<String> ACCEPTED_VERSIONS = new ArrayList<String>();
    private static final List<String> ACCEPTED_GENDERS = new ArrayList<String>();
    //private static final List<String> INDICATOR_ACTIONS = new ArrayList<String>();

    private static final String SOTKA_ENCODING = "ISO-8859-1";

    private static final String DATA_SEPARATOR = ";";
    private static final String ROW_SEPARATOR = "\r\n";

    private String version = "1.1";
    private String indicator;
    private String[] years = new String[0];
    private String gender;

    private static String sotkaBaseURL;

    private static Map<String, Class> requests = new HashMap<String, Class>();
    static {
        // register possible actions
        registerAction(IndicatorData.class);
        registerAction(IndicatorMetadata.class);
        registerAction(Indicators.class);
        registerAction(Regions.class);

        ACCEPTED_VERSIONS.add("1.1");

        ACCEPTED_GENDERS.add("total");
        ACCEPTED_GENDERS.add("male");
        ACCEPTED_GENDERS.add("female");
        ACCEPTED_GENDERS.add("");
    }

    public SotkaRequest() {
        // "http://www.sotkanet.fi/rest";
        sotkaBaseURL = PropertyUtil.get("sotka.baseurl");

        if(this.getClass().equals(SotkaRequest.class)) {
            throw new RuntimeException("Cannot be instantiated");
        }
    }

    public String getName() {
        return null;
    }

    private static void registerAction(final Class req) {
        try {
            log.debug("Adding req ", req);
            log.debug("reg name: " + getInstance(req).getName());
            requests.put(getInstance(req).getName(), req); // .getClass()
        }
        catch (Exception ex) {
            log.error(ex, "Error adding action! " + req);
        }
    }

    public static SotkaRequest getInstance(final String action) {
    	log.debug("action: " + action);
        Class c = requests.get(action);
        if(c != null) {
        	log.debug("Class name : " + c.getName());
            return getInstance(c);
        }
        throw new RuntimeException("Unregistered action requested:" + action);
    }

    private static SotkaRequest getInstance(final Class req) {
        try {
        	log.debug("Palautetaan SotkaRequest (luokasta otetaan instanssi)");
            return (SotkaRequest) req.newInstance();
        } catch (Exception ignored) { }
        throw new RuntimeException("Unable to craft request instance, shouldn't happen...");
    }

    public String getVersion() {
        return version;
    }

    public String getIndicator() {
        return indicator;
    }

    public void setIndicator(final String indicator) {
        this.indicator = indicator.toLowerCase();
    }

    public String[] getYears() {
        return years;
    }

    public void setYears(String[] years) {
        if(years != null && years.length > 0) {
            this.years = years;
        }
    }

    public String getGender() {
        return gender;
    }

    public void setVersion(final String version) throws ActionException {
        final String lowerCaseVersion = version.toLowerCase();
        if (!ACCEPTED_VERSIONS.contains(lowerCaseVersion)) {
            throw new ActionException("Unknown SOTKAnet version requested "
                    + version);
        }
        this.version = lowerCaseVersion;
    }

    public void setGender(final String gender) throws ActionException {
        final String lowerCaseGender = gender.toLowerCase();
        if (!ACCEPTED_GENDERS.contains(lowerCaseGender)) {
            throw new ActionException("Unknown SOTKAnet gender requested "
                    + gender);
        }
        this.gender = lowerCaseGender;
    }

    public String getUrl() {
        StringWriter writer = new StringWriter();
        writer.write(sotkaBaseURL);
        writer.write("/");
        writer.write(version);
        writer.write(getRequestSpecificParams());
        return writer.toString();
    }

    public String getRequestSpecificParams() {
        return "";
    }

    public boolean isCSV() {
        return false;
    }

    public String getData() throws ActionException {
        HttpURLConnection con = null;
        try {
            final String url = getUrl();
            con = IOHelper.getConnection(url);

            // Response encoding
            //final String enco = IOHelper.getCharset(con, SOTKA_ENCODING);

            final String data = IOHelper.readString(con.getInputStream()); //, SOTKA_ENCODING);
            if (isCSV()) {
                return getJsonFromCSV(data);
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ActionException("Couldn't proxy request SOTKAnet server", e);
        } finally {
            try {
                con.disconnect();
            }
            catch (Exception ignored) {}
        }
    }

    /**
     * This method returning json from csv data getJsonFromCSV(String csv)
     *
     * @param csv intput csv data
     * @return json
     */
    String getJsonFromCSV(final String csv) {

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
        } catch (Exception e) {
            log.warn(e, "Error transforming CSV to JSON", csv);
            return "{error:" + e.toString() + "}";
        }
    }
}
