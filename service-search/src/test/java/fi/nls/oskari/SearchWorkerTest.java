package fi.nls.oskari;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SearchWorkerTest {

    @Test
    public void testCheckLegalSearch() {
        assertEquals(SearchWorker.ERR_EMPTY, SearchWorker.checkLegalSearch(null));
        assertEquals(SearchWorker.ERR_EMPTY, SearchWorker.checkLegalSearch(""));

        assertEquals(SearchWorker.STR_TRUE, SearchWorker.checkLegalSearch("foobar*"));

        assertEquals(SearchWorker.ERR_TOO_SHORT, SearchWorker.checkLegalSearch("foo"));
        assertEquals(SearchWorker.ERR_TOO_SHORT, SearchWorker.checkLegalSearch("foo*"));
        assertEquals(SearchWorker.ERR_TOO_SHORT, SearchWorker.checkLegalSearch("*foo"));

        assertEquals(SearchWorker.STR_TRUE, SearchWorker.checkLegalSearch("*foo*"));
        assertEquals(SearchWorker.ERR_TOO_WILD, SearchWorker.checkLegalSearch("*f*o*o*"));

        assertEquals(SearchWorker.STR_TRUE, SearchWorker.checkLegalSearch("()()("));
    }

}
