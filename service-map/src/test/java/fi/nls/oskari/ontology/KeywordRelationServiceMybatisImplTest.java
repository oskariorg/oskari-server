package fi.nls.oskari.ontology;

import fi.nls.oskari.ontology.domain.Keyword;
import fi.nls.oskari.ontology.domain.Relation;
import fi.nls.oskari.ontology.domain.RelationType;
import fi.nls.oskari.ontology.service.KeywordRelationServiceMybatisImpl;
import fi.nls.oskari.ontology.service.KeywordServiceMybatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.test.util.TestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class KeywordRelationServiceMybatisImplTest {

    private static KeywordRelationServiceMybatisImpl keywordRelationServiceMybatis = null;
    private static Relation testRelation = null;
    private static Long testId1 = null;
    private static Long testId2 = null;
    private static RelationType testRelationType = null;

    @BeforeClass
    public static void init() {
        assumeTrue(TestHelper.dbAvailable());
        Keyword keyword = new Keyword();
        keyword.setEditable(true);
        keyword.setLang("FI");
        keyword.setUri("testUri");
        keyword.setValue("testKeyword");
        KeywordServiceMybatisImpl keywordServiceMybatis = new KeywordServiceMybatisImpl();
        testId1 = keywordServiceMybatis.addKeyword(keyword);
        testId2 = keywordServiceMybatis.addKeyword(keyword);

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
    @AfterClass
    public static void delete() {
        try {
            keywordRelationServiceMybatis.deleteAllRelations();
        }
        catch (Exception e) {
            //TODO error handling
        }
        PropertyUtil.clearProperties();
    }
}