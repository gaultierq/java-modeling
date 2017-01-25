package io.gaultier.modeling.tool.generate;

import java.io.*;

import io.gaultier.modeling.util.base.*;

public class JavaWriter {

    static final String MODEL_AMF = "io.gaultier.api.amf.ModelAmf";
    static final String SERVICE_AMF = "io.gaultier.api.amf.ServiceAmf";
    static final String REMOTE_INVOKER = "io.gaultier.api.amf.RemoteInvoker";
    static final String ID = "io.gaultier.modeling.model.Id";
    static final String ABSTRACT_DAO = "io.gaultier.modeling.model.dao.AbstractDao";
    static final String DATA_DEFINITION = "io.gaultier.modeling.model.data.DataDefinition";
    static final String DATA_LIST = "io.gaultier.modeling.model.data.DataList";
    //static final String DATA_LIST = "java.util.List";
    static final String FIELD_DEFINITION = "io.gaultier.modeling.model.data.FieldDefinition";
    static final String FIELD_TYPE = "io.gaultier.modeling.model.data.FieldType";
    static final String MODEL_DATA = "io.gaultier.modeling.model.data.ModelData";
    static final String JOIN_DEFINITION = "io.gaultier.modeling.model.db.JoinDefinition";
    static final String STATEMENT_LOCATION = "io.gaultier.modeling.model.db.StatementLocation";

    static final String SERVICE_CLASS = "io.gaultier.modeling.service.ServiceClass";
    static final String SERVICE_REPOSITORY = "io.gaultier.modeling.service.ServiceRepository";
    static final String DATA_SERIALIZER = "io.gaultier.modeling.service.DataSerializer";
    static final String AMF_SERIALIZER = "io.gaultier.modeling.service.amf.AmfSerializer";
    static final String BOT_LIMITER = "io.gaultier.modeling.app.monitoring.BotLimiter";

    protected SourceModel model;
    protected String packageName;
    protected String className;
    protected StringBuilder head = new StringBuilder();
    protected StringBuilder body = new StringBuilder();

    JavaWriter(SourceModel m, String p, String n) {
        model = m;
        packageName = p;
        className = n;
    }

    JavaWriter write(String t) {
        body.append(t);
        return this;
    }

    JavaWriter writeln(String t) {
        write(t);
        ln();
        return this;
    }

    JavaWriter writeln(String t, int tab) {
        for (int i = 1; i <= tab; i++) {
            write("\t");
        }
        write(t);
        ln();
        return this;
    }

    JavaWriter ln() {
        write("\n");
        return this;
    }

    JavaWriter writeHeader(String s) {
        head.append(s + "\n");
        return this;
    }

    void output() {
        File f = makeFileName();
        //System.out.println("Out: " + f);
        f.delete();
        try {
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(FileUtils.stringToBytes(head.toString() + body.toString(), FileUtils.UTF_8));
            fos.close();
        }
        catch (IOException e) {
            throw new WrappedException(e);
        }
    }

    protected File makeFileName() {
        File dir = model.getTargetRoot();
        if (!packageName.isEmpty()) {
            dir = new File(dir, packageName.replace('.', '/'));
        }
        return model.makeFile(dir, className + ".java");
    }

    static String toFieldName(String clName) {
        return clName.substring(0, 1).toLowerCase() + clName.substring(1);
    }

    public static String toConstantName(String n, boolean cl) {
        if (cl) {
            assert !Character.isLowerCase(n.charAt(0)) : n;
        }
        //String o = n;
        for (int i = 0; i < n.length(); i++) {
            char c = n.charAt(i);
            if (!Character.isLowerCase(c)) {
                if (i == 0 && cl)  {
                    n = Character.toLowerCase(c) + n.substring(i + 1);
                } else {
                    n = n.substring(0, i) + '_' + Character.toLowerCase(c) + n.substring(i + 1);
                    i++;
                }
            }
        }
        n = n.toUpperCase();
        //assert cl || toJavaName(n).equals(o) : o  + "!=" + toJavaName(n);
        return n;
    }

    public void generated() {
        writeln("/* Generated class do not edit */");
    }
}
