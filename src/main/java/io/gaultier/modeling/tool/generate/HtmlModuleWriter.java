package io.gaultier.modeling.tool.generate;

import java.util.*;
import java.util.Map.Entry;

public class HtmlModuleWriter {
    
    private final Map<String, HtmlWriter> moduleMap = new HashMap<String, HtmlWriter>();
    private final SourceModel model;
    private final StringBuilder moduleCommonHeader = new StringBuilder();
    
    HtmlModuleWriter(SourceModel m) {
        model = m;
    }

    public HtmlWriter getWriter(String moduleName, String outputDirectory, boolean isDef) {
        HtmlWriter h = moduleMap.get(moduleName);
        if (h != null) {
            return h;
        }
        h = new HtmlWriter(model, outputDirectory, moduleName.toLowerCase());
        if (isDef) {
            h.setAsDefintionFile();
        }
        if (isDef) {
            h.writeln("declare module " + moduleName + " {");
        }
        else {
            h.writeHeader(moduleCommonHeader.toString());
            h.writeln("export module " + moduleName + " {");
        }
        moduleMap.put(moduleName, h);
        return h;
    }
    
    public void addToModuleHeader(String value) {
        moduleCommonHeader.append(value + "\n");
    }
    
    void output() {
        for (Entry<String, HtmlWriter> val : moduleMap.entrySet()) {
            HtmlWriter h = val.getValue();
            h.writeln("}");
            h.output();
        }
    }
}