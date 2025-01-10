package fi.nls.oskari;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SearchWorkerTest {

    @Test
    public void testCheckLegalSearch() {
        Assertions.assertEquals(SearchWorker.ERR_EMPTY, SearchWorker.checkLegalSearch(null));
        Assertions.assertEquals(SearchWorker.ERR_EMPTY, SearchWorker.checkLegalSearch(""));

        Assertions.assertEquals(SearchWorker.STR_TRUE, SearchWorker.checkLegalSearch("foo"));
        Assertions.assertEquals(SearchWorker.STR_TRUE, SearchWorker.checkLegalSearch("foobar*"));

        Assertions.assertEquals(SearchWorker.ERR_TOO_SHORT, SearchWorker.checkLegalSearch("foo*"));
        Assertions.assertEquals(SearchWorker.ERR_TOO_SHORT, SearchWorker.checkLegalSearch("*foo"));

        Assertions.assertEquals(SearchWorker.STR_TRUE, SearchWorker.checkLegalSearch("*foo*"));
        Assertions.assertEquals(SearchWorker.ERR_TOO_WILD, SearchWorker.checkLegalSearch("*f*o*o*"));

        Assertions.assertEquals(SearchWorker.STR_TRUE, SearchWorker.checkLegalSearch("()()("));
    }

}
