package fi.nls.oskari.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WFSConversionHelper {
    public static final String STRING = "string";
    public static final String NUMBER = "number";
    public static final String BOOLEAN = "boolean";
    public static final String GEOMETRY = "geometry";
    public static final String UNKNOWN = "unknown";
    private static final Set<String> NUMBER_TYPES = new HashSet<>(
            Arrays.asList("double",
                "float",
                "byte",
                "decimal",
                "int",
                "integer",
                "long",
                "negativeInteger",
                "nonNegativeInteger",
                "nonPositiveInteger",
                "positiveInteger",
                "short",
                "unsignedLong",
                "unsignedInt",
                "unsignedShort",
                "unsignedByte"
            )
    );
    private static final Set<String> GEOMETRY_TYPES = new HashSet<>(
            Arrays.asList(
                "GeometryPropertyType",
                "PointPropertyType",
                "LinePropertyType",
                "PolygonPropertyType",
                "MultiPointPropertyType",
                "MultiLinePropertyType",
                "MultiPolygonPropertyType",
                "SurfacePropertyType"
            )
    );
    private static final Set<String> STRING_TYPES = new HashSet<>(Arrays.asList("string", "date", "time"));
    private static final Set<String> BOOLEAN_TYPES = new HashSet<>(Arrays.asList("boolean"));

    public static boolean isNumeberType (String type) {
        return NUMBER_TYPES.contains(type);
    }
    public static boolean isStringType (String type) {
        return STRING_TYPES.contains(type);
    }
    public static boolean isBooleanType (String type) {
        return BOOLEAN_TYPES.contains(type);
    }
    public static boolean isGeometryType (String type) {
        return GEOMETRY_TYPES.contains(type);
    }
    public static String getSimpleType (String type) {
        if (isGeometryType(type)) {
            return GEOMETRY;
        }
        if (isStringType(type)) {
            return STRING;
        }
        if (isNumeberType(type)) {
            return NUMBER;
        }
        if (isBooleanType(type)) {
            return BOOLEAN;
        }
        return UNKNOWN;
    }
    public static String getStringOrNumber (String type) {
        return isNumeberType(type) ? NUMBER : STRING;

    }
    public static String stripNamespace(String tag) {
        String splitted[] = tag.split(":");
        if (splitted.length > 1) {
            return splitted[1];
        }
        return splitted[0];
    }
}
