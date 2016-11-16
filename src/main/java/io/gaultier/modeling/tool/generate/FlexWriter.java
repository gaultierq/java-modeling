package io.gaultier.modeling.tool.generate;

import java.io.*;

public class FlexWriter extends JavaWriter {

    FlexWriter(SourceModel m, String p, String n) {
        super(m, p, n);
    }

    @Override
    protected File makeFileName() {
        File dir = model.getFlexRoot();
        if (!packageName.isEmpty()) {
            dir = new File(dir, packageName.replace('.', '/'));
        }
        return model.makeFile(dir, className + ".as");
    }
}
