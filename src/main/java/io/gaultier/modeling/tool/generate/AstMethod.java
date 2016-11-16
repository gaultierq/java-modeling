package io.gaultier.modeling.tool.generate;

import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.type.*;

import java.util.*;

public class AstMethod {

    private AstClass cl;
    private MethodDeclaration md;

    AstMethod(AstClass c, MethodDeclaration d) {
        cl = c;
        md = d;
        //System.out.println(md);
    }

    public boolean isStatic() {
        return ModifierSet.isStatic(md.modifiers);
    }

    public boolean isPublic() {
        return ModifierSet.isPublic(md.modifiers);
    }

    String getName() {
        return md.name;
    }
    
    public AstClass getReturnType() {
        return getType(cl, md.type);
    }

    public Collection<AstVariable> getParameters() {
        Collection<AstVariable> res = new ArrayList<AstVariable>();
        for (Parameter p : AstClass.nvl(md.parameters)) {
            res.add(new AstVariable(cl, p));
        }
        return res;
    }
    
    public Collection<AstMethodAnnotation> getAnnotations() {
        Collection<AstMethodAnnotation> res = new ArrayList<AstMethodAnnotation>();
        if (md.annotations != null) {
            for (AnnotationExpr an : md.annotations) {
                res.add(new AstMethodAnnotation(this, an));
            }
        }
        return res;
    }
    
    public Collection<?> getReturnTypes() {
        //for (Type t : md.type)
        return null;
    }

    static AstClass getType(AstClass cl, Type type) {
        AstClass c;
        if (type instanceof VoidType) {
            c = new AstClass(cl.model, "void");
        }
        else if (type instanceof PrimitiveType) {
            String n = ((PrimitiveType) type).type.name();
            c = new AstClass(cl.model, n.toLowerCase());
        }
        else {
            c = new AstClass(cl.model, cl.resolveClass(AstClass.getName(type)));
            //TO-DO not found
        }
        //System.out.println(type.getClass());
        if (type instanceof ReferenceType) {
            ReferenceType rt = (ReferenceType) type;
            if (rt.type instanceof ClassOrInterfaceType) {
                //System.out.println(((ReferenceType) type).type.getClass());
                for (Type p : AstClass.nvl(((ClassOrInterfaceType) rt.type).typeArgs)) {
                    c.addArgument(getType(cl, p));
                    //System.out.println("<<<<  " + p);
                }
            }
            c.setArray(rt.arrayCount);
        }
        return c;//cl.mp.getClass(c);
    }
    
    public AstClass getClassAssociated() {
        return cl;
    }

    /*String getDocumentation() {
        return md.toString();
    }*/
}
