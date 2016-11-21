package io.gaultier.modeling.tool.generate;

import java.util.Date;
import java.util.List;

import io.gaultier.modeling.model.Id;
import io.gaultier.modeling.model.Version;
import io.gaultier.modeling.model.data.DataField;
import io.gaultier.modeling.model.data.DataType;
import io.gaultier.modeling.model.data.PrimaryKeyType;
import io.gaultier.modeling.model.data.PrimitiveSubstitution;
import io.gaultier.modeling.model.data.Syntax;
import io.gaultier.modeling.util.base.ByteArray;

public class ModelField {

	private ModelClass clazz;
	private int index;

	private String name;
	private DataType type;
	private AstClass enumType;
	private AstClass object;
	private AstClass list;

	private boolean persistent;
	private String columnName;
	private PrimaryKeyType primaryKey;

	private boolean amf;
	private boolean json;
	private boolean html;
	private PrimitiveSubstitution substitution;
	private Syntax[] syntaxes;

	private boolean deprecated;

	private String javaType;
	private String flexType;

	ModelField(ModelClass c, int i) {
		clazz = c;
		index = i;
	}

	void parse(AstClassAnnotation f) {
		name = f.getString("name");
		type = f.getEnum("type");
		enumType = f.getClass("enumType");
		object = f.getClass("object");
		list = f.getClass("list");
		//System.out.println(name + " type: " + type);

		persistent = f.getBoolean("persistent");
		columnName = f.getString("column");
		primaryKey = f.getEnum("primaryKey");

		deprecated = "deprecated".equals(f.getString("attr"));

		amf = f.getBoolean("amf");
		json = f.getBoolean("json");
		html = f.getBoolean("html");
		substitution = f.getEnum("substitution");
		//System.out.println(name + "/" + columnName);

		//System.out.println(name + " type=" + type + " enum=" + enumType + " pk=" + primaryKey);
		if (enumType.getQualifiedName().equals(DataType.class.getName())) {
			// Not an enum
			enumType = null;
		}
		if (object.getQualifiedName().equals(DataField.class.getName())) {
			object = null;
		}
		if (list.getQualifiedName().equals(DataField.class.getName())) {
			list = null;
		}
		if (columnName.isEmpty()) {
			columnName = null;
		}
		if (object != null || list != null) {
			persistent = false;
		}
		if (object != null || list != null || enumType != null) {
			type = null;
		}

		//assert columnName == null || persistent : name + " has column but no persistance";
	}

	String getName() {
		return name;
	}

	boolean isDeprecated() {
		return deprecated;
	}

	boolean isAmf() {
		return amf && clazz.isAmf();
	}

	boolean isJson() {
		return json && clazz.isJson();
	}

	boolean isIpad() {
		return isAmf() && clazz.isIpad(); //TODO Ipad
	}

	boolean isList() {
		return list != null;
	}

	public ModelClass getObjectModel() {
		if (object == null) {
			return null;
		}
		return clazz.proc.getModelClass(object);
	}

	PrimaryKeyType getPrimaryKey() {
		return primaryKey;
	}

	String getConstantName() {
		return JavaWriter.toConstantName(name, false);
	}

	String getJavaType() {
		if (javaType == null) {
			javaType = findJavaType();
		}
		return javaType;
	}

	private String findJavaType() {
		if (list != null) {
			return JavaWriter.DATA_LIST + "<" + list.getFullName() + ">";
		}
		if (object != null) {
			return object.getFullName();
		}
		if (enumType != null) {
			return enumType.getFullName();
		}
		switch (type) {
		case STRING:
			return String.class.getName();
		case INTEGER:
			return Integer.class.getName();
		case LONG:
			return Long.class.getName();
		case DOUBLE:
			return Double.class.getName();
		case DATETIME:
			return Date.class.getName();
		case BINARY:
			return ByteArray.class.getName();
		case ID:
			return Id.class.getName();
		case VERSION:
			return Version.class.getName();
		case BOOLEAN:
			return Boolean.class.getName();
		}
		assert false : type;
		return null;
	}

