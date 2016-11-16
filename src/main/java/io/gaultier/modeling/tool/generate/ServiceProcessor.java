package io.gaultier.modeling.tool.generate;

import java.util.*;

public class ServiceProcessor {

    private static final String SUFFIX = "Service";

    private final SourceModel model;
    private boolean display = false;

    ServiceProcessor(SourceModel m) {
        model = m;
    }

    private void processService(ModelProcessor mp, AstClass c, AstClassAnnotation a) {
        String name = c.getSimpleName();
        if (!name.endsWith(SUFFIX)) {
            model.warning(c.getQualifiedName() + " is marked as service");
            return;
        }
        name = name.substring(0, name.length() - SUFFIX.length());
        if (display) {
            System.out.println(name);
        }
        String facName = name + "Facade";
        String amfName = facName + "Amf";
        String apiName = name + "ServiceAmf";
        JavaWriter fw = null;
        JavaWriter aw = null;
        JavaWriter pw = null;
        Collection<String> enumTypes = new TreeSet<String>();
        for (AstMethod m : c.getMethods()) {
            if (m.isStatic()) {
                continue;
            }
            if (!m.isPublic()) {
                continue;
            }
            if (fw == null || aw == null || pw == null) {
                fw = new JavaWriter(model, c.getPackageName(), facName);
                fw.writeln("package " + c.getPackageName() + ";");
                fw.writeln("public interface " + facName + " {");

                aw = new JavaWriter(model, c.getPackageName(), amfName);
                aw.writeln("package " + c.getPackageName() + ";");
                aw.ln();
                aw.writeln("public final class " + amfName + " {");

                aw.ln();
                aw.writeln("\tprivate " + c.getPackageName() + "." + facName +
                        " facade = " + JavaWriter.SERVICE_REPOSITORY + ".get().getAmf(" +
                        c.getPackageName() + "." + facName + ".class);");

                pw = new JavaWriter(model, ModelClass.getApiAmfPackage(c.getPackageName()), apiName);
                pw.writeln("package " + ModelClass.getApiAmfPackage(c.getPackageName()) + ";");
                pw.ln();
                pw.generated();
                pw.writeln("public final class " + apiName + " extends " + JavaWriter.SERVICE_AMF + " {");
                pw.ln();
                pw.writeln("\tpublic " + apiName + "(" + JavaWriter.REMOTE_INVOKER + " i) {");
                pw.writeln("\t\tsuper(i);");
                pw.writeln("\t\tremoteLocation = \"" + JavaWriter.toFieldName(name).toLowerCase() + ".\";");
                pw.writeln("\t}");
            }

            writeMethod(mp, fw, aw, pw, m, enumTypes);
        }

        if (fw != null && aw != null && pw != null) {
            for (String e : enumTypes) {
                aw.ln();
                aw.writeln("\tprivate static final " + JavaWriter.FIELD_TYPE + " " + getEnumTypeConstant(e) +
                        " = " + JavaWriter.FIELD_TYPE + ".getEnum(" + e + ".class);");
            }
            fw.writeln("}");
            aw.writeln("}");
            pw.writeln("}");
            fw.output();
            aw.output();
            pw.output();
        }
        else {
            model.warning("No services for " + c.getQualifiedName());
        }
    }

