package io.gaultier.modeling.tool.generate;

import java.io.*;
import java.util.*;

import io.gaultier.modeling.model.data.*;

public class ModelClass implements Comparable<ModelClass> {

    private static final String SUFFIX = "Data";

    ModelProcessor proc;
    private String packageName;
    private String dataName;
    private String baseName;
    private List<ModelField> fields = new ArrayList<ModelField>();
    private String tableName;
    private boolean amf;
    private boolean json;
    private boolean local;
    private String location;

    public ModelClass parse(ModelProcessor p, AstClass c, AstClassAnnotation a) {
		String name = c.getSimpleName();
        if (!name.endsWith(SUFFIX)) {
            p.model.warning(c.getQualifiedName() + " is marked as data");
            return null;
        }
        this.proc = p;
        this.packageName = c.getPackageName();
        this.dataName = name;
        this.baseName = name.substring(0, name.length() - SUFFIX.length());
        //System.out.println(dataName);
		this.tableName = a.getString("table");
		if (this.tableName.isEmpty()) {
		    this.tableName = null;
		}
		this.location = a.getString("location");
		if (this.location.isEmpty()) {
		    this.location = null;
		}
		this.amf = a.getBoolean("amf");
		this.json = a.getBoolean("json");
		this.local = a.getBoolean("local");
		assert !this.local || (!this.amf && !this.json);
		
		for (AstClassAnnotation f : a.getAnnotations("fields")) {
		    ModelField field = new ModelField(this, this.fields.size());
		    field.parse(f);
		    this.fields.add(field);
		}
        return this;
	}

    boolean isPersisted() {
        return tableName != null;
    }

    boolean hasDao() {
        return isPersisted() && proc.model.getClass(getDaoPackage() + "." + getDaoName()) != null;
    }

    boolean isAmf() {
        return amf;
    }

    boolean isLocal() {
        return local;
    }

    boolean isJson() {
        return json;
    }

    boolean isIpad() {
        return isAmf(); //TODO Ipad
    }

    Collection<ModelField> getFields() {
        return fields;
    }

    ModelField getField(String name) {
        for (ModelField f : fields) {
            if (name.equals(f.getName())) {
                return f;
            }
        }
        return null;
    }

    List<ModelField> getPrimaryKey() {
        List<ModelField> pk = new ArrayList<ModelField>();
        for (ModelField f : fields) {
            if (f.getPrimaryKey() != PrimaryKeyType.NO) {
                pk.add(f);
            }
        }
        return pk;
    }

    String getBaseName() {
        return baseName;
    }

    String getDataName() {
        return dataName;
    }

    String getPackageName() {
        return packageName;
    }

    String getQualifiedName() {
        return packageName + "." + dataName;
    }

    private String getStubName() {
        return baseName + "Stub";
    }

    private String getDaoName() {
        return baseName + "Dao";
    }

    private String getDaoPackage() {
        String p = packageName;
        String pat = ".model";
        if (p.endsWith(pat)) {
            p = p.substring(0, p.length() - pat.length()) + ".dao";
        }
        return p;
    }

    private String getDaoStubName() {
        return getDaoName() + "Stub";
    }

    private String getDaoStubPackage() {
        return getDaoPackage();
    }

    String getFlexStubName() {
        return baseName + "Data";
    }

    String getApiAmfStubName() {
        return baseName + "Amf";
    }

    String getIpadStubName() {
        return baseName + "Data";
    }

    String getHtmlStubName() {
        return baseName + "Stub";
    }

    

    String getApiAmfStubQualifiedName() {
        return getApiAmfPackage() + "." + getApiAmfStubName();
    }

    String getFlexPackage() {
        return getClientPackage(packageName, true, ".pearlTree.io.object.");
    }

    String getIpadPackage() {
        return getClientPackage(packageName, false, "");
    }

    String getApiAmfPackage() {
        return getApiAmfPackage(packageName);
    }

    static String getApiAmfPackage(String pack) {
        return getClientPackage(pack, true, ".api.amf.");
    }

