package io.gaultier.modeling.model.data.enumtype;

import io.gaultier.modeling.model.data.*;

public class AmfEnumValues extends EnumValues {

    @Override
    protected boolean hasSpecialValue(Enum<?> e) {
        return e instanceof AmfEnum;
    }

    @Override
    protected DataType getSpecialType(Enum<?> e) {
        return DataType.INTEGER;
    }

    @Override
    protected Object getSpecialValue(Enum<?> e) {
        return ((AmfEnum) e).getAmfValue();
    }
}
