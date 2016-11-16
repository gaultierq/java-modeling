package io.gaultier.modeling.tool.generate;

import java.util.*;

public class ControlProcessor {

    private final SourceModel model;

    ControlProcessor(SourceModel m) {
        model = m;
    }

    void process() {
        Map<String, AstClass> ctrls = new TreeMap<String, AstClass>();
        for (AstClass c : model.getParsedClasses()) {
            if (!c.getFullName().endsWith("Control")) {
                continue;
            }
            ctrls.put(c.getFullName(), c);
        }

        for (AstClass c : ctrls.values()) {
            System.out.println(c.getFullName());
            for (AstMethod m : c.getMethods()) {
                if (!m.getName().startsWith("action")) {
                    continue;
                }
                System.out.println("\t" + m.getName());
            }
        }
    }
}