    private static String getClientPackage(String pack, boolean includeOysterPrefix, String subRoot) {
        String pat = ".model";
        if (pack.endsWith(pat)) {
            pack = pack.substring(0, pack.length() - pat.length());
        }

        pat += '.';
        for (int i; (i = pack.indexOf(pat)) >= 0;) {
            pack = pack.substring(0, i) + pack.substring(i + pat.length() - 1);
        }

        pat = ".oyster.";
        int i = pack.indexOf(pat);
        if (i >= 0) {
            pack = (includeOysterPrefix ? pack.substring(0, i) : "") + subRoot + pack.substring(i + pat.length());
        }
        return pack;
    }

    String getAmfAlias() {
        return baseName;
    }

    String getDefinitionConstant() {
        return "DEFINITION";
    }

    void write() {
        JavaWriter w = new JavaWriter(proc.model, packageName, getStubName());

        w.writeln("package " + packageName + ";");
        w.generated();
        w.write("public abstract class " + getStubName() + " extends " + JavaWriter.MODEL_DATA);

        w.write("<" + getQualifiedName() + ">");
//        if (!isLocal()) { 
//            w.write(" implements " + Externalizable.class.getName());
//        }
        w.writeln(" {");
        //System.out.println(getStubName());
        writeDef(w);
        for (ModelField f : fields) {
            System.out.println(f.getName());
            f.writeDef(w);
        }
        
        writeInit(w);

//        writeDaoConstant(w);
        writeForeignConstants(w);

        writeConstructor(w);
        writeGetDef(w);
//        writeGetDao(w);
//        writeSerialize(w);
        
//        for (ModelField f : fields) {
//            f.writeField(w);
//        }
        
        for (ModelField f : fields) {
        	f.writeAccessorsOld(w);
        }
        //writeSerialize(w);
        //writeToString(w);

        w.writeln("}");
        w.output();
    }

	private void writeDef(JavaWriter w) {
        w.writeln("public static final " + JavaWriter.DATA_DEFINITION + "<" + getQualifiedName() + "> " + getDefinitionConstant() +
                " = " + JavaWriter.DATA_DEFINITION + ".create(" +
                getQualifiedName() + ".class, " +
                (tableName == null ? "null" : "\"" + tableName + "\"") +
                ")" + (location == null ? "" : ".overrideLocations(" + JavaWriter.STATEMENT_LOCATION + "." + location + ")") + ";");
    }

	private void writeGetDef(JavaWriter w) {
        w.writeln("@" + Override.class.getName());
        w.writeln("public final " + JavaWriter.DATA_DEFINITION + "<" + getQualifiedName() + "> getDefinition() {");
        w.writeln("return " + getDefinitionConstant() + ";");
        w.writeln("}");
    }

    private void writeInit(JavaWriter w) {
    }

    @SuppressWarnings("unused")
	private void writeDaoConstant(JavaWriter w) {
        if (!isPersisted()) {
            return;
        }
        String type = hasDao() ? getDaoPackage() + "." + getDaoName() : getDaoStubPackage() + "." + getDaoStubName();
        w.writeln("public static final " + type + " DAO = new " + type + "();");
    }

    @SuppressWarnings("unused")
	private void writeGetDao(JavaWriter w) {
        if (!isPersisted()) {
            return;
        }
        w.writeln("@" + Override.class.getName());
        w.writeln("public final " + JavaWriter.ABSTRACT_DAO + "<" + getQualifiedName() + "> getDao() {");
        w.writeln("return DAO;");
        w.writeln("}");
    }

	private void writeForeignConstants(JavaWriter w) {
        if (!isPersisted()) {
            return;
        }
        for (ModelField f : fields) {
            f.writeForeignConstant(w);
        }
    }

    private void writeConstructor(JavaWriter w) {
        w.writeln("public " + getStubName() + "() {");
        w.writeln("super(" + fields.size() + ");");
        w.writeln("}");
    }

