package io.gaultier.modeling.model.data;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface DataClass {

    DataField[] fields();
    String table() default "";
    boolean amf() default true;
    boolean json() default false;
    boolean local() default false;
    
    int syntaxes() default -1; //-1 == unset
    
    String location() default "";
}
