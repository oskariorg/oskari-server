package fi.nls.oskari.control;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;

/**
 * Example route handling CMS integration.
 * Returns responses based on html/json files or responds with dummy content if not found.
 * @author SMAKINEN
 */
@OskariActionRoute("GetArticlesByTag")
public class GetArticlesByTagHandler extends ActionHandler {

    private Logger log = LogFactory.getLogger(GetArticlesByTagHandler.class);

    private static final String KEY_TAGS = "tags";
    private static final String KEY_ID = "id";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_ARTICLES = "articles";
    private static final String KEY_TITLE = "title";
    private static final String KEY_BODY = "body";

    private static String fileLocation = null;

    @Override
    public void init() {
        super.init();
        fileLocation = PropertyUtil.get("actionhandler.GetArticlesByTag.dir", "/fi/nls/oskari/control");

        if(!fileLocation.endsWith(File.separator)) {
            fileLocation = fileLocation + File.separator;
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final String lang = params.getLocale().getLanguage();
        final String commaSeparatedTags = params.getHttpParam(KEY_TAGS, "");

        log.debug("Getting articles for language:", lang, "with tags:", commaSeparatedTags);
        JSONObject articleContent = getContent(commaSeparatedTags);
        // create dummy content since content files are not provided for these tags
        if(articleContent == null) {
            articleContent = JSONHelper.createJSONObject("static", "[no cms, dummy content]");
            JSONHelper.putValue(articleContent, KEY_TITLE, "[title]");
            JSONHelper.putValue(articleContent, KEY_BODY, "[body from GetArticlesByTag action route with tags: '" + commaSeparatedTags + "']");
        }
        final JSONArray articles = new JSONArray();

        JSONObject articleJson = new JSONObject();
        JSONHelper.putValue(articleJson, KEY_ID, "none");
        JSONHelper.putValue(articleJson, KEY_CONTENT, articleContent);
        articles.put(articleJson);

        final JSONObject response = new JSONObject();
        JSONHelper.putValue(response, KEY_ARTICLES, articles);
        ResponseHelper.writeResponse(params, response);
    }

    protected JSONObject getContent(final String commaSeparatedTags) {

        final String fileName = commaSeparatedTags
                .replace(',', '_')
                .replace(' ', '_')
                .replace('/', '_')
                .replace('.', '_')
                .replace('\\', '_');
        final String htmlContent = readInputFile(fileName + ".html");
        if(htmlContent != null) {
            log.debug("Found HTML-file");
            return JSONHelper.createJSONObject("body", htmlContent);
        }
        final String jsonContent = readInputFile(fileName + ".json");
        if(jsonContent != null) {
            log.debug("Found JSON-file");
            return JSONHelper.createJSONObject(jsonContent);
        }
        return null;
    }

    protected String readInputFile(final String filename) {
        InputStream in = getClass().getResourceAsStream(fileLocation + filename);
        if(in != null) {
            try {
                return IOHelper.readString(in);
            } catch (Exception ignore) {
                log.info("Unable to read file from classpath:", fileLocation + filename);
            }
            finally {
                IOHelper.close(in);
            }
        }
        return null;
    }
}
