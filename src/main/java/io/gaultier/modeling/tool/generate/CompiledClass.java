package io.gaultier.modeling.tool.generate;

import java.lang.reflect.*;
import java.util.*;

public class CompiledClass {

    //private final SourceModel model;
    private Class<?> real;
    private String name;

    CompiledClass(SourceModel m, String n) {
        //model = m;
        name = n;
    }

    private void loadClass() {
        if (real != null) {
            return;
        }
        try {
            real = Class.forName(name);
        } catch (ClassNotFoundException e) {
            // Not found
        } catch (NoClassDefFoundError e) {
            // Not found
        }
    }

    boolean exists() {
        loadClass();
        return real != null;
    }

    public Map<String, Object> getAnnotationDefaultValues() {
        Map<String, Object> values = new HashMap<String, Object>();
        loadClass();
        if (real == null) {
            return values;
        }
        for (Method m : real.getMethods()) {
            Object v = m.getDefaultValue();
            if (v == null) {
                continue;
            }
            values.put(m.getName(), v);
        }
        return values;
    }
}
