package io.gaultier.modeling.tool.generate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.gaultier.modeling.model.data.DataClass;
import io.gaultier.modeling.model.data.DataDefinition;

public class ModelProcessor {

    //private static final String ONLY_PROCESS = "Url Association Tree Pearl User SpatialTree TreeList TreeCollapsed SyncOutput SyncOperation SyncOperationOutput";

    final SourceModel model;
    //    private final HtmlSingleFileWriter modeldts;
    private Map<String, ModelClass> classes = new HashMap<String, ModelClass>();

    ModelProcessor(SourceModel m) {
        model = m;
//        modeldts = new HtmlSingleFileWriter(model);
    }

    ModelClass getModelClass(AstClass c) {
        return classes.get(c.getQualifiedName());
    }

    private void processData(AstClass c, AstClassAnnotation a) {
        ModelClass res = new ModelClass();
        ModelClass mc = res.parse(this, c, a);
        if (mc != null) {
            classes.put(c.getQualifiedName(), mc);
        }
    }

    void process() {
        for (AstClass c : model.getParsedClasses()) {
            for (AstClassAnnotation a : c.getAnnotations()) {
                if (DataClass.class.getName().equals(a.getQualifiedName())) {
                    processData(c, a);
                }
            }
        }
        ModelClass[] cs = classes.values().toArray(new ModelClass[classes.size()]);
        Arrays.sort(cs);
        for (ModelClass c : cs) {
            c.write();
        }

        //TODO program param
        writeDataObjectRegistery(cs, "com.babysittor.model");

    }

    private void writeDataObjectRegistery(ModelClass[] cs, String pack) {
        JavaWriter w = new JavaWriter(model, pack, "DataObjectRegistery");
        w.writeln("package " + pack + ";\n");
        w.writeln("public final class DataObjectRegistery {");
        w.writeln("@SuppressWarnings(\"rawtypes\")");
        w.writeln("public final static " + DataDefinition.class.getName() + "[] ALL_DEFINITIONS = new " + DataDefinition.class.getName() + "[] {");
        for (int i = 0; i < cs.length; i++) {
            ModelClass c = cs[i];
            w.write(c.getPackageName() + ".");
            w.write(c.getDataName() + ".");
            w.write(c.getDefinitionConstant());
            if (i + 1 < cs.length) {
                w.writeln(",");
            }
        }
        w.writeln("};");
        String s = DataDefinition.class.getName();
        w.writeln("public final static java.util.Map<Class, "+ s +"> MAP_DEF = new java.util.HashMap<>();\n" +
                "static {\n" +
                "    for ("+ s +" def : ALL_DEFINITIONS) {\n" +
                "        MAP_DEF.put(def.getDataClass(), def);\n" +
                "}\n" +
                "}\n");

        w.writeln("}");
        w.output();
    }


}