    private void writeMethod(ModelProcessor mp, JavaWriter fw, JavaWriter aw, JavaWriter pw, AstMethod m, Collection<String> enumTypes) {
        if (display) {
            System.out.println("\t" +  m.getName());
        }
        aw.ln();
        aw.write("\tpublic ");
        pw.ln();

        boolean isReturningCollection = m.getReturnType().isCollection();
        String at = " " + m.getName() + "(";
        String ft = at;
        String pt = at;
        String ty;
        boolean first = true;
        for (AstVariable p : m.getParameters()) {
            if (first) {
                first = false;
            }
            else {
                at += ", ";
                ft += ", ";
                pt += ", ";
            }
            ft += p.getType().getFullName();
            if (p.getType().isId()) {
                at += "long";
                pt += "long";
            }
            else if (p.getType().isEnum()) {
                at += "int";
                pt += "int";
                //System.out.println(p.getType().getQualifiedName() + " is enum");
            }
            else { //TO-DO datalist
                //System.out.println(p.getType().getFullName());
                at += p.getType().getFullName();
                ModelClass mc = mp.getModelClass(p.getType());
                //System.out.println(p.getType().getQualifiedName() + p.getType() + " " + mc);
                if (mc == null) {
                    String c = p.getType().getFullName();
                    if (p.getType().isCollection()) {
                        if (c.indexOf('<') >= 0) {
                            pt += c.substring(0, c.indexOf('<')) + "<?>";
                        }
                    }
                    else {
                        pt += c;
                    }
                }
                else {
                    pt += mc.getApiAmfStubQualifiedName();
                }
            }
            at += " " + p.getName();
            ft += " " + p.getName();
            pt += " " + p.getName();
        }
        fw.writeln(m.getReturnType().getFullName() + ft + ");");

        String pty;
        if (isReturningCollection) {
            ty = Object.class.getName() + "[]";
            pty = ty;
        }
        else if (m.getReturnType().isId()) {
            ty = "long";
            pty = ty;
        }
        else {
            ty = m.getReturnType().getFullName();
            ModelClass mc = mp.getModelClass(m.getReturnType());
            pty = mc == null ? ty : mc.getApiAmfStubQualifiedName();
        }
        aw.writeln(ty + at + ") {");
        aw.writeln("\t\t" + JavaWriter.BOT_LIMITER + ".service(1);");

        if (pty.indexOf('<') >= 0) {
            pw.writeln("\t@SuppressWarnings(\"unchecked\")");
        }
        pw.write("\tpublic ");
        pw.writeln(pty + pt + ") {");

        at = "\t\t";
        pt = "\t\t";
        if (isReturningCollection) {
            at += m.getReturnType().getFullName() + " resCollection = ";
            pt += "return (" + pty + ") ";
        }
        else if (m.getReturnType().isId()) {
            at += "return ";
            pt += "Number resPrimitive = (Number) ";
        }
        else if (!"void".equals(m.getReturnType().getFullName())) {
            if (m.getReturnType().isPrimitive()) {
                at += "return ";
                if ("boolean".equals(m.getReturnType().getFullName())) {
                    pt += "Boolean resPrimitive = (Boolean) ";
                }
                else {
                    pt += "Number resPrimitive = (Number) ";
                }
            }
            else {
                at += "return ";
                pt += "return (" + pty + ") ";
            }
        }
        at += "facade." + m.getName() + "(";
        pt += "remoteInvoker.invoke(remoteLocation + \"" + m.getName() + "\"";
        first = true;
        for (AstVariable p : m.getParameters()) {
            if (first) {
                first = false;
            }
            else {
                at += ", ";
            }
            pt += ", ";
            if (p.getType().isId()) {
                at += JavaWriter.ID + ".valueOfFromPrimitive(" + p.getName() + ")";
            }
            else if (p.getType().isEnum()) {
                enumTypes.add(p.getType().getQualifiedName());
                at += getEnumTypeConstant(p.getType().getQualifiedName()) + ".<" + p.getType().getQualifiedName() + ">enumFromAmf(" + p.getName() + ")";
            }
            else if (p.getType().isDataList()) {
                model.warning("Flex param cannot be DataList");
                //TO-DO Convert datalist
            }
            else {
                at += p.getName();
            }
            pt += p.getName();
        }
        at += ");";
        aw.writeln(at);
        pt += ");";
        pw.writeln(pt);
        if (isReturningCollection) {
            aw.writeln("\t\treturn resCollection == null ? null : resCollection.toArray();");
        }
        else if (m.getReturnType().isId()) {
            pw.writeln("\t\treturn resPrimitive == null ? 0L : resPrimitive.longValue();");
        }
        else if (m.getReturnType().isPrimitive() && !"void".equals(m.getReturnType().getFullName())) {
            pw.writeln("\t\treturn resPrimitive == null ? " + m.getReturnType().getDefaultValue() + " : resPrimitive." + m.getReturnType().getFullName() + "Value();");
        }
        aw.writeln("\t}");
        pw.writeln("\t}");
    }

    private String getEnumTypeConstant(String e) {
        return "ENUM_" + e.replace('.', '_');
    }

    void process(ModelProcessor mp) {
        for (AstClass c : model.getParsedClasses()) {
            for (AstClassAnnotation a : c.getAnnotations()) {
                //System.out.println(a.getQualifiedName());
                if (JavaWriter.SERVICE_CLASS.equals(a.getQualifiedName())) {
                    //System.out.println("Service: " + c.getQualifiedName());
                    processService(mp, c, a);
                }
            }
        }
    }
}
