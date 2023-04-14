package org.oskari.statistics.user;

import java.time.OffsetDateTime;

public class UserIndicatorDataRow {

    long id;
    long userId;
    String title = "";
    String source = "";
    String description = "";

    long regionsetId;
    int year;
    String data;

    boolean published;
    OffsetDateTime created;
    OffsetDateTime updated;
}
