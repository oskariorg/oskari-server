package fi.nls.oskari.domain.map.myfeatures;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import fi.nls.oskari.util.WFSConversionHelper;

public enum MyFeaturesFieldType {

    Boolean(WFSConversionHelper.BOOLEAN, Boolean.class, boolean.class),
    Integer(WFSConversionHelper.NUMBER, Long.class, byte.class, short.class, int.class, long.class, Byte.class, Short.class, Integer.class, java.math.BigInteger.class),
    Double(WFSConversionHelper.NUMBER, Double.class, float.class, double.class, Float.class, java.math.BigDecimal.class),
    String(WFSConversionHelper.STRING, String.class),
    Date(WFSConversionHelper.STRING, java.time.LocalDate.class),
    Timestamp(WFSConversionHelper.STRING,java.time.OffsetDateTime.class, java.util.Date.class, java.sql.Timestamp.class, java.time.Instant.class),
    UUID(WFSConversionHelper.STRING,java.util.UUID.class),
    ;

    private static final Map<Class<?>, MyFeaturesFieldType> CLASS_NAME_TO_FIELDTYPE;
    static {
        CLASS_NAME_TO_FIELDTYPE = new HashMap<>();
        for (MyFeaturesFieldType t : MyFeaturesFieldType.values()) {
            for (Class<?> b : t.bindings) {
                CLASS_NAME_TO_FIELDTYPE.put(b, t);
            }
        }
    }

    public static Optional<MyFeaturesFieldType> valueFromBinding(Class<?> binding) {
        return Optional.ofNullable(CLASS_NAME_TO_FIELDTYPE.get(binding));
    }

    private final String simpleType;
    private final Class<?>[] bindings;

    private MyFeaturesFieldType(String simpleType, Class<?>... bindings) {
        this.simpleType = simpleType;
        this.bindings = bindings;
    }

    public String getSimpleType() {
        return simpleType;
    }

    public Class<?> getOutputBinding() {
        return bindings[0];
    }

}