    @SuppressWarnings("unused")
	private void writeSerialize(JavaWriter w) {
        if (isLocal()) {
            return;
        }

        w.writeln("@" + Override.class.getName());
        w.writeln("public void writeExternal(" + ObjectOutput.class.getName() + " out) throws " + IOException.class.getName() + " {");
        w.writeln("prepareForSerializationSafe();");
        w.writeln(JavaWriter.DATA_SERIALIZER + ".getAmf().write" + baseName + "((" + getQualifiedName() + ") this, out);");
        w.writeln("}");

        w.writeln("@" + Override.class.getName());
        w.writeln("public void readExternal(" + ObjectInput.class.getName() + " in) throws " + IOException.class.getName() + ", " + ClassNotFoundException.class.getName() + " {");
        w.writeln(JavaWriter.DATA_SERIALIZER + ".getAmf().read" + baseName + "((" + getQualifiedName() + ") this, in);");
        w.writeln("}");
    }

    /*private void writeToString(JavaWriter w) {
        w.writeln("@" + Override.class.getName());
        w.writeln("public " + String.class.getName() + " toString() {");
        w.writeln(StringBuilder.class.getName() + " b = new " + StringBuilder.class.getName() + "();");
        w.writeln("b.append(super.toString()).append('{');");
        boolean first = true;
        for (ModelField f : fields) {
            w.write("b");
            if (first) {
                first = false;
            } else {
                w.write(".append(\", \")");
            }
            f.writeToString(w);
        }
        w.writeln("b.append('}');");
        w.writeln("return b.toString();");
        w.writeln("}");
    }*/

    void writeDao() {
        if (!isPersisted()) {
            return;
        }
        JavaWriter w = new JavaWriter(proc.model, getDaoStubPackage(), getDaoStubName());

        w.writeln("package " + getDaoStubPackage() + ";");
        w.generated();
        w.writeln("public" + (hasDao() ? " abstract" : "") + " class " + getDaoStubName() + " extends " + JavaWriter.ABSTRACT_DAO + "<" + getQualifiedName() + "> {");
        writeDaoMethods(w);
        w.writeln("}");
        w.output();
    }

    private void writeDaoMethods(JavaWriter w) {
        if (!isPersisted()) {
            return;
        }
        writeDaoSelect(w);
        writeDaoInsert(w);
        writeDaoUpdate(w);
        writeDaoDelete(w);
    }

    private String getDaoHelper() {
        return "helper()";
    }

    private void writeDaoSelect(JavaWriter w) {
        w.writeln("public void select(" + getQualifiedName() + " data) {");
        w.writeln("select(data, true, false);");
        w.writeln("}");

        w.writeln("public boolean select(" + getQualifiedName() + " data, boolean mustHave, boolean forUpdate) {");
        w.writeln("return select(null, data, mustHave, forUpdate);");
        w.writeln("}");

        w.writeln("public boolean select(" + JavaWriter.STATEMENT_LOCATION + " loc, " + getQualifiedName() + " data, boolean mustHave, boolean forUpdate) {");
        w.writeln("return " + getDaoHelper() + ".selectThis(loc, data, mustHave, forUpdate, null);");
        w.writeln("}");
    }

    private void writeDaoInsert(JavaWriter w) {
        w.writeln("public void insert(" + getQualifiedName() + " data) {");
        w.writeln("" + getDaoHelper() + ".insert(data);");
        w.writeln("}");
    }

    private void writeDaoUpdate(JavaWriter w) {
        w.writeln("public void update(" + getQualifiedName() + " data) {");
        w.writeln("update(data, true);");
        w.writeln("}");

        w.writeln("public boolean update(" + getQualifiedName() + " data, boolean mustHave) {");
        w.writeln("return " + getDaoHelper() + ".update(data, mustHave);");
        w.writeln("}");
    }

    private void writeDaoDelete(JavaWriter w) {
        w.writeln("public void delete(" + getQualifiedName() + " data) {");
        w.writeln("delete(data, true);");
        w.writeln("}");

        w.writeln("public boolean delete(" + getQualifiedName() + " data, boolean mustHave) {");
        w.writeln("return " + getDaoHelper() + ".delete(data, mustHave);");
        w.writeln("}");
    }

