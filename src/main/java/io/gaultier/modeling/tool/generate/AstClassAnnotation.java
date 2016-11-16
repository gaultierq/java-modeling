package io.gaultier.modeling.tool.generate;

import japa.parser.ast.expr.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import io.gaultier.modeling.util.base.*;

public class AstClassAnnotation extends AstAnnotation {

    private AstClass cl;
    
    AstClassAnnotation(AstClass c, AnnotationExpr a) {
        cl = c;
        parsedAnn = a;
    }

    private AstClass getBaseClass() {
        return cl;
    }

    @Override
    public String getQualifiedName() {
        return getLiveAnnotation() == null ? cl.resolveClass(getNormalName()) : getLiveAnnotation().annotationType().getName();
    }

    @Override
    protected Map<String, Object> getValues() {
        if (values != null) {
            return values;
        }
        CompiledClass acl = cl.model.getCompiledClass(getQualifiedName());
        values = acl.getAnnotationDefaultValues();
        readValues(values);
        return values;
    }

    public Collection<AstClassAnnotation> getAnnotations(String name) {
        Object o = getValues().get(name);
        assert o != null : name;
        if (o instanceof Collection<?>) {
            @SuppressWarnings("unchecked")
            Collection<AstClassAnnotation> list = (Collection<AstClassAnnotation>) o;
            return list;
        }
        if (o instanceof AstClassAnnotation) {
            /*System.out.println(an);
            ((JdtAnnotation) o).cree.printStackTrace();*/
            return Collections.singletonList((AstClassAnnotation) o);
        }
        List<AstClassAnnotation> l = new ArrayList<AstClassAnnotation>();
        for (Annotation a : (Annotation[]) o) {
            AstClassAnnotation ba = new AstClassAnnotation(cl, null);
            ba.liveAnn = a;
            l.add(ba);
        }
        getValues().put(name, l);
        //System.out.println(o.getClass());
        return l;
    }

    @Override
    protected Object getValue(Expression ex) {
        if (ex instanceof LiteralExpr) {
            Object res = ((LiteralExpr) ex).value;
            return res;
        }
        if (ex instanceof ArrayInitializerExpr) {
            List<Expression> lex = ((ArrayInitializerExpr) ex).values;
            List<Object> res = new ArrayList<Object>();
            for (Expression e : lex) {
                res.add(getValue(e));
            }
            return res;
        }
        if (ex instanceof NormalAnnotationExpr) {
            return new AstClassAnnotation(getBaseClass(), (NormalAnnotationExpr) ex);
        }
        if (ex instanceof SingleMemberAnnotationExpr) {
            return new AstClassAnnotation(getBaseClass(), (SingleMemberAnnotationExpr) ex);
        }
        if (ex instanceof QualifiedNameExpr) {
            QualifiedNameExpr e = (QualifiedNameExpr) ex;
            try {
                Class<?> c = Class.forName(getBaseClass().resolveClass(e.qualifier.name));//TO-DO class
                Field f = c.getField(e.name);
                return f.get(null);
            } catch (Exception e1) {
                throw new WrappedException(e1);
            }
        }
        if (ex instanceof ClassExpr) {
            String c = getBaseClass().resolveClass(AstClass.getName(((ClassExpr) ex).type));
            return getBaseClass().model.getClass(/*c == null ? Object.class.getName() : */c);
            //TO-DO not found:
            //TO-DO Uniquement pour instanceof
        }
        if (ex instanceof NameExpr) {
            String name = ((NameExpr) ex).name;
            return cl.findConstant(name);
        }
        assert false : ex.getClass();
        return false;
    }

    public AstClass getClass(String name) {
        Object v = getValues().get(name);
        assert v != null : name;
        if (v instanceof AstClass) {
            return (AstClass) v;
        }
        v = cl.model.obtainClass(((Class<?>) v).getName());
        getValues().put(name, v);
        return (AstClass) v;
    }
}
