package io.gaultier.modeling.model.data;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface DataField {

    String name();
    DataType type() default DataType.STRING;
    Class<? extends Enum<?>> enumType() default DataType.class; // Default, means not enum
    Class<?> object() default DataField.class; // DataField will never be used here
    Class<?> list() default DataField.class;

    boolean persistent() default true;
    String column() default "";
    PrimaryKeyType primaryKey() default PrimaryKeyType.NO;

    boolean amf() default true;
    boolean json() default true;
    boolean html() default true;
    
    int syntaxes() default -1; //unset
    
    PrimitiveSubstitution substitution() default PrimitiveSubstitution.DEFAULT;

    String attr() default "";
}
