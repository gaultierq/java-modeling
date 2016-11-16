package io.gaultier.modeling.model.data.enumtype;

import io.gaultier.modeling.model.data.*;

public class PersistentEnumValues extends EnumValues {

    @Override
    protected boolean hasSpecialValue(Enum<?> e) {
        return e instanceof PersistentEnum;
    }

    @Override
    protected DataType getSpecialType(Enum<?> e) {
        return ((PersistentEnum) e).getPersistentType();
    }

    @Override
    protected Object getSpecialValue(Enum<?> e) {
        return ((PersistentEnum) e).getPersistentValue();
    }
}
