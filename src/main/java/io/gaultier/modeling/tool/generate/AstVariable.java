package io.gaultier.modeling.tool.generate;

import japa.parser.ast.body.*;
import japa.parser.ast.type.*;

public class AstVariable {

    private AstClass cl;
    private String name;
    private Type type;

    AstVariable(AstClass c, Parameter s) {
        cl = c;
        name = s.id.name;
        //modifiers = s.annotations;
        type = s.type;
    }

    String getName() {
        return name;
    }

    AstClass getType() {
        /*String t = cl.resolveClass(AstClass.getName(type));
        //System.out.println(t);
        AstClass c;
        c = cl.model.obtainClass(t);
        return c;*/
        return AstMethod.getType(cl, type);
    }
}