	String getFlexType() {
		if (flexType == null) {
			flexType = findFlexType();
		}
		return flexType;
	}

	private String findFlexType() {
		if (list != null) {
			return "Array";
			//return "mx.collections.ArrayCollection";
		}
		if (object != null) {
			ModelClass c = clazz.proc.getModelClass(object);
			if (c != null) {
				assert c.isAmf() : c.getQualifiedName();
				return c.getFlexPackage() + "." + c.getFlexStubName();
			}
			return object.getFullName();
		}
		if (enumType != null) {
			return "int";
		}
		switch (type) {
		case STRING:
			return "String";
		case INTEGER:
			return "int";
		case LONG:
			return "int";
		case DOUBLE:
			return "Number";
		case DATETIME:
			return "Number";
		case BINARY:
			return "flash.utils.ByteArray";//"ByteArray";
		case ID:
			return "int";
		case VERSION:
			return "int";
		}
		assert false : type;
		return null;
	}

	private String getIpadType() {
		if (list != null) {
			return "NSMutableArray *";
			//return "mx.collections.ArrayCollection";
		}
		if (object != null) {
			ModelClass c = clazz.proc.getModelClass(object);
			if (c != null) {
				assert c.isIpad() : c.getQualifiedName();
				return c.getIpadStubName() + " *"; //TODO Import
			}
			//assert false : object.getFullName();
			return "<invalid object " + object.getFullName() + "> ";
		}
		if (enumType != null) {
			return "int ";
		}
		switch (type) {
		case STRING:
			return "NSString *";
		case INTEGER:
		case LONG:
		case ID:
		case VERSION:
			return "int ";
		case DOUBLE:
		case DATETIME:
			return "double ";
		case BINARY:
			return "NSData *";
		}
		assert false : type;
		return null;
	}

	String getApiAmfType() {
		if (list != null) {
			ModelClass c = clazz.proc.getModelClass(list);
			String n;
			if (c != null) {
				n = c.getApiAmfPackage() + "." + c.getApiAmfStubName();
			}
			else {
				n = list.getFullName();
			}
			return List.class.getName() + "<" + n + ">";
			//return "mx.collections.ArrayCollection";
		}
		if (object != null) {
			ModelClass c = clazz.proc.getModelClass(object);
			if (c != null) {
				return c.getApiAmfPackage() + "." + c.getApiAmfStubName();
			}
			return object.getFullName();
		}
		if (enumType != null) {
			return "int";
		}
		switch (type) {
		case STRING:
			return "String";
		case INTEGER:
			return "int";
		case LONG:
			return "int";
		case DOUBLE:
			return "double";
		case DATETIME:
			return "double";
		case BINARY:
			return "byte[]";//"ByteArray";
		case ID:
			return "int";
		case VERSION:
			return "int";
		}
		assert false : type;
		return null;
	}

	/*private String convertToAmf(String ex) {
        if (list != null) {
            return "convertToList((" + List.class.getName() + "<?>) " + ex + ")";
        }
        return ex;
    }

    private String convertFromAmf(String ex) {
        if (list != null) {
            return ex;
        }
        if (object != null) {
            ModelClass c = clazz.proc.getModelClass(object);
            if (c != null) {
                return ex;
            }
            return ex;
        }
        if (enumType != null) {
            return ex;
        }
        switch (type) {
        case STRING:
            return ex;
        case INTEGER:
            return ex;
        case LONG:
            return "convertFromLong((" + Integer.class.getName() + ") " + ex + ")";
        case DOUBLE:
            return ex;
        case DATETIME:
            return ex;
        case BINARY:
            return ex;
        }
        assert false : type;
        return null;
    }*/

	private void writeDeprecated(JavaWriter w) {
		if (isDeprecated()) {
			w.writeln("@" + Deprecated.class.getName());
		}
	}

