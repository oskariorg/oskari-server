package fi.nls.oskari.control.statistics.plugins.unsd;

import fi.nls.oskari.util.ConversionHelper;

public class CountryRegion implements Comparable<CountryRegion> {
    int m49woleadingZeroes;
    String m49;
    String iso2;
    String iso3;
    String name;

    public CountryRegion(String iso2, String iso3, String m49, String name) {
        this.iso2 = iso2;
        this.iso3 = iso3;
        this.m49 = m49;
        this.m49woleadingZeroes = ConversionHelper.getInt(m49, -999);
        this.name = name;
        if (this.iso2 == null) {
            this.iso2 = "";
        }
    }

    public boolean matches(String anyCode) {
        boolean matchesIso = anyCode.equalsIgnoreCase(iso2) ||
                anyCode.equalsIgnoreCase(iso3);
        if (matchesIso) {
            return true;
        }
        // When getting data these are without the leading zeroes
        return anyCode.equalsIgnoreCase(m49) ||
                ConversionHelper.getInt(anyCode, -1) == m49woleadingZeroes;
    }

    public boolean isValid() {
        return hasValue(m49) && hasValue(iso3) && hasValue(name);
    }

    private boolean hasValue(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public String toString() {
        return String.join(";", iso2, iso3, m49, name);
    }

    @Override
    public int compareTo(CountryRegion o) {
        // dummy impl
        return name.compareTo(o.name);
    }
}
