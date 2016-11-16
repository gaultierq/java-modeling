package io.gaultier.modeling.model.data.enumtype;

import java.util.*;

import io.gaultier.modeling.model.data.*;

public abstract class EnumValues {

    private Map<Enum<?>, Object> enumToValue;
    private Map<Object, Enum<?>> valueToEnum;

    protected abstract boolean hasSpecialValue(Enum<?> e);

    protected abstract Object getSpecialValue(Enum<?> e);

    protected abstract DataType getSpecialType(Enum<?> e);

    public DataType init(Class<? extends Enum<?>> e) {
        DataType type = null;
        Enum<?>[] enumValues = e.getEnumConstants();
        enumToValue = new HashMap<Enum<?>, Object>();
        valueToEnum = new HashMap<Object, Enum<?>>();
        Boolean special = null;
        for (Enum<?> enumValue : enumValues) {
            if (hasSpecialValue(enumValue)) {
                if (special == null) {
                    special = true;
                    type = getSpecialType(enumValue);
                } else {
                    assert special.booleanValue();
                    assert type == getSpecialType(enumValue);
                }
            } else {
                if (special == null) {
                    special = false;
                    type = DataType.INTEGER;
                } else {
                    assert !special.booleanValue();
                }
            }
            Object value;
            if (special) {
                value = getSpecialValue(enumValue);
            } else {
                value = enumValue.ordinal();
            }
            if (value != null) {
                enumToValue.put(enumValue, value);
                valueToEnum.put(value, enumValue);
            }
        }
        assert type != null;
        return type;
    }

    public Object enumToValue(Object e) {
        return enumToValue.get(e);
    }

    public Enum<?> valueToEnum(Object e) {
        return valueToEnum.get(e);
    }
}
