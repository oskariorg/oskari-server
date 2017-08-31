package fi.nls.oskari.ontology;

import fi.nls.oskari.ontology.domain.Keyword;
import fi.nls.oskari.ontology.domain.Relation;
import fi.nls.oskari.ontology.domain.RelationType;
import fi.nls.oskari.ontology.service.KeywordRelationServiceMybatisImpl;
import fi.nls.oskari.ontology.service.KeywordServiceMybatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.TestHelper;
import org.junit.*;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@Ignore
public class KeywordRelationServiceMybatisImplTest {

    private static KeywordRelationServiceMybatisImpl keywordRelationServiceMybatis = null;
    private static KeywordServiceMybatisImpl keywordServiceMybatis = null;
    private static Relation testRelation = null;
    private static Long testId1 = null;
    private static Long testId2 = null;
    private static RelationType testRelationType = null;

    @BeforeClass
    public static void init() {
        assumeTrue(TestHelper.dbAvailable());
    }

    @Before
    public void setUp() {
        Keyword keyword = new Keyword();
        keyword.setEditable(true);
        keyword.setLang("FI");
        keyword.setUri("testUri");
        keyword.setValue("testKeyword");
        keywordServiceMybatis = new KeywordServiceMybatisImpl();
        testId1 = keywordServiceMybatis.addKeyword(keyword);
        Keyword keyword2 = new Keyword();
        keyword2.setLang("SV");
        keyword2.setUri("testUri2");
        keyword2.setValue("testKeyword2");
        testId2 = keywordServiceMybatis.addKeyword(keyword2);

        keywordRelationServiceMybatis = new KeywordRelationServiceMybatisImpl();
        testRelationType = RelationType.AK;
        testRelation = new Relation();
        testRelation.setKeyid1(testId1);
        testRelation.setKeyid2(testId2);
        testRelation.setRelationType(testRelationType);
    }

    @Test
    public void testAddAndGetRelation() {
        keywordRelationServiceMybatis.addRelation(testRelation);
        Relation relation = keywordRelationServiceMybatis.getRelation(testRelation);

        assertTrue("Add and get relation", relation.getKeyid1().equals(testRelation.getKeyid1()));
    }

    @Test
    public void testGetRelations() {
        keywordRelationServiceMybatis.addRelation(testRelation);
        List<Relation> relationList = keywordRelationServiceMybatis.getRelationsForKeyword(testRelation.getKeyid1());

        assertTrue("Add and get relation list", relationList.get(0).getKeyid1().equals(testRelation.getKeyid1()));
    }

    @Test
    public void test() {
        keywordRelationServiceMybatis.addRelation(testRelation);
        List<Relation> relationList = keywordRelationServiceMybatis.getRelationsByTypeForKeyword(testRelation);

        assertTrue("Add and get relation by type", relationList.get(0).getKeyid1().equals(testRelation.getKeyid1()));
    }

    @After
    public void delete() {
        try {
            keywordRelationServiceMybatis.deleteAllRelations();
        } catch (Exception e) {
        }
    }

    @AfterClass
    public static void tearDown() {
        PropertyUtil.clearProperties();
    }
}