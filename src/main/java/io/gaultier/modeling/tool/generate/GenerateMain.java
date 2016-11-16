package io.gaultier.modeling.tool.generate;

import java.io.*;

public class GenerateMain {

    private final SourceModel model = new SourceModel();

    private boolean setOption(String opt) {
        return model.setOption(opt);
    }

    private void loadFiles(File file) {
        if (file.getName().startsWith(".")) {
            return;
        }
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                loadFiles(child);
            }
        } else {
            System.out.println("processing:" + file.getName());
            if (file.getName().endsWith(".java")) {
                model.loadClass(file);
            }
        }
    }

    private void exec(String... args) {
        for (String a : args) {
            if (a.startsWith("-")) {
                if (!setOption(a)) {
                    System.err.println("Invalid option: " + a);
                    System.exit(1);
                    
                }
            }
        }
        for (String a : args) {
            if (a.startsWith("-")) {
                continue;
            }
            loadFiles(new File(a));
        }
        model.process();
    }

    public static void main(String... args) {
        new GenerateMain().exec(args);
    }
}
