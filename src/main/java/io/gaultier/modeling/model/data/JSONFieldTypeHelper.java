package io.gaultier.modeling.model.data;


public class JSONFieldTypeHelper {

    public static DataDefinition<?> getObjectType(FieldType t) {
        return t.getObjectType();
    }
}
