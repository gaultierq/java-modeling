package io.gaultier.modeling.tool.generate;

import japa.parser.ast.expr.*;

import java.util.*;

//import io.gaultier.modeling.ajax.annotation.*;

public class AstMethodAnnotation extends AstAnnotation {

    private AstMethod met;

    public AstMethodAnnotation(AstMethod m, AnnotationExpr a) {
        met = m;
        parsedAnn = a;
    }

    @Override
    public String getQualifiedName() {
        return getNormalName();
    }

    private String parseExpression(Expression expr) {
        if (expr instanceof LiteralExpr) {
            LiteralExpr lit = (LiteralExpr) expr;
            return (String) lit.value;
        }
        else if (expr instanceof BinaryExpr) {
            BinaryExpr bin = (BinaryExpr) expr;
            return parseExpression(bin.left) + parseExpression(bin.right);
        }
        else if (expr instanceof QualifiedNameExpr) {
           QualifiedNameExpr qne = (QualifiedNameExpr) expr;
           String s = met.getClassAssociated().model.obtainClass(met.getClassAssociated().resolveClass(qne.qualifier.name)).findField(qne.name).toString();
           return s;
        }
        else if (expr instanceof NameExpr) {
            NameExpr nam = (NameExpr) expr;
            return met.getClassAssociated().findField(nam.name).toString();
            //assert false : nam.data; //FIXME
        }
        else {
            assert false : expr.getClass().getSimpleName();
        }
        return "";
    }

    public String getSingleValue() {
        if (parsedAnn instanceof SingleMemberAnnotationExpr) {
            SingleMemberAnnotationExpr sma = (SingleMemberAnnotationExpr) parsedAnn;
            return parseExpression(sma.memberValue);
        }
        assert false : parsedAnn;
        return "";
    }

    @Override
    protected Map<String, Object> getValues() {
        if (values != null) {
            return values;
        }
        values = new HashMap<String, Object>();
        readValues(values);
        return values;
    }

    @Override
    protected Object getValue(Expression ex) {
        if (ex instanceof LiteralExpr) {
            Object res = ((LiteralExpr) ex).value;
            return res;
        }
        else if (ex instanceof ArrayInitializerExpr) {
            List<Expression> lex = ((ArrayInitializerExpr) ex).values;
            List<Object> res = new ArrayList<Object>();
            for (Expression e : lex) {
                res.add(getValue(e));
            }
            return res;
        }
        else if (ex instanceof QualifiedNameExpr) {
            QualifiedNameExpr q = (QualifiedNameExpr) ex;
            //FIXME
            assert false;
            //return RequestType.valueOf(q.name);
        }
        assert false : ex.getClass().getName();
        return null;
    }
}
