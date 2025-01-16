package fi.nls.oskari.control.view;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ontology.SearchKeywordsHandler;
import fi.nls.oskari.ontology.domain.Keyword;
import fi.nls.oskari.ontology.service.KeywordService;
import fi.nls.oskari.ontology.service.KeywordServiceMybatisImpl;
import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.control.JSONActionRouteTest;
import fi.nls.test.util.ResourceHelper;
import fi.nls.test.util.TestHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author TMIKKOLAINEN
 */
public class SearchKeywordsHandlerTest extends JSONActionRouteTest {

    final private SearchKeywordsHandler handler = new SearchKeywordsHandler();

    private KeywordService keywordService = null;

    @BeforeAll
    public static void addLocales() throws Exception {
        Properties properties = new Properties();
        try {
            properties.load(SearchKeywordsHandlerTest.class.getResourceAsStream("test.properties"));
            PropertyUtil.addProperties(properties);
            String locales = PropertyUtil.getNecessary("oskari.locales");
            if (locales == null)
                Assertions.fail("No darned locales");
        } catch (DuplicateException e) {
            Assertions.fail("Should not throw exception" + e.getStackTrace());
        }

        Assumptions.assumeTrue(TestHelper.dbAvailable());
    }

    @BeforeEach
    public void setUp() throws Exception {
        keywordService = mock(KeywordServiceMybatisImpl.class);

        handler.setService(keywordService);
        handler.init();
    }

    @AfterAll
    public static void teardown() {
        PropertyUtil.clearProperties();
    }

    private static Keyword createKeyword(Long id, String value, Long... layers) {
        Keyword keyword = new Keyword();
        keyword.setId(id);
        keyword.setValue(value);
        List<Long> layerIds = new ArrayList<Long>();
        for(Long layer : layers) {
            layerIds.add(layer);
        }
        keyword.setLayerIds(layerIds);
        return keyword;
    }

    private static List<Map<String, Object>> getPermissionsList(Long... layerIDs) {
        List<Map<String, Object>> permissions = new ArrayList<Map<String, Object>>();
        for (Long layerID : layerIDs) {
            Map<String, Object> permission = new HashMap<String, Object>();
            permission.put("id", layerID);
            permissions.add(permission);
        }
        return permissions;
    }

    @Test
    public void testEmptyResponse() throws Exception {
        // Empty result should still give us a fake exact hit
        doReturn(null).when(keywordService).findExactKeyword(anyString(), anyString());
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("keyword", "nothing");
        parameters.put("lang", "fi");
        final ActionParameters actionParameters = createActionParams(parameters);
        verifyResponseNotWritten(actionParameters);
        handler.handleAction(actionParameters);
        verifyResponseWritten(actionParameters);
        verifyResponseContent(ResourceHelper.readJSONArrayResource("SearchKeywordsHandlerTest-empty-result.json", this));
    }

    @Test
    public void testNonPermittedLayerResponse() throws Exception {
        // test a response that doesn't have a layer because the user doesn't have permissions for it.
        // mock response from services
        doReturn(createKeyword(1l, "test", 1l, 2l)).when(keywordService).findExactKeyword(anyString(), anyString());
        // do unmocked calls simply return a new<Whatever>?
        // check that the response doesn't have layers
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("keyword", "test");
        parameters.put("lang", "fi");
        final ActionParameters actionParameters = createActionParams(parameters);
        verifyResponseNotWritten(actionParameters);
        handler.handleAction(actionParameters);
        verifyResponseWritten(actionParameters);
        verifyResponseContent(ResourceHelper.readJSONArrayResource("SearchKeywordsHandlerTest-one-layer.json", this));
    }

    @Test
    public void testFullResponse() throws Exception {
        // test a response that carries all possible response/relation types.
        // shows that we get all the relation types and that they are in the right order

        // exact match kw with no layers
        doReturn(createKeyword(1l, "test")).when(keywordService).findExactKeyword(anyString(), anyString());
        // synonym with no layers
        List<Keyword> synonyms = new ArrayList<Keyword>();
        synonyms.add(createKeyword(2l, "trial"));
        doReturn(synonyms).when(keywordService).findSynonyms(anyLong(), anyString());
        // near match with no layers
        List<Keyword> nearMatches = new ArrayList<Keyword>();
        nearMatches.add(createKeyword(3l, "testarossa"));
        doReturn(nearMatches).when(keywordService).findKeywordsMatching(anyString(), anyString());
        // parent with a layer
        List<Keyword> parents = new ArrayList<Keyword>();
        parents.add(createKeyword(4l, "parent", 1l));
        doReturn(parents).when(keywordService).findParents(anyLong(), anyString());
        // sibling with a not-permitted layer
        List<Keyword> siblings = new ArrayList<Keyword>();
        siblings.add(createKeyword(5l, "sibling", 2l));
        doReturn(siblings).when(keywordService).findSiblings(anyLong(), anyString());

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("keyword", "test");
        parameters.put("lang", "fi");
        final ActionParameters actionParameters = createActionParams(parameters);
        verifyResponseNotWritten(actionParameters);
        handler.handleAction(actionParameters);
        verifyResponseWritten(actionParameters);
        verifyResponseContent(ResourceHelper.readJSONArrayResource("SearchKeywordsHandlerTest-all-relation-types.json", this));

    }

    @Test
    public void testNonPermittedLayerDoesNotBlockParentInclusion() throws Exception {
        doReturn(createKeyword(1l, "test", 1l)).when(keywordService).findExactKeyword(anyString(), anyString());
        List<Keyword> parents = new ArrayList<Keyword>();
        parents.add(createKeyword(4l, "parent", 2l));
        doReturn(parents).when(keywordService).findParents(anyLong(), anyString());

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("keyword", "test");
        parameters.put("lang", "fi");
        final ActionParameters actionParameters = createActionParams(parameters);
        verifyResponseNotWritten(actionParameters);
        handler.handleAction(actionParameters);
        verifyResponseWritten(actionParameters);
        verifyResponseContent(ResourceHelper.readJSONArrayResource("SearchKeywordsHandlerTest-non-permitted-layer-on-exact-match.json", this));
    }

    @AfterEach
    public void delete() {
        PropertyUtil.clearProperties();
    }
}