	void writeDef(JavaWriter w) {
		writeDeprecated(w);
		w.write("public static final " + JavaWriter.FIELD_DEFINITION +
				"<" + getJavaType() + ", " + clazz.getQualifiedName() + ">" +
				" " + getConstantName() + " = " +
				clazz.getDefinitionConstant() + ".<" + getJavaType() + ">createField(" +
				index + ", " +
				"\"" + name + "\", " +
				(type == null ? "null" : DataType.class.getName() + "." + type.name()) + ", " +
				(enumType == null ? "null" : enumType.getQualifiedName() + ".class") + ", " +
				(list != null) + ", " +
				(object == null ? list == null ? "null" : list.getQualifiedName() + "." + clazz.getDefinitionConstant() : object.getQualifiedName() + "." + clazz.getDefinitionConstant()) + ", " +
				persistent + ", " +
				(columnName == null ? "\"" + name + "\"" : "\"" + columnName + "\"") + ", "
				+ isAmf() + ", "
				+ json + ", "
				+ html + ", " +
				PrimitiveSubstitution.class.getName() + "." + substitution.name() + ", "
				+ Syntax.encode(syntaxes)
				+ ")");
		if (primaryKey != PrimaryKeyType.NO) {
			w.write(".setPrimaryKey(" + PrimaryKeyType.class.getName() + "." + primaryKey.name() + ")");
		}
		w.writeln(";");
	}

	void writeForeignConstant(JavaWriter w) {
		if (object != null) {
			ModelClass pkClass = clazz.proc.getModelClass(object);
			if (!pkClass.isPersisted()) {
				return;
			}
			ModelField fkField = clazz.getField(getName() + "Id");
			if (fkField == null) {
				return;
			}
			assert pkClass.getPrimaryKey().size() == 1;
			ModelField pkField = pkClass.getPrimaryKey().get(0);
			assert fkField.type == pkField.type : clazz.getQualifiedName() + "." + fkField.getName() + " " + fkField.type + " != " + pkClass.getQualifiedName() + "." + pkField.getName() + " " + pkField.type;
			if (pkField.isDeprecated()) {
				pkField.writeDeprecated(w);
			}
			else {
				fkField.writeDeprecated(w);
			}
			w.writeln("public static final " + JavaWriter.JOIN_DEFINITION +
					"<" + clazz.getQualifiedName() + ", " + pkClass.getQualifiedName() + ">" +
					" " + getConstantName() + "_JOIN = new " + JavaWriter.JOIN_DEFINITION +
					"<" + clazz.getQualifiedName() + ", " + pkClass.getQualifiedName() + ">" +
					"(" + clazz.getQualifiedName() + "." + getConstantName() +
					").clause(" + pkClass.getQualifiedName() + "." + pkField.getConstantName() +
					", " + clazz.getQualifiedName() + "." + fkField.getConstantName() + ");");
		}
	}

	void writeAccessors(JavaWriter w) {
		writeDeprecated(w);
		if (getJavaType().indexOf('<') >= 0) {
			//w.writeln("@" + SuppressWarnings.class.getName() + "(\"unchecked\")");
		}
		w.writeln("public final " + getJavaType() + " get" + toMethod(name) + "() {");
		w.writeln("return " + toField(name) + ";");
		w.writeln("}");

		writeDeprecated(w);
		w.writeln("public final void set" + toMethod(name) + "(" + getJavaType() + " " + name + ") {");
		w.writeln(toField(name) + " = " + name + ";");
		w.writeln("}");
	}

	void writeField(JavaWriter w) {
		writeDeprecated(w);
		if (getJavaType().indexOf('<') >= 0) {
			//w.writeln("@" + SuppressWarnings.class.getName() + "(\"unchecked\")");
		}
		w.writeln("private " + getJavaType() + " " + toField(name) + ";");
	}

	void writeAccessorsOld(JavaWriter w) {

		//getter
		writeDeprecated(w);
		if (((String) getJavaType()).indexOf('<') >= 0) {
			w.writeln("@" + SuppressWarnings.class.getName() + "(\"unchecked\")");
		}
		w.writeln("public " + getJavaType() + " get" + toMethod(name) + "() {");
		w.writeln("return " + getValueAccessor("getValue(" + index + ")") + ";");
		w.writeln("}");


		//setter
		writeDeprecated(w);
		w.writeln("public void set" + toMethod(name) + "(" + getJavaType() + " " + name + ") {");
		w.writeln("setValue(" + index + ", " + name + ");");
		w.writeln("}");

		//edit
		writeDeprecated(w);
        w.writeln("@" + SuppressWarnings.class.getName() + "(\"unchecked\")");
        String editJavaType = findEditJavaType();
        w.writeln("public " + editJavaType + " edit" + toMethod(name) + "() {");
		w.writeln("return" + "(" + editJavaType + ")" + " getEditValue(" + index + ");");
		w.writeln("}");
	}

