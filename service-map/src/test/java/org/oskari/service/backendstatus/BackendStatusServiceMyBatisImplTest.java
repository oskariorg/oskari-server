package org.oskari.service.backendstatus;

import fi.nls.oskari.domain.map.BackendStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import java.util.Arrays;
import java.util.List;

public class BackendStatusServiceMyBatisImplTest {

    @Test
    @Disabled("Requires a database connection, truncates table")
    public void testInsertingWorks() {
        String url = System.getProperty("oskari.test.db.url");
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(url);

        BackendStatus foo = new BackendStatus(1, "OK", "foo", "https://fake.uri");
        BackendStatus bar = new BackendStatus(2, "ERROR", "bar", "https://not.so.fake");
        BackendStatus baz = new BackendStatus(3, "OK", "baz", "http://maybe.not.https");

        List<BackendStatus> statuses = Arrays.asList(foo, bar, baz);

        BackendStatusServiceMyBatisImpl bs = new BackendStatusServiceMyBatisImpl(ds);

        bs.insertAll(statuses);

        List<BackendStatus> found = bs.findAll();
        Assertions.assertNotNull(found);
        Assertions.assertEquals(3, found.size());
        for (int i = 0; i < 3; i++) {
            // Should be ordered by id
            BackendStatus expect = statuses.get(i);
            BackendStatus actual = found.get(i);
            validate(expect, actual);
        }

        List<BackendStatus> alerts = bs.findAllWithAlert();
        Assertions.assertNotNull(alerts);
        Assertions.assertEquals(1, alerts.size());
        validate(bar, alerts.get(0)); // bar is the one with ERROR
    }

    private void validate(BackendStatus expect, BackendStatus actual) {
        Assertions.assertEquals(expect.getMapLayerId(), actual.getMapLayerId());
        Assertions.assertEquals(expect.getStatus(), actual.getStatus());
        Assertions.assertEquals(expect.getStatusMessage(), actual.getStatusMessage());
        Assertions.assertEquals(expect.getInfoUrl(), actual.getInfoUrl());
        Assertions.assertNotNull(actual.getTimestamp());
    }

}
