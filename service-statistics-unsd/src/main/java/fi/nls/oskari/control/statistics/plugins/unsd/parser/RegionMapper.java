package fi.nls.oskari.control.statistics.plugins.unsd.parser;

import fi.nls.oskari.control.statistics.plugins.unsd.CountryRegion;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RegionMapper {

    private List<CountryRegion> countries;

    public RegionMapper() {
        countries = readResource().stream()
                .map(row -> parseRow(row))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<String> readResource() {
        try {
            return IOHelper.readLines(getClass().getResourceAsStream("/M49codes.csv"));
        } catch (IOException ex) {
            throw new ServiceRuntimeException("Unable to read M49 country mapping", ex);
        }
    }


    private static Optional<CountryRegion> parseRow(String row) {
        // FI;FIN;246;Finland
        String[] data = row.split(";");
        if (data.length != 4) {
            return Optional.empty();
        }

        CountryRegion c = new CountryRegion(data[0].trim(), data[1].trim(), data[2].trim(), data[3].trim());
        if (c.isValid()) {
            return Optional.of(c);
        }
        return Optional.empty();
    }


    public Optional<CountryRegion> find(String anyCode) {
        return countries.stream().filter(c -> c.matches(anyCode)).findFirst();
    }
}