	private String findEditJavaType() {
		String editJavaType;
		if (isList()) {
			editJavaType = "android.databinding.ObservableList<" + list.getFullName() + ">";
		}
		else if (object != null) {
			editJavaType = object.getFullName();
		}
		else {
			editJavaType = "android.databinding.ObservableField<" + getJavaType() + ">";
		}
		return editJavaType;
	}

	void writeFieldOld(JavaWriter w) {
		writeDeprecated(w);
		if (getJavaType().indexOf('<') >= 0) {
			//w.writeln("@" + SuppressWarnings.class.getName() + "(\"unchecked\")");
		}
		w.writeln("private " + getJavaType() + " " + toField(name) + ";");
	}

	private String getValueAccessor(String value) {
		if (type != null) {
			switch (type) {
			case BOOLEAN:
				return value + " == null ? Boolean.FALSE : (" + Boolean.class.getName() + ") " + value;
			}
		}
		return "(" + getJavaType() + ") " + value; //identity
	}

	/*void writeReadObject(JavaWriter w) {
        if (!isAmf()) {
            return;
        }
        w.writeln("setValue(" + index + ", " + convertFromAmf("in.readObject()") + ");");
    }*/

	/*void writeWriteObject(JavaWriter w) {
        if (!isAmf()) {
            return;
        }
        String v = "getValue(" + index + ")";
        v = convertToAmf(v);
        w.writeln("out.writeObject(" + v + ");");
    }*/

	/*void writeToString(JavaWriter w) {
        w.writeln(".append(\"" + name + "=\").append(get" + toMethod(name) + "());");
    }*/

	void writeFlexImport(FlexWriter w) {
		if (!isAmf()) {
			return;
		}
		String t = getFlexType();
		if (t.startsWith("io.gaultier.") || t.startsWith("mx.")) {
			w.writeln("import " + getFlexType() + ";");
		}
	}

	void writeFlexField(FlexWriter w) {
		if (!isAmf()) {
			return;
		}
		w.writeln("private var " + "_" + name + ":" + getFlexType() + ";");
	}

	void writeFlexAccessors(FlexWriter w) {
		if (!isAmf()) {
			return;
		}

		w.writeln("public function get " + name + "():" + getFlexType() + " { return " + "_" + name + "; }");

		w.writeln("public function set " + name + "(value:" + getFlexType() + "):void { " + "_" + name + " = value; }");

		//w.writeln("public function get " + name + "():" + getFlexType() + " { throw \"" + clazz.getFlexStubName() + ".get " + name + "\"; }");

		//w.writeln("public function set " + name + "(value:" + getFlexType() + "):void { throw \"" + clazz.getFlexStubName() + ".set " + name + "\"; }");
	}

	void writeFlexReadObject(FlexWriter w) {
		if (!isAmf()) {
			return;
		}
		if (list != null) {
			w.writeln(name + " = input.readObject() as " + getFlexType() + ";");
		}
		else if (object != null) {
			w.writeln(name + " = input.readObject() as " + getFlexType() + ";");
		}
		else if (enumType != null) {
			w.writeln(name + " = input.readInt();");
		}
		else {
			switch (type) {
			case INTEGER:
			case LONG:
			case ID:
			case VERSION:
				w.writeln(name + " = input.readInt();");
				break;
			case DOUBLE:
			case DATETIME:
				w.writeln(name + " = input.readDouble();");
				break;
			case STRING:
				w.writeln(name + " = input.readObject() as " + getFlexType() + ";");
				break;
			case BINARY:
				w.writeln(name + " = input.readObject() as " + getFlexType() + ";");
				break;
			default:
				assert false : type;
			}
		}
	}

