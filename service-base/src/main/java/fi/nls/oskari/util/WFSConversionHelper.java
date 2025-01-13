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
    public static final String TYPE_COLLECTION = "collection";
    public static final String TYPE_POINT = "point";
    public static final String TYPE_LINE = "line";
    public static final String TYPE_AREA = "area";

    private static final Set<String> NUMBER_TYPES = new HashSet<>(
            Arrays.asList("double",
                "float",
                "byte",
                "decimal",
                "int",
                "number",
                "integer",
                "long",
                "negativeinteger",
                "nonnegativeinteger",
                "nonpositiveinteger",
                "positiveinteger",
                "short",
                "unsignedlong",
                "unsignedint",
                "unsignedshort",
                "unsignedbyte"
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
                "MultiLineStringPropertyType",
                "SurfacePropertyType",
                "MultiSurfacePropertyType",
                // OGC API Features/GeoJSON:
                "Point",
                "LineString",
                "Polygon",
                "MultiPoint",
                "MultiLineString",
                "MultiPolygon"
            )
    );
    private static final Set<String> STRING_TYPES = new HashSet<>(Arrays.asList("string", "date", "time"));
    private static final Set<String> BOOLEAN_TYPES = new HashSet<>(Arrays.asList("boolean"));

    public static boolean isNumberType(String type) {
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
        if (type == null) {
            return UNKNOWN;
        }
        if (isGeometryType(type)) {
            return GEOMETRY;
        }
        String lower = type.toLowerCase();
        if (isStringType(lower)) {
            return STRING;
        }
        if (isNumberType(lower)) {
            return NUMBER;
        }
        if (isBooleanType(lower)) {
            return BOOLEAN;
        }
        return UNKNOWN;
    }

    public static String getStyleType (String rawGeometryType) {
        if (rawGeometryType == null) {
            return UNKNOWN;
        }
        String lower = rawGeometryType.toLowerCase();
        if (lower.contains("surface") || lower.contains("polygon")) {
            return TYPE_AREA;
        }
        if (lower.contains(TYPE_POINT)) {
            return TYPE_POINT;
        }
        if (lower.contains(TYPE_LINE)) {
            return TYPE_LINE;
        }
        return TYPE_COLLECTION;
    }

    public static String getStringOrNumber (String type) {
        return isNumberType(type) ? NUMBER : STRING;

    }
    public static String stripNamespace(String tag) {
        String splitted[] = tag.split(":");
        if (splitted.length > 1) {
            return splitted[1];
        }
        return splitted[0];
    }
}
