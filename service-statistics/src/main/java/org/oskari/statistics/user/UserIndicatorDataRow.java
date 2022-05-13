package org.oskari.statistics.user;

import java.util.Date;

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
    Date created;
}