	void writeFlexWriteObject(FlexWriter w) {
		if (!isAmf()) {
			return;
		}
		if (list != null) {
			w.writeln("output.writeObject(" + name + ");");
		}
		else if (object != null) {
			w.writeln("output.writeObject(" + name + ");");
		}
		else if (enumType != null) {
			w.writeln("output.writeInt(" + name + ");");
		}
		else {
			switch (type) {
			case INTEGER:
			case LONG:
			case ID:
			case VERSION:
				w.writeln("output.writeInt(" + name + ");");
				break;
			case DOUBLE:
			case DATETIME:
				w.writeln("output.writeDouble(" + name + ");");
				break;
			case STRING:
				w.writeln("output.writeObject(" + name + ");");
				break;
			case BINARY:
				w.writeln("output.writeObject(" + name + ");");
				break;
			default:
				assert false : type;
			}
		}
	}

	void writeIpadFieldH(IpadWriter w) {
		if (!isIpad()) {
			return;
		}
		w.writeln("\t" + getIpadType() + "_" + name + ";");
	}

	void writeIpadPropertyH(IpadWriter w) {
		if (!isIpad()) {
			return;
		}
		String type = getIpadType();
		w.writeln("@property (readwrite, " + (type.endsWith("*") ? "retain" : "assign") + ") " + type + name + ";");
	}

	void writeIpadSynthesizeM(IpadWriter w) {
		if (!isIpad()) {
			return;
		}
		w.writeln("@synthesize " + name + " = _" + name + ";");
	}

	void writeIpadDeallocM(IpadWriter w) {
		if (!isIpad()) {
			return;
		}
		String type = getIpadType();
		if (!type.endsWith("*")) {
			return;
		}
		w.writeln("\t[_" + name + " release];");
	}

	void writeIpadDecodeM(IpadWriter w) {
		if (!isIpad()) {
			return;
		}
		w.writeln("\t\tself." + name + " = " + getIpadDecoderExpr() + ";");
	}

	private String getIpadDecoderExpr() {
		if (list != null) {
			return "(id)[amfUnarchiver decodeObjectOrNil]";
		}
		if (object != null) {
			return "(id)[amfUnarchiver decodeObjectOrNil]";
		}
		if (enumType != null) {
			return "[amfUnarchiver decodeInt]";
		}
		switch (type) {
		case INTEGER:
		case LONG:
		case ID:
		case VERSION:
			return "[amfUnarchiver decodeInt]";
		case DOUBLE:
		case DATETIME:
			return "[amfUnarchiver decodeDouble]";
		case STRING:
			return "(id)[amfUnarchiver decodeObjectOrNil]";
		case BINARY:
			return "(id)[amfUnarchiver decodeObjectOrNil]";
		}
		assert false : type;
		return null;
	}

	void writeIpadKeyedDecodeM(IpadWriter w) {
		if (!isIpad()) {
			return;
		}
		w.writeln("\t\tself." + name + " = [coder " + getIpadKeyedDecoderExpr() + ":@\"" + name + "\"];");
	}

	private String getIpadKeyedDecoderExpr() {
		if (list != null) {
			return "decodeObjectForKey";
		}
		if (object != null) {
			return "decodeObjectForKey";
		}
		if (enumType != null) {
			return "decodeIntForKey";
		}
		switch (type) {
		case INTEGER:
		case LONG:
		case ID:
		case VERSION:
			return "decodeIntForKey";
		case DOUBLE:
		case DATETIME:
			return "decodeDoubleForKey";
		case STRING:
			return "decodeObjectForKey";
		case BINARY:
			return "decodeObjectForKey";
		}
		assert false : type;
		return null;
	}


	void writeIpadEncodeM(IpadWriter w) {
		if (!isIpad()) {
			return;
		}
		w.writeln("\t\t[amfArchiver " + getIpadEncoderExpr() + ":self." + name + "];");
	}

