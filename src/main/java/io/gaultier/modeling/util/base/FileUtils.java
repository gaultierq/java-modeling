package io.gaultier.modeling.util.base;

import java.io.*;
import java.security.*;
import java.util.*;



/**
 * Miscellaneous functions for dealing with files and streams.
 */
public final class FileUtils {

    public static final String UTF_8 = "UTF-8";
    public static final String ISO_8859_1 = "ISO-8859-1";
    public static final String ISO_8859_15 = "ISO-8859-15";

    private FileUtils() {
    }

    public static FileInputStream open(File f) {
        try {
            return new FileInputStream(f);
        }
        catch (IOException e) {
            throw new WrappedException(e);
        }
    }

    public static void close(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        }
        catch (IOException e) {
            throw new WrappedException(e);
        }
    }

    public static ByteBufferStream readInto(File f, ByteBufferStream bbs) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        try {
            bbs.appendFullyFrom(fis);
        }
        finally {
            fis.close();
        }
        return bbs;
    }

    public static <T extends Appendable> T readInto(File f, T a) {
        try {
            InputStream is = FileUtils.open(f);
            BufferedReader r = new BufferedReader(new InputStreamReader(is, FileUtils.UTF_8));
            for (;;) {
                String l = r.readLine();
                if (l == null) {
                    break;
                }
                l = l.trim();
                if (l.isEmpty()) {
                    continue;
                }
                a.append(l);
            }
            is.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return a;
    }

    public static void writeTo(ByteBufferStream bbs, File f) throws IOException {
        FileOutputStream fos = new FileOutputStream(f);
        try {
            bbs.writeTo(fos);
        }
        finally {
            fos.close();
        }
    }

    /**
     * Read the full content of the reader into a StringBuilder.
     * @param r input.
     * @return Builder containing the content of the reader.
     */
    public static StringBuilder readFully(Reader r) throws IOException {
        char[] buf = new char[4096];
        StringBuilder b = new StringBuilder(buf.length);
        int i;
        while ((i = r.read(buf)) > 0) {
            b.append(buf, 0, i);
        }
        return b;
    }

    public static StringBuilder readFullyUTF8(File f) throws IOException {
        Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), FileUtils.UTF_8));
        return readFully(r);
    }

    public static void readFully(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[4096];
        int i;
        while ((i = is.read(buf)) > 0) {
            os.write(buf, 0, i);
        }
    }

    public static String bytesToString(byte[] t, String charset) {
        try {
            return new String(t, charset);
        }
        catch (UnsupportedEncodingException e) {
            throw new WrappedException(e);
        }
    }

    public static byte[] stringToBytes(String t, String charset) {
        try {
            return t.getBytes(charset);
        }
        catch (UnsupportedEncodingException e) {
            throw new WrappedException(e);
        }
    }

    /**
     * Collects all the files in a directory tree.
     * @param dir Base directory.
     * @return Files.
     */
    public static List<File> traverse(File dir) {
        return traverse(dir, null);
    }

    /**
     * Collects matching files in a directory tree.
     * @param dir Base directory.
     * @param match Filter.
     * @return Matching files.
     */
    public static List<File> traverse(File dir, FileFilter match) {
        List<File> res = new ArrayList<File>();
        traverse(dir, res, match);
        return res;
    }

    /**
     * Traverse a directory tree, collecting files.
     * @param dir Base directory.
     * @param res Resulting file collection.
     * @param match Optional filter for files.
     */
    public static void traverse(File dir, Collection<File> res, FileFilter match) {
        File[] fs = dir.listFiles();
        for (File f : fs) {
            if (".".equals(f.getName()) || "..".equals(f.getName())) {
                continue;
            }
            if (f.isDirectory()) {
                traverse(f, res, match);
            }
            else if (match == null || match.accept(f)) {
                res.add(f);
            }
        }
    }

    public static ByteBufferStream md5(byte[] buf) {
        return md5(buf, 0, buf.length);
    }

    public static ByteBufferStream md5(byte[] buf, int start, int length) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            throw new WrappedException(e);
        }
        md.update(buf, start, length);
        return new ByteBufferStream(md.digest());
    }

    public static ByteBufferStream md5(ByteBufferStream buf) {
        return md5(buf.getBuffer(), 0, buf.size());
    }

    public static ByteBufferStream sha1(byte[] buf) {
        return sha1(buf, 0, buf.length);
    }

    public static ByteBufferStream sha1(byte[] buf, int start, int length) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA1");
        }
        catch (NoSuchAlgorithmException e) {
            throw new WrappedException(e);
        }
        return new ByteBufferStream(md.digest(buf));
    }

    public static File getTempDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    public static String unhex(String text) {
        return ByteBufferStream.fromHexString(text).toString(FileUtils.UTF_8);
    }

    public static File createTempFile(String action) throws IOException {
        File tmp = null;
        try {
            tmp = File.createTempFile(action, null, getTempDir());
        }
        catch (IOException e) {
            
            FileUtils.deleteSafely(tmp);
            throw e;
        }
        return tmp;
    }

    public static void deleteSafely(File tmp) {
        if (tmp != null && tmp.exists()) {
            tmp.delete();
        }
    }
}
