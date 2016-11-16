package io.gaultier.modeling.tool.generate;

import java.io.*;
import java.util.*;

public class HtmlWriter extends JavaWriter {
    
    public static final String PATH_TO_SEND_STATS = "app/core/stats/";
    public static final String RELATIVE_PATH = "../../";
    private static final String RELATIVE_PATH_TO_DATA = "../../data/";
    
    private List<String> imports = new ArrayList<String>();
    private List<String> importsShortName = new ArrayList<String>();
    private Map<String, String> importMap = new HashMap<String, String>();
    private boolean isDefinitionFile = false;

    HtmlWriter(SourceModel m, String p, String n) {
        super(m, p, n);
    }
    
    private String writeImport(String name) {
        return writeImport(name, RELATIVE_PATH_TO_DATA);
    }
    
    private String writeImport(String name, String path) {
        int cutIdx = 3;
        String shortName = name.substring(0, cutIdx).toUpperCase();
        if (!imports.contains(name)) {
            while (importsShortName.contains(shortName)) {
                shortName = name.substring(0, cutIdx).toUpperCase();
                cutIdx++;
            }
            writeHeader("import " + shortName + " = require(\"" + path + name.toLowerCase() + "\");");
            imports.add(name);
            importsShortName.add(shortName);
            importMap.put(name, shortName);
        }
        return shortName;
    }
    
    public String getImportName(String input) {
        return getImportName(input, null);
    }
    
    public String getImportName(String input, String path) {
        String importName = importMap.get(input);
        if (importName == null) {
            if (path == null) {
                importName = writeImport(input);
            }
            else {
                importName = writeImport(input, path);
            }
        }
        return importName;
    }

    @Override
    protected File makeFileName() {
        File dir = model.getHtmlRoot();
        if (!packageName.isEmpty()) {
            dir = new File(dir, packageName.replace('.', '/'));
        }
        return model.makeFile(dir, className + getExtension());
    }
    
    public void setAsDefintionFile() {
        isDefinitionFile = true;
    }
    
    /*public boolean isDefinitionFile() {
        return isDefinitionFile;
    }*/
    
    protected String getExtension() {
        if (isDefinitionFile) {
            return ".d.ts";
        }
        return ".ts";
    }
}
