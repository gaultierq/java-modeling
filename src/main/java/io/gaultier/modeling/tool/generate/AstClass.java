package io.gaultier.modeling.tool.generate;

import japa.parser.ast.*;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.type.*;

import java.io.*;
import java.util.*;

public class AstClass {

    private static final Collection<String> PRIMITIVE_TYPES = new HashSet<String>(Arrays.asList("boolean", "byte", "char", "double", "float", "int", "long", "short", "void"));

    final SourceModel model;
    private File source;
    private CompilationUnit cu;
    private TypeDeclaration td;
    private String name;

    private int array;
    private final List<AstClass> typeArguments = new ArrayList<AstClass>();
    private Map<String, String> classes = new HashMap<String, String>();
    private Set<String> singleImports = new LinkedHashSet<String>();
    private Set<String> onDemandImports = new LinkedHashSet<String>();
    private Set<String> onDemandStaticImports = new LinkedHashSet<String>();

    AstClass(SourceModel m, File s) {
        model = m;
        source = s;
    }

    AstClass(SourceModel m, String n) {
        model = m;
        name = n;
    }

    void setArray(int a) {
        array = a;
    }

    @Override
    public String toString() {
        return "[AstClass: " + name + "]";
    }

    void read(CompilationUnit c) {
        cu = c;
        assert cu.types != null : source;
        for (Object o : cu.types) {
            if (!(o instanceof TypeDeclaration)) {
                continue;
            }
            TypeDeclaration t = (TypeDeclaration) o;
            if (!ModifierSet.isPublic(t.modifiers)) {
                continue;
            }
            assert td == null;
            td = t;
        }
        for (ImportDeclaration id : nvl(cu.imports)) {
            if (id.isStatic) {
                if (id.isAsterisk) {
                    addOnDemandStaticImport(id.name.toString());
                }
            }
            if (id.isAsterisk) {
                addOnDemandImport(id.name.toString());
            } else {
                addSingleImport(id.name.toString());
            }
        }
    }

    File getSource() {
        return source;
    }

    protected void addSingleImport(String i) {
        singleImports.add(i);
    }

    protected void addOnDemandImport(String i) {
        onDemandImports.add(i);
    }

    protected void addOnDemandStaticImport(String i) {
        onDemandStaticImports.add(i);
        System.out.println("s: " + i);
    }

    boolean isParsed() {
        return cu != null;
    }

    public String getQualifiedName() {
        String p = getPackageName();
        return p == null || p.isEmpty() ? getSimpleName() : p + "." + getSimpleName();
    }

    public String getSimpleName() {
        if (isParsed()) {
            return td == null ? null : td.name;
        }
        if (name == null) {
            return null;
        }
        int i = name.lastIndexOf('.');
        String res = i < 0 ? name : name.substring(i + 1);
        for (i = 0; i < array; i++) {
            res += "[]";
        }
        return res;
    }

    public String getPackageName() {
        if (isParsed()) {
            return td == null ? null : cu.pakage.toString();
        }
        if (name == null) {
            return null;
        }
        int i = name.lastIndexOf('.');
        return i < 0 ? "" : name.substring(0, i);
    }

    public void addArgument(AstClass c) {
        typeArguments.add(c);
    }

    public List<AstClass> getArguments() {
        return typeArguments;
    }

    String resolveClass(String c) {
        String r = classes.get(c);
        if (r != null) {
            return r;
        }
        if (PRIMITIVE_TYPES.contains(c)) {
            return c;
        }
        /*if (typeParameters.contains(c)) {
            return null;
        }*/
        r = resolveClassFromImports(c);
        if (r == null) {
            //System.out.println(typeParameters + " " + singleImports + " " + onDemandImports + " " + cl);
            model.warning("No class " + c + " in " + getQualifiedName());
            //new Exception().printStackTrace();
            return null;
        }
        classes.put(c, r);
        return r;
    }

    private String resolveClassFromImports(String c) {
        if (findClass(c)) {
            return c;
        }
        for (String i : singleImports) {
            int j = i.lastIndexOf('.');
            if ((j < 0 ? i : i.substring(j + 1)).equals(c)) {
                return i;
            }
        }
        String s = getQualifiedName();
        //System.out.println("qn: " + s);
        s = s.substring(0, s.lastIndexOf('.'));
        s += "." + c;
        if (findClass(s)) {
            return s;
        }
        for (String i : onDemandImports) {
            //System.out.println("*" + i);
            s = i + "." + c;
            if (findClass(s)) {
                return s;
            }
        }
        s = "java.lang." + c;
        if (findClass(s)) {
            return s;
        }
        return null;
    }

    protected boolean findClass(String c) {
        if (c == null) {
            return false;
        }
        //System.out.println("Find: " + c);
        if (model.getClass(c) != null) {
            return true;
        }
        return model.getCompiledClass(c).exists();
    }

