package fi.nls.oskari.control;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Returns dummy response for CMS route
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

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final String lang = params.getLocale().getLanguage();
        final String commaSeparatedTags = params.getHttpParam(KEY_TAGS);
        log.debug("Getting articles for language:", lang, "with tags:", commaSeparatedTags);

        final JSONArray articles = new JSONArray();
        JSONObject articleContent = JSONHelper.createJSONObject("static", "[no cms, dummy content]");
        JSONHelper.putValue(articleContent, KEY_TITLE, "[title]");
        JSONHelper.putValue(articleContent, KEY_BODY, "[body from GetArticlesByTag action route with tags: '" + commaSeparatedTags + "']");
        JSONObject articleJson = new JSONObject();
        JSONHelper.putValue(articleJson, KEY_ID, "none");
        JSONHelper.putValue(articleJson, KEY_CONTENT, articleContent);
        articles.put(articleJson);

        final JSONObject response = new JSONObject();
        JSONHelper.putValue(response, KEY_ARTICLES, articles);
        ResponseHelper.writeResponse(params, response);
    }
}
