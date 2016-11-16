package io.gaultier.modeling.model.data.enumtype;

import io.gaultier.modeling.model.data.*;

public interface PersistentEnum {

    DataType getPersistentType();

    Object getPersistentValue();
}
