package io.gaultier.modeling.util.base;

import java.io.*;
import java.nio.*;

public interface ByteArray extends Cloneable, Comparable<ByteArray> {

    byte byteAt(int index);

    int size();

    InputStream getInput();

    void writeTo(OutputStream os) throws IOException;

    byte[] toByteArray();

    /**
     * Use only if you know what your are doing.
     * @return The content that might be shared with this ByteArray.
     */
    byte[] toByteArrayUnsafe();

    String toString(int offset, int length, String charset);

    String toString(String charset);

    String toStringRepresentation();

    String toHexRepresentation(boolean pretty);

    String toHexRepresentation(boolean pretty, boolean full);

    ByteArray clone();

    ByteBuffer asByteBuffer();
}
