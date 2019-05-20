package org.oskari.statistics.plugins.unsd;

import fi.nls.oskari.util.ConversionHelper;

import java.util.HashMap;
import java.util.Map;

public class CountryRegion implements Comparable<CountryRegion> {

    public enum Type {
        ISO2,
        ISO3,
        M49,
        M49_WO_LEADING
    }

    private String name;
    private Map<Type, String> codes = new HashMap<>();

    public CountryRegion(String name) {
        if (name != null) {
            name = name.trim();
        }
        this.name = name;
    }

    public void addCode(Type type, String code) {
        if (code == null) {
            code = "";
        }
        codes.put(type, code.trim());
        if(type.equals(Type.M49)) {
            // When getting data these are without the leading zeroes
            addCode(Type.M49_WO_LEADING, Integer.toString(ConversionHelper.getInt(code, -999)));
        }
    }

    public String getCode(Type type) {
        return codes.getOrDefault(type, "");
    }

    public boolean matches(String anyCode) {
        return codes.values().stream()
                .filter(c -> c.equalsIgnoreCase(anyCode))
                .findFirst()
                .isPresent();
    }

    public boolean isValid() {
        return !getCode(Type.M49).isEmpty()
                && !getCode(Type.ISO2).isEmpty()
                && hasValue(name);
    }

    private boolean hasValue(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public String toString() {
        return String.join(";", getCode(Type.ISO2), getCode(Type.ISO3), getCode(Type.M49), name);
    }

    @Override
    public int compareTo(CountryRegion o) {
        // dummy impl
        return name.compareTo(o.name);
    }
}