    /*private List<ModelField> getPrimaryKey() {
        List<ModelField> pk = new ArrayList<ModelField>();
        for (ModelField f : fields) {
            if (f.getPrimaryKey() != PrimaryKeyType.NO) {
                pk.add(f);
            }
        }
        return pk;
    }*/

    void writeFlex() {
        if (!isAmf()) {
            return;
        }

        FlexWriter w = new FlexWriter(proc.model, getFlexPackage(), getFlexStubName());

        w.writeln("package " + getFlexPackage() + " {");
        w.ln();

        for (ModelField f : fields) {
            f.writeFlexImport(w);
        }

        w.writeln("import flash.utils.IExternalizable;");
        w.writeln("import flash.utils.IDataInput;");
        w.writeln("import flash.utils.IDataOutput;");
        w.ln();
        w.writeln("[RemoteClass(alias=\"" + getAmfAlias() + "\")]");
        w.writeln("public class " + getFlexStubName() + " implements IExternalizable {");
        w.ln();
        //w.writeln("public static var VERSION:String = \"" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "\";");
        //w.ln();

        for (ModelField f : fields) {
            f.writeFlexField(w);
        }
        w.ln();

        for (ModelField f : fields) {
            f.writeFlexAccessors(w);
        }
        writeFlexSerialize(w);

        w.writeln("}");
        w.writeln("}");
        w.output();
    }

    private void writeFlexSerialize(FlexWriter w) {
        if (!isAmf()) {
            return;
        }

        w.ln();
        w.writeln("public function readExternal(input:IDataInput):void {");
        for (ModelField f : fields) {
            f.writeFlexReadObject(w);
        }
        w.writeln("}");

        w.ln();
        w.writeln("public function writeExternal(output:IDataOutput):void {");
        for (ModelField f : fields) {
            f.writeFlexWriteObject(w);
        }
        w.writeln("}");
    }

    void writeApiAmf() {
        if (!isAmf()) {
            return;
        }

        JavaWriter w = new JavaWriter(proc.model, getApiAmfPackage(), getApiAmfStubName());

        w.writeln("package " + getApiAmfPackage() + ";");
        w.ln();
        w.generated();
        w.writeln("public final class " + getApiAmfStubName() + " extends " + JavaWriter.MODEL_AMF + " implements " + Externalizable.class.getName() + " {");
        w.ln();
        //w.writeln("public static var VERSION:String = \"" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "\";");
        //w.ln();

        for (ModelField f : fields) {
            f.writeApiAmfField(w);
        }

        writeApiAmfSerialize(w);

        w.writeln("}");
        w.output();
    }

    private void writeApiAmfSerialize(JavaWriter w) {
        if (!isAmf()) {
            return;
        }

        w.ln();
        /*boolean hasList = false;
        for (ModelField f : fields) {
            hasList |= f.isAmf() && f.isList();
        }
        if (hasList) {
            w.writeln("@SuppressWarnings(\"unchecked\")");
        }*/
        w.writeln("@Override");
        w.writeln("public void readExternal(" + ObjectInput.class.getName() + " input) throws " + IOException.class.getName() + ", " + ClassNotFoundException.class.getName() + " {");
        for (ModelField f : fields) {
            f.writeApiAmfReadObject(w);
        }
        w.writeln("}");

        w.ln();
        w.writeln("@Override");
        w.writeln("public void writeExternal(" + ObjectOutput.class.getName() + " output) throws " + IOException.class.getName() + " {");
        for (ModelField f : fields) {
            f.writeApiAmfWriteObject(w);
        }
        w.writeln("}");
    }

    void writeIpad() {
        if (!isIpad()) {
            return;
        }
        Collection<ModelClass> refs = new TreeSet<ModelClass>();
        for (ModelField f : fields) {
            if (!f.isIpad()) {
                continue;
            }
            ModelClass c = f.getObjectModel();
            if (c != null && !equals(c)) {
                refs.add(c);
            }
        }
        writeIpadH(refs);
        writeIpadM(refs);
    }

