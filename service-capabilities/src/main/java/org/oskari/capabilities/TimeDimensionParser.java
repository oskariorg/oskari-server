package org.oskari.capabilities;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

        String intervalStr = parts[2];

        ChronoUnit unit;
        int interval;

        try {
            if (intervalStr.startsWith("PT") && intervalStr.endsWith("H")) {
                unit = ChronoUnit.HOURS;
                interval = Integer.parseInt(intervalStr.substring(2, intervalStr.length() - 1));
            } else if (intervalStr.startsWith("P") && intervalStr.endsWith("D")) {
                unit = ChronoUnit.DAYS;
                interval = Integer.parseInt(intervalStr.substring(1, intervalStr.length() - 1));
            } else if (intervalStr.startsWith("P") && intervalStr.endsWith("M")) {
                unit = ChronoUnit.MONTHS;
                interval = Integer.parseInt(intervalStr.substring(1, intervalStr.length() - 1));
            } else if (intervalStr.startsWith("P") && intervalStr.endsWith("Y")) {
                unit = ChronoUnit.YEARS;
                interval = Integer.parseInt(intervalStr.substring(1, intervalStr.length() - 1));
            } else {
                throw new IllegalArgumentException("Unsupported interval format: " + intervalStr);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unsupported interval format: " + intervalStr);
        }

        ZonedDateTime start;
        try {
            start = ZonedDateTime.parse(parts[0]);
        }  catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Unable to parse start date from: " + parts[0]);
        }
        ZonedDateTime end;
        try {
            end = ZonedDateTime.parse(parts[1]);
        }  catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Unable to parse end date from: " + parts[1]);
        }

        List<ZonedDateTime> times = new ArrayList<>();
        ZonedDateTime currentTime = start;
        while (!currentTime.isAfter(end)) {
            times.add(currentTime);
            currentTime = currentTime.plus(interval, unit);
        }

        return times;
    }


    public static List<String> parseTimeDimensionAsStrings(String timeDimension) {
        return parseTimeDimension(timeDimension).stream()
                .map(time -> time.format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
                .collect(Collectors.toList());
    }
}