    Object findField(String name) {
        for (AstField f : getFields()) {
            if (name.equals(f.getName())) {
                return f.getExprValue();
            }
        }
        return null;
    }

    Object findConstant(String name) {
        for (String i : onDemandStaticImports) {
            AstClass c = model.obtainClass(i);
            for (AstField f : c.getFields()) {
                if (name.equals(f.getName())) {
                    return f.getValue();
                }
            }
        }
        return null;
    }

    static String getName(Type t) {
        if (t instanceof ClassOrInterfaceType) {
            return ((ClassOrInterfaceType) t).name;
        }
        if (t instanceof ReferenceType) {
            return getName(((ReferenceType) t).type);
        }
        if (t instanceof PrimitiveType) {
            return ((PrimitiveType) t).type.toString().toLowerCase();
        }
        assert false : t.getClass();
        return null;
    }

    public String getFullName() {
        String res = "";
        //System.out.println(typeArguments);
        if (!typeArguments.isEmpty()) {
            for (AstClass pc : typeArguments) {
                if (res.length() != 0) {
                    res += ", ";
                }
                res += pc.getFullName();
            }
            res = "<" + res + ">";
        }
        res = getQualifiedName() + res;
        return res;
    }

    public Collection<AstClassAnnotation> getAnnotations() {
        Collection<AstClassAnnotation> res = new ArrayList<AstClassAnnotation>();
        if (td == null || !(td instanceof ClassOrInterfaceDeclaration)) {
            return res;
        }
        for (AnnotationExpr an : nvl(((ClassOrInterfaceDeclaration) td).annotations)) {
            res.add(new AstClassAnnotation(this, an));
        }
        return res;
    }

    public Collection<AstField> getFields() {
        Collection<AstField> res = new ArrayList<AstField>();
        System.out.println("getFields " + td + " " + getQualifiedName());
        if (td == null) {
            return res;
        }
        for (BodyDeclaration m : nvl(td.members)) {
            if (!(m instanceof FieldDeclaration)) {
                continue;
            }
            res.add(new AstField(this, (FieldDeclaration) m));
        }
        if (td instanceof EnumDeclaration) {
            EnumDeclaration ed = (EnumDeclaration) td;
            for (EnumConstantDeclaration c : ed.entries) {
                res.add(new AstField(this, c));
            }
        }
        return res;
    }

    public Collection<AstMethod> getMethods() {
        Collection<AstMethod> res = new ArrayList<AstMethod>();
        if (td == null) {
            return res;
        }
        for (BodyDeclaration m : nvl(td.members)) {
            if (!(m instanceof MethodDeclaration)) {
                //System.out.println(m.getClass());
                continue;
            }
            res.add(new AstMethod(this, (MethodDeclaration) m));
        }
        return res;
    }

    static <T> Collection<T> nvl(Collection<T> c) {
        if (c != null) {
            return c;
        }
        return Collections.emptyList();
    }

    void process() {
    }

    boolean isPrimitive() {
        String n = getQualifiedName();
        return PRIMITIVE_TYPES.contains(n);
    }

    boolean isCollection() {
        String n = getQualifiedName();
        return Collection.class.getName().equals(n) || List.class.getName().equals(n) || isDataList();
    }

    boolean isDataList() {
        String n = getQualifiedName();
        return JavaWriter.DATA_LIST.equals(n);
    }

    boolean isId() {
        String n = getQualifiedName();
        return JavaWriter.ID.equals(n);
    }

    boolean isEnum() {
        if (isPrimitive()) {
            return false;
        }
        if (array != 0) {
            return false;
        }
        if (td != null) {
            if (td instanceof EnumDeclaration) {
                return true;
            }
            return false;
        }
        if (getSimpleName().endsWith("Data")) {
            return false;
        }
        try {
            Class<?> cl = Class.forName(getQualifiedName());
            //assert !cl.isEnum() : getQualifiedName();
            return cl.isEnum();
        }
        catch (ClassNotFoundException e) {
            //e.printStackTrace();
            model.warning(e.toString() + " (Enum class needs to be added in build.xml ?)");
            return false;
        }
        catch (NoClassDefFoundError e) {
            //e.printStackTrace();
            model.warning(e.toString());
            if (e.getCause() != null) {
                model.warning("Caused by: " + e.getCause());
            }
            return false;
        }
    }

    String getDefaultValue() {
        if (isPrimitive()) {
            String n = getQualifiedName();
            if ("int".equals(n)) {
                return "0";
            }
            if ("long".equals(n)) {
                return "0L";
            }
            if ("boolean".equals(n)) {
                return "false";
            }
            assert false : n;
        }
        return "null";
    }
}
