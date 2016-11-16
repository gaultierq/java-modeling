package io.gaultier.modeling.tool.generate;

import japa.parser.ast.expr.*;

import java.lang.annotation.*;
import java.util.*;

public abstract class AstAnnotation {

    protected Annotation liveAnn;
    protected AnnotationExpr parsedAnn;
    protected Map<String, Object> values;
    
    public abstract String getQualifiedName();
    protected abstract Object getValue(Expression ex);
    protected abstract Map<String, Object> getValues();
    
    protected Annotation getLiveAnnotation() {
        return liveAnn;
    }
    
    protected String getNormalName() {
        /*if (an == null) {
            return getLiveAnnotation().annotationType().getName();
        }*/
        if (parsedAnn instanceof MarkerAnnotationExpr) {
            return ((MarkerAnnotationExpr) parsedAnn).name.name;
        }
        if (parsedAnn instanceof SingleMemberAnnotationExpr) {
            return ((SingleMemberAnnotationExpr) parsedAnn).name.name;
        }
        if (parsedAnn instanceof NormalAnnotationExpr) {
            return ((NormalAnnotationExpr) parsedAnn).name.name;
        }
        assert false : parsedAnn;
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getEnum(String name) {
        return (T) getValues().get(name);
    }
    
    protected void addValue(Map<String, Object> values, String name, Expression ex) {
        values.put(name, getValue(ex));
    }

    protected void readValues(Map<String, Object> values) {
        if (parsedAnn instanceof NormalAnnotationExpr) {
            NormalAnnotationExpr na = (NormalAnnotationExpr) parsedAnn;
            for (MemberValuePair mvp : na.pairs) {
                addValue(values, mvp.name, mvp.value);
            }
        }
        else if (parsedAnn instanceof SingleMemberAnnotationExpr) {
            SingleMemberAnnotationExpr sma = (SingleMemberAnnotationExpr) parsedAnn;
            addValue(values, "value", sma.memberValue);
        }
        else if (parsedAnn instanceof MarkerAnnotationExpr) {
        }
        else {
            assert false : parsedAnn;
        }
    }
    
    public String getString(String name) {
        return (String) getValues().get(name);
    }

    public Boolean getBoolean(String name) {
        Boolean b = (Boolean) getValues().get(name);
        //assert b != null : name;
        if (b == null) {
            return false;
        }
        return b;
    }
    
    public Integer getInt(String name) {
        Integer i = (Integer) getValues().get(name);
        //assert b != null : name;
        if (i == null) {
            return -1;
        }
        return i;
    }
}
