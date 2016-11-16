package io.gaultier.modeling.tool.generate;

import java.io.*;

public class IpadWriter extends JavaWriter {

    private final boolean inc;

    IpadWriter(SourceModel m, String p, String n, boolean i) {
        super(m, p, n);
        inc = i;
    }

    @Override
    protected File makeFileName() {
        File dir = model.getIpadRoot();
        if (!packageName.isEmpty()) {
            dir = new File(dir, packageName.replace('.', '/'));
        }
        return model.makeFile(dir, className + "." + (inc ? "h" : "m"));
    }
}
