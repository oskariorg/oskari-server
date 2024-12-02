package org.oskari.capabilities;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TimeDimensionParser {

    public static List<ZonedDateTime> parseTimeDimension(String timeDimension) {
        if (timeDimension == null || timeDimension.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = timeDimension.split("/");
        if (parts.length != 3) {
            return Collections.emptyList();
        }

        TemporalAmount period = getTemporalAmount(parts[2]);
        return getZonedDateTimes(parts[0], parts[1], period);
    }

    private static TemporalAmount getTemporalAmount(String intervalStr) {
        try {
            if (intervalStr.contains("T")) {
                // time can in the start PT3H or in the middle P1DT5H
                // and needs to be parsed with Duration instead of Period for some reason.
                return Duration.parse(intervalStr);
            } else {
                return Period.parse(intervalStr);
            }
        }  catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Unsupported interval format: " + intervalStr);
        }
    }

    private static List<ZonedDateTime> getZonedDateTimes(String startTime, String endTime, TemporalAmount period) {
        ZonedDateTime start;
        try {
            start = ZonedDateTime.parse(startTime);
        }  catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Unable to parse start date from: " + startTime);
        }
        ZonedDateTime end;
        try {
            end = ZonedDateTime.parse(endTime);
        }  catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Unable to parse end date from: " + endTime);
        }

        List<ZonedDateTime> times = new ArrayList<>();
        ZonedDateTime currentTime = start;
        while (!currentTime.isAfter(end)) {
            times.add(currentTime);
            currentTime = currentTime.plus(period);
        }
        return times;
    }


    public static List<String> parseTimeDimensionAsStrings(String timeDimension) {
        return parseTimeDimension(timeDimension).stream()
                .map(time -> time.format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
                .collect(Collectors.toList());
    }
}