    private void writeIpadH(Collection<ModelClass> refs) {
        IpadWriter w = new IpadWriter(proc.model, getIpadPackage(), getIpadStubName(), true);

        if (!refs.isEmpty()) {
            w.ln();
            for (ModelClass c : refs) {
                w.writeln("@class " + c.getIpadStubName() + ";");
            }
            w.ln();
        }

        w.writeln("#import <Foundation/Foundation.h>");
        w.ln();
        w.writeln("@interface " + getIpadStubName() + " : NSObject <NSCoding> {");
        //w.writeln("@interface " + getIpadStubName() + " : NSObject <NSCoding, NSCopying> {");
        w.ln();

        w.writeln("@private");
        for (ModelField f : fields) {
            f.writeIpadFieldH(w);
        }
        w.writeln("}");
        w.ln();

        for (ModelField f : fields) {
            f.writeIpadPropertyH(w);
        }
        w.ln();

        w.writeln("@end");
        w.output();
    }

    private void writeIpadM(Collection<ModelClass> refs) {
        IpadWriter w = new IpadWriter(proc.model, getIpadPackage(), getIpadStubName(), false);

        w.writeln("#import \"" + getIpadStubName() + ".h\"");
        w.writeln("#import \"AMFUnarchiver.h\"");
        w.writeln("#import \"AMFArchiver.h\"");
        for (ModelClass c : refs) {
            w.writeln("#import \"" + c.getIpadStubName() + ".h\"");
        }
        w.ln();

        w.writeln("@implementation " + getIpadStubName());
        for (ModelField f : fields) {
            f.writeIpadSynthesizeM(w);
        }
        w.ln();

        w.writeln("-(void) dealloc {");
        for (ModelField f : fields) {
            f.writeIpadDeallocM(w);
        }
        w.writeln("\t[super dealloc];");
        w.writeln("}");
        w.ln();

        w.writeln("- (id)initWithCoder:(NSCoder *)coder");
        w.writeln("{");
        w.writeln("\tself = [super init];");
        w.writeln("\tif ([coder isKindOfClass: [AMFUnarchiver class]]) {");
        w.writeln("\t\tAMFUnarchiver* amfUnarchiver = (AMFUnarchiver*) coder;");
        for (ModelField f : fields) {
            f.writeIpadDecodeM(w);
        }
        w.writeln("\t}");
        w.writeln("\telse if ([coder isKindOfClass:[NSKeyedUnarchiver class]]) {");
        for (ModelField f : fields) {
            f.writeIpadKeyedDecodeM(w);
        }
        w.writeln("\t}");
        w.writeln("\treturn self;");
        w.writeln("}");
        w.ln();

        w.writeln("- (void)encodeWithCoder:(NSCoder *)encoder");
        w.writeln("{");
        w.writeln("\tif ([encoder isKindOfClass: [AMFArchiver class]]) {");
        w.writeln("\t\tAMFArchiver* amfArchiver = (AMFArchiver*) encoder;");
        for (ModelField f : fields) {
            f.writeIpadEncodeM(w);
        }
        w.writeln("\t}");
        w.writeln("\telse if ([encoder isKindOfClass:[NSKeyedArchiver class]]) {");
        for (ModelField f : fields) {
            f.writeIpadKeyedEncodeM(w);
        }
        w.writeln("\t}");
        w.writeln("}");
        w.ln();

        w.writeln("@end");
        w.output();
    }


    public void writeJsonStub() {
//        if (!isAmf()) {
//            return;
//        }
//        HtmlWriter h = new HtmlWriter(proc.model, "data", getHtmlStubName().toLowerCase());
//        h.writeHeader("/// <reference path=\"" + HtmlWriter.RELATIVE_PATH + "gen/ts-definition/modeldata.d.ts\" />");
//        h.writeln("export class " + getHtmlStubName() + " implements " + HtmlSingleFileWriter.MODULE_NAME + "." + getHtmlInterfaceName() + " { ");
//        h.writeln("");
//        for (ModelField f : fields) {
//            f.writeJsonInterface(h, false);
//        }
//        h.writeln("");
//        h.writeln("}");
//        h.output();
    }

    @Override
    public int compareTo(ModelClass o) {
        return getQualifiedName().compareTo(o.getQualifiedName());
    }
}
