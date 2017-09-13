package org.oskari.service.backendstatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;

import fi.nls.oskari.domain.map.BackendStatus;

public class BackendStatusServiceMyBatisImplTest {

    @Test
    @Ignore("Requires a database connection, truncates table")
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
        assertNotNull(found);
        assertEquals(3, found.size());
        for (int i = 0; i < 3; i++) {
            // Should be ordered by id
            BackendStatus expect = statuses.get(i);
            BackendStatus actual = found.get(i);
            validate(expect, actual);
        }

        List<BackendStatus> alerts = bs.findAllWithAlert();
        assertNotNull(alerts);
        assertEquals(1, alerts.size());
        validate(bar, alerts.get(0)); // bar is the one with ERROR
    }

    private void validate(BackendStatus expect, BackendStatus actual) {
        assertEquals(expect.getMapLayerId(), actual.getMapLayerId());
        assertEquals(expect.getStatus(), actual.getStatus());
        assertEquals(expect.getStatusMessage(), actual.getStatusMessage());
        assertEquals(expect.getInfoUrl(), actual.getInfoUrl());
        assertNotNull(actual.getTimestamp());
    }

}
