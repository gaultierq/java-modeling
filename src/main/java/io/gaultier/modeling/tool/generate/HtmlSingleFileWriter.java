package io.gaultier.modeling.tool.generate;

public class HtmlSingleFileWriter extends HtmlWriter {
    
    public static final String MODULE_NAME = "serverData";
    private static final String OUTPUT = "ts-definition";
    private static final String FILE_NAME = "modeldata";
    
    HtmlSingleFileWriter(SourceModel m) {
        super(m, OUTPUT, FILE_NAME);
    }

    public void startModule() {
        writeln("declare module " + MODULE_NAME + " {");
    }
    
    public void startWritingClass(String cName) {
        writeln("\texport interface " + cName + " {");
    }
    
    public void stopWritingClass() {
//        writeln("\tmakeData: () => ;");
//        writeln("\ttoJson: ()=>string;");
        writeln("\t}");
        writeln("");
    }
    
    public void writeAttribute(String attr) {
        writeln("\t\t" + attr);
    }
    
    @Override
    void output() {
        writeln("}");
        super.output();
    }
    
    @Override
    protected String getExtension() {
        return ".d.ts";
    }
}