	private String getIpadEncoderExpr() {
		if (list != null) {
			return "encodeObject";
		}
		if (object != null) {
			return "encodeObject";
		}
		if (enumType != null) {
			return "encodeFixedSizeInt";
		}
		switch (type) {
		case INTEGER:
		case LONG:
		case ID:
		case VERSION:
			return "encodeFixedSizeInt";
		case DOUBLE:
		case DATETIME:
			return "encodeFixedSizeDouble";
		case STRING:
			return "encodeObject";
		case BINARY:
			return "encodeObject";
		}
		assert false : type;
		return null;
	}

	void writeIpadKeyedEncodeM(IpadWriter w) {
		if (!isIpad()) {
			return;
		}
		w.writeln("\t\t[encoder " + getIpadKeyedEncoderExpr() + ":self." + name + " forKey:@\"" + name + "\"];");
	}

	private String getIpadKeyedEncoderExpr() {
		if (list != null) {
			return "encodeObject";
		}
		if (object != null) {
			return "encodeObject";
		}
		if (enumType != null) {
			return "encodeInteger";
		}
		switch (type) {
		case INTEGER:
		case LONG:
		case ID:
		case VERSION:
			return "encodeInteger";
		case DOUBLE:
		case DATETIME:
			return "encodeDouble";
		case STRING:
			return "encodeObject";
		case BINARY:
			return "encodeObject";
		}
		assert false : type;
		return null;
	}

	//    void writeIpadCopyM(IpadWriter w) {
	//        if (!isIpad()) {
	//            return;
	//        }
	//        w.writeln("\tcopy." + name + " = self." + name + ";");
	//    }

	void writeApiAmfField(JavaWriter w) {
		if (!isAmf()) {
			return;
		}
		writeDeprecated(w);
		w.writeln("public " + getApiAmfType() + " " + name + ";");
	}

	void writeApiAmfReadObject(JavaWriter w) {
		if (!isAmf()) {
			return;
		}
		//w.writeln("try {");
		if (list != null) {
			w.writeln(name + " = asList(input.readObject());");
		}
		else if (object != null) {
			w.writeln(name + " = (" + getApiAmfType() + ") input.readObject();");
		}
		else if (enumType != null) {
			w.writeln(name + " = input.readInt();");
		}
		else {
			switch (type) {
			case INTEGER:
			case LONG:
			case ID:
			case VERSION:
				w.writeln(name + " = input.readInt();");
				break;
			case DOUBLE:
			case DATETIME:
				w.writeln(name + " = input.readDouble();");
				break;
			case STRING:
				w.writeln(name + " = (" + getApiAmfType() + ") input.readObject();");
				break;
			case BINARY:
				w.writeln(name + " = (" + getApiAmfType() + ") input.readObject();");
				break;
			default:
				assert false : type;
			}
		}
		//w.writeln("} catch (Error e) {e.printStackTrace();}");
	}

	void writeApiAmfWriteObject(JavaWriter w) {
		if (!isAmf()) {
			return;
		}
		if (list != null) {
			w.writeln("output.writeObject(fromList(" + name + "));");
		}
		else if (object != null) {
			w.writeln("output.writeObject(" + name + ");");
		}
		else if (enumType != null) {
			w.writeln("output.writeInt(" + name + ");");
		}
		else {
			switch (type) {
			case INTEGER:
			case LONG:
			case ID:
			case VERSION:
				w.writeln("output.writeInt(" + name + ");");
				break;
			case DOUBLE:
			case DATETIME:
				w.writeln("output.writeDouble(" + name + ");");
				break;
			case STRING:
				w.writeln("output.writeObject(" + name + ");");
				break;
			case BINARY:
				w.writeln("output.writeObject(" + name + ");");
				break;
			default:
				assert false : type;
			}
		}
	}


	public static String toMethod(String n) {
		return camelCaseFromUnderscore(n);
	}

	public static String toField(String n) {
		return "_" + n;
	}

    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }


    public static String camelCaseFromUnderscore(String input) {
        StringBuilder b = new StringBuilder();
        for (String el : input.split("_")) {
            b.append(capitalize(el.toLowerCase()));
        }
        return b.toString();
    }
}
