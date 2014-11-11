package fi.nls.oskari.spatineo.dto;

import com.google.common.base.MoreObjects;

import java.util.List;

/**
 * A data transfer object for interacting with the Spatineo Serval JSON interface.
 */
public class SpatineoResponseDto {

    public static class Stats {
        public Double hoursDown;
        public Double hoursMaintenance;
        public Double hoursUp;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(Stats.class)
                    .add("hoursDown", hoursDown)
                    .add("hoursMaintenance", hoursMaintenance)
                    .add("hoursUp", hoursUp)
                    .toString();
        }
    }

    public static class Result {
        public String infoUrl;
        public String status;
        public String statusMessage;
        public Stats week;
        public Stats year;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(Result.class)
                    .add("status", status)
                    .add("statusMessage", statusMessage)
                    .add("infoUrl", infoUrl)
                    .add("week", week)
                    .add("year", year)
                    .toString();
        }
    }

    public List<Result> result;

    public String status;

    public String version;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(SpatineoResponseDto.class)
                .add("status", status)
                .add("version", version)
                .add("result", result)
                .toString();
    }
}
