package io.gaultier.modeling.tool.generate;

import japa.parser.*;
import japa.parser.ast.*;

import java.io.*;
import java.util.*;


import io.gaultier.modeling.util.base.*;

public class SourceModel {

	private static final Collection<String> IGNORED_FILES;
	private static final Collection<String> IGNORED_FILE_EXTENSIONS;
	private File genTargetRoot;
	private File flexTargetRoot;
	private File ipadTargetRoot;
	private File htmlTargetRoot;
	private boolean outputCheck = true;
	private boolean dumpControls;
	private Collection<File> generatedFiles = new HashSet<File>();

	private Map<String, AstClass> classes = new TreeMap<String, AstClass>();
	private Collection<AstClass> parsedClasses = new ArrayList<AstClass>();
	private Map<String, CompiledClass> compiled = new HashMap<String, CompiledClass>();


	private ModelProcessor models = new ModelProcessor(this);
	private ControlProcessor controls = new ControlProcessor(this);

	private boolean onlyJson;

	static {
		IGNORED_FILES = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		IGNORED_FILES.addAll(Arrays.asList(".", "..", ".svn", ".DS_Store"));
		IGNORED_FILE_EXTENSIONS = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		IGNORED_FILE_EXTENSIONS.addAll(Arrays.asList("js", "js.map"));
	}

	public CompiledClass getCompiledClass(String n) {
		CompiledClass c = compiled.get(n);
		if (c == null) {
			c = new CompiledClass(this, n);
			compiled.put(n, c);
		}
		return c;
	}

	void loadClass(File file) {
		StringBuilder buf;
		try {
			FileInputStream fis = new FileInputStream(file);
			buf = FileUtils.readFully(new InputStreamReader(fis, FileUtils.UTF_8));
			fis.close();
		}
		catch (IOException e) {
			throw new WrappedException(e);
		}
		CompilationUnit node;
		try {
			node = JavaParser.parse(new ByteArrayInputStream(buf.toString().getBytes()));
		}
		catch (ParseException e) {
			throw new WrappedException("Source: " + file, e);
		}
		AstClass c = new AstClass(this, file);
		System.out.println(file);
		c.read(node);
		
		if (c.getQualifiedName() == null) {
			return;
		}
		
		parsedClasses.add(c);
		addClass(c);
	}

	void addClass(AstClass c) {
		System.out.println("Add: " + c.getQualifiedName());
		if (classes.put(c.getQualifiedName(), c) != null) {
			assert false;
		}
	}

	public AstClass getClass(String n) {
		return classes.get(n);
	}

	public AstClass obtainClass(String n) {
		AstClass c = classes.get(n);
		if (c == null) {
			System.out.println("Creating: " + n);
			c = new AstClass(this, n);
			classes.put(n, c);
		}
		return c;
	}

	Collection<AstClass> getParsedClasses() {
		return parsedClasses;
	}

	void process() {
		for (AstClass c : parsedClasses) {
			c.process(); //?
		}

		//create the models
		models.process();         

		//creating the apis
		if (dumpControls) {
			controls.process();
		}
		checkFiles();
	}

	void warning(String msg) {
		System.out.println("Warning: " + msg);
	}

	File getTargetRoot() {
		return genTargetRoot;
	}

	File getFlexRoot() {
		return flexTargetRoot;
	}

	File getIpadRoot() {
		return ipadTargetRoot;
	}

	File getHtmlRoot() {
		return htmlTargetRoot;
	}

	File makeFile(File dir, String file) {
		dir.mkdirs();
		File f = new File(dir, file);
		if (!generatedFiles.add(f)) {
			warning("File " + f + " is being written twice");
		}
		return f;
	}

	private void checkFiles() {
		if (!outputCheck) {
			return;
		}
		StringBuilder rm = new StringBuilder();
		StringBuilder rmdir = new StringBuilder();
		checkFiles(getTargetRoot(), rm, rmdir);
		checkFiles(getFlexRoot(), rm, rmdir);
		checkFiles(getIpadRoot(), rm, rmdir);
		checkFiles(getHtmlRoot(), rm, rmdir);
		if (rm.length() != 0) {
			System.out.println("\n" + ("rm") + rm);
		}
		if (rmdir.length() != 0) {
			System.out.println("\nrmdir" + rmdir);
		}
	}

	private boolean checkFiles(File f, StringBuilder rm, StringBuilder rmdir) {
		if (f == null) {
			return false;
		}
		if (IGNORED_FILES.contains(f.getName())) {
			return false;
		}
		for (String ext : IGNORED_FILE_EXTENSIONS) {
			if (f.getName().endsWith(ext)) {
				return false;
			}
		}
		if (f.isDirectory()) {
			boolean hasFiles = false;
			for (File sf : f.listFiles()) {
				hasFiles |= checkFiles(sf, rm, rmdir);
			}
			if (!hasFiles) {
				warning("Dir " + f + " is in the way, clean the output dirs");
				rmdir.append(" " + f);
			}
			return true;
		}
		if (generatedFiles.contains(f)) {
			return true;
		}
		warning("File " + f + " is in the way, clean the output dirs");
		rm.append(" " + f);
		return true;
	}

	boolean setOption(String opt) {
		String n = "-target=";
		if (opt.startsWith(n)) {
			genTargetRoot = new File(opt.substring(n.length()));
			return true;
		}
		n = "-flex=";
		if (opt.startsWith(n)) {
			flexTargetRoot = new File(opt.substring(n.length()));
			return true;
		}
		n = "-ipad=";
		if (opt.startsWith(n)) {
			ipadTargetRoot = new File(opt.substring(n.length()));
			return true;
		}
		n = "-noOutputCheck";
		if (opt.equals(n)) {
			outputCheck = false;
			return true;
		}
		n = "-dumpControls";
		if (opt.equals(n)) {
			dumpControls = true;
			return true;
		}
		n = "-html=";
		if (opt.startsWith(n)) {
			htmlTargetRoot = new File(opt.substring(n.length()));
			return true;
		}
		n = "-onlyJson";
		if (opt.startsWith(n)) {
			onlyJson = true;
			return true;
		}
		return false;
	}
}
