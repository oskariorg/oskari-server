package org.oskari.csw.request;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class GetRecordsTest {

    @Test
    @Ignore("Doesn't test anything")
    public void getXML() {
        GetRecords moi = new GetRecords();
        moi.addEqualFilter("gmd:title", "helsinki");
        System.out.println(moi.getXML());
    }
}