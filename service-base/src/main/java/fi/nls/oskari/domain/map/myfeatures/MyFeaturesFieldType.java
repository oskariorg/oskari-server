package fi.nls.oskari.domain.map.myfeatures;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum MyFeaturesFieldType {

    Boolean(Boolean.class, boolean.class),
    Integer(Long.class, byte.class, short.class, int.class, long.class, Byte.class, Short.class, Integer.class, java.math.BigInteger.class),
    Double(Double.class, float.class, double.class, Float.class, java.math.BigDecimal.class),
    String(String.class),
    Date(java.time.LocalDate.class),
    Timestamp(java.time.OffsetDateTime.class, java.util.Date.class, java.sql.Timestamp.class, java.time.Instant.class),
    UUID(java.util.UUID.class),
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

    private final Class<?>[] bindings;

    private MyFeaturesFieldType(Class<?>... bindings) {
        this.bindings = bindings;
    }

    public Class<?> getOutputBinding() {
        return bindings[0];
    }

}
