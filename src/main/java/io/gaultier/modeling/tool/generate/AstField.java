package io.gaultier.modeling.tool.generate;

import io.gaultier.modeling.util.base.*;

import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;

public class AstField {

    private AstClass cl;
    private String name;
    private FieldDeclaration field;

    AstField(AstClass c, FieldDeclaration f) {
        cl = c;
        field = f;
        if (field.variables.size() > 0) {
            name = field.variables.get(0).id.name; //FIXME
        }
        //System.out.println(f);
    }

    AstField(AstClass c, EnumConstantDeclaration e) {
        //System.out.println(e);
        cl = c;
        name = e.name;
    }

    String getName() {
        return name;
    }

    String getExprValue() {
        String res = "";
        for (VariableDeclarator var : field.variables) {
            res += parseExpression(var.init);
        }
        return res;
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
            String s = cl.model.obtainClass(cl.resolveClass(qne.qualifier.name)).findField(qne.name).toString();
            return s;
         }
        else if (expr instanceof NameExpr) {
            NameExpr nam = (NameExpr) expr;
            return cl.findField(nam.name).toString();
        }
        else {
            assert false : expr.getClass().getSimpleName();
        }
        return "";
    }

    Object getValue() {
        Class<?> real;
        try {
            real = Class.forName(cl.getQualifiedName());
        } catch (ClassNotFoundException e) {
            throw new WrappedException(e);
        }
        Object[] vs = real.getEnumConstants();
        if (vs == null) {
            return null;
        }
        for (Object v : vs) {
            if (((Enum<?>) v).name().equals(getName())) {
                return v;
            }
        }
        return null;
    }
}
