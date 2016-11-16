package io.gaultier.modeling.util.base;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;

public final class ByteBufferStream extends OutputStream implements Serializable, ByteArray {

    private byte[] buf;
    private int outIndex;

    public ByteBufferStream() {
        this(0x100);
    }

    public ByteBufferStream(int initialCapacity) {
        buf = new byte[initialCapacity];
    }

    /**
     * Construct a bbs with the specified initial buffer.
     * The out index is at the end of the buffer.
     * @param buffer Buffer (not copied).
     */
    public ByteBufferStream(byte[] buffer) {
        outIndex = buffer.length;
        buf = buffer;
    }

    public void moveSize(int len) {
        outIndex += len;
    }

    public void ensure(int len) {
        int needed = outIndex + len;
        if (needed <= buf.length) {
            return;
        }
        int target = Integer.highestOneBit(needed);
        if (target < needed) {
            target <<= 1;
        }
        buf = Arrays.copyOf(buf, target);
    }

    public byte[] getBuffer() {
        return buf;
    }

    @Override
    public byte byteAt(int index) {
        return buf[index];
    }

    public void setByteAt(int index, byte b) {
        buf[index] = b;
    }

    void copyAt(int srcPos, Object dest, int destPos, int length) {
        System.arraycopy(buf, srcPos, dest, destPos, length);
    }

    /**
     * Make the buffer exactly the  current size.
     */
    public ByteBufferStream trimToSize() {
        if (outIndex < buf.length) {
            buf = Arrays.copyOf(buf, outIndex);
        }
        return this;
    }

    public int getCapacity() {
        return buf.length;
    }

    public int getFreeCapacity() {
        return buf.length - outIndex;
    }

    @Override
    public int size() {
        return outIndex;
    }

    public void reset() {
        outIndex = 0;
    }

    /**
     * Delete a chunk of data.
     * If input streams exist, their read pos becomes invalid, they should be discarded.
     * @param start Start index of chunk.
     * @param length Length of deleted chunk.
     */
    public void delete(int start, int length) {
        assert start >= 0;
        assert length >= 0;
        int trailing = outIndex - (start + length);
        assert trailing >= 0;
        if (length == 0) {
            return;
        }
        if (trailing == 0) {
            outIndex = start;
            return;
        }
        System.arraycopy(buf, start + length, buf, start, trailing);
        outIndex -= length;
    }

    @Override
    public Input getInput() {
        return new Input(this);
    }

    /**
     * Copy the contents.
     * @return A buffer of length {@link #size()}.
     */
    @Override
    public byte[] toByteArray() {
        return Arrays.copyOf(buf, outIndex);
    }

    @Override
    public byte[] toByteArrayUnsafe() {
        if (outIndex == buf.length) {
            return buf;
        }
        return toByteArray();
    }

    @Override
    public ByteBuffer asByteBuffer() {
        return ByteBuffer.wrap(buf, 0, outIndex);
    }

    /**
     * Write the buffer content to the specified stream.
     * @param os Target stream.
     */
    @Override
    public void writeTo(OutputStream os) throws IOException {
        os.write(buf, 0, outIndex);
    }

    public void writeTo(DataOutput os) throws IOException {
        os.write(buf, 0, outIndex);
    }

    /**
     * Performs a read from the specified InputStream, appending the result to this buffer.
     * @param is Read from.
     * @param length At most length bytes will be read.
     * @return See {@link InputStream#read(byte[], int, int)}
     */
    public int appendFrom(InputStream is, int length) throws IOException {
        ensure(length);
        int r = is.read(buf, outIndex, length);
        if (r > 0) {
            outIndex += r;
        }
        return r;
    }

    /**
     * Append the entire content (until EOF) of the stream to this buffer.
     * @param is Source.
     * @return Number of bytes read.
     */
    public int appendFullyFrom(InputStream is) throws IOException {
        int total = 0;
        for (;;) {
            if (getFreeCapacity() < 0x1000) {
                ensure(0x1000);
            }
            assert getFreeCapacity() > 0;
            int r = is.read(buf, outIndex, getFreeCapacity());
            if (r < 0) {
                break;
            }
            outIndex += r;
            total += r;
        }
        return total;
    }

    /**
     * Convert the string to the charset and append the bytes to this buffer.
     * @param text Text.
     * @param charset Charset name.
     * @return This.
     */
    public ByteBufferStream append(String text, String charset) {
        try {
            write(text.getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            throw new WrappedException(e);
        }
        return this;
    }

    /**
     * Read a string from a portion of this buffer.
     * @param offset Start from.
     * @param length Number of bytes to read.
     * @param charset Charset name.
     * @return The string.
     */
    @Override
    public String toString(int offset, int length, String charset) {
        try {
            return new String(buf, offset, length, charset);
        } catch (UnsupportedEncodingException e) {
            throw new WrappedException(e);
        }
    }

    /**
     * Convert this buffer to a string.
     * @param charset Charset name.
     * @return The string.
     */
    @Override
    public String toString(String charset) {
        return toString(0, outIndex, charset);
    }

    public String toString(Charset charset) {
        return new String(buf, 0, outIndex, charset);
    }

    @Deprecated
    public String toString(int hibyte) {
        return new String(buf, hibyte, 0, outIndex);
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }

    @Override
    public void write(byte[] b, int start, int len) {
        assert len >= 0;
        if (len != 0) {
            ensure(len);
            System.arraycopy(b, start, buf, outIndex, len);
            outIndex += len;
        }
    }

    @Override
    public void write(byte[] b) {
        write(b, 0, b.length);
    }

    @Override
    public void write(int b) {
        ensure(1);
        buf[outIndex++] = (byte) b;
    }

    @Override
    public String toString() {
        return "byte[" + outIndex + "]";
    }

    @Override
    public String toStringRepresentation() {
        return toStringRepresentation(false);
    }

    public String toStringRepresentation(boolean force) {
        int n = 0;
        for (int i = outIndex; i-- > 0;) {
            byte b = byteAt(i);
            if (b < 32 || b >= 127) {
                n++;
            }
        }
        if (!force && outIndex != 0 && n * 10 > outIndex) {
            return toHexRepresentation(true);
        }
        StringBuilder t = new StringBuilder(outIndex * 2 + outIndex / 4 + 2);
        t.append('[');
        for (int i = 0; i < outIndex; i++) {
            int l = byteAt(i);
            if (l < 32 || l >= 127) {
                int h = l;
                l &= 0xf;
                h >>= 4;
                h &= 0xf;
                t.append('\\');
                t.append((char) (h < 10 ? h + '0' : h - 10 + 'a'));
                t.append((char) (l < 10 ? l + '0' : l - 10 + 'a'));
            } else if (l == '\\') {
                t.append("\\\\");
            } else {
                t.append((char) l);
            }
        }
        t.append(']');
        return t.toString();
    }

    @Override
    public String toHexRepresentation(boolean pretty) {
        return toHexRepresentation(pretty, true);
    }

    /**
     * Content as "[12345678 9abcde]".
     * @param pretty If false, use plain "0123456789abcde".
     * @param full If false, truncate long content.
     * @return Text.
     */
    @Override
    public String toHexRepresentation(boolean pretty, boolean full) {
        StringBuilder t = new StringBuilder(outIndex * 2 + outIndex / 4 + 2);
        if (pretty) {
            t.append('[');
        }
        for (int i = 0; i < outIndex; i++) {
            if (!full && i >= 0x1000) {
                t.append("… (").append(outIndex).append(" total bytes)");
                break;
            }
            if (pretty && i != 0 && (i % 4) == 0) {
                t.append(' ');
            }
            int l = byteAt(i);
            int h = l;
            l &= 0xf;
            h >>= 4;
            h &= 0xf;
            t.append((char) (h < 10 ? h + '0' : h - 10 + 'a'));
            t.append((char) (l < 10 ? l + '0' : l - 10 + 'a'));
        }
        if (pretty) {
            t.append(']');
        }
        return t.toString();
    }

    private static int parseHexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        throw new NumberFormatException(String.valueOf(c));
    }

    public static ByteBufferStream fromHexStringSafe(String hex) {
        if (hex == null) {
            return null;
        }
        return fromHexString(hex);
    }

    public static ByteBufferStream fromHexString(String hex) {
        int h = hex.length();
        int b = h / 2;
        if (b * 2 != h) {
            throw new NumberFormatException(hex);
        }
        ByteBufferStream bbs = new ByteBufferStream(b);
        for (; b-- > 0;) {
            int v = parseHexDigit(hex.charAt(--h));
            v |= parseHexDigit(hex.charAt(--h)) << 4;
            bbs.buf[b] = (byte) v;
        }
        bbs.outIndex = bbs.buf.length;
        return bbs;
    }

    @Override
    public ByteBufferStream clone() {
        ByteBufferStream bbs;
        try {
            bbs = (ByteBufferStream) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new WrappedException(e);
        }
        bbs.buf = buf.clone();
        bbs.outIndex = outIndex;
        return bbs;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeInt(outIndex);
        stream.write(buf, 0, outIndex);
    }

    private void readObject(ObjectInputStream stream) throws IOException {
        outIndex = stream.readInt();
        buf = new byte[outIndex];
        stream.readFully(buf);
    }

    /**
     * Warning : this method is inefficient.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof ByteBufferStream)) {
            return false;
        }
        ByteBufferStream bbs = (ByteBufferStream) o;
        if (outIndex != bbs.outIndex) {
            return false;
        }
        return Arrays.equals(outIndex == buf.length ? buf : Arrays.copyOf(buf, outIndex), bbs.outIndex == bbs.buf.length ? bbs.buf : Arrays.copyOf(bbs.buf, bbs.outIndex));
    }

    /**
     * Warning : this method is inefficient.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(outIndex == buf.length ? buf : Arrays.copyOf(buf, outIndex));
    }

    @Override
    public int compareTo(ByteArray o) {
        return compareTo((ByteBufferStream) o);
    }

    /**
     * Warning : this method is inefficient.
     */
    public int compareTo(ByteBufferStream o) {
        int len = Math.min(outIndex, o.outIndex);
        for (int i = 0; i < len; i++) {
            if (buf[i] != o.buf[i]) {
                return (buf[i] & 0xff) < (o.buf[i] & 0xff) ? -1 : 1;
            }
        }
        if (outIndex > len) {
            return 1;
        }
        if (o.outIndex > len) {
            return -1;
        }
        return 0;
    }

    public static class Input extends InputStream {

        private final ByteBufferStream out;
        private int inIndex;
        private int mark;

        Input(ByteBufferStream b) {
            out = b;
        }

        @Override
        public int available() {
            return out.size() - inIndex;
        }

        @Override
        public void close() {
        }

        @Override
        public void mark(int limit) {
            mark = inIndex;
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        @Override
        public int read() {
            if (inIndex < out.size()) {
                return 0xff & out.byteAt(inIndex++);
            }
            return -1;
        }

        @Override
        public int read(byte[] b, int start, int len) {
            int r = out.size() - inIndex;
            if (r <= 0) {
                return -1;
            }
            len = Math.min(len, r);
            if (len <= 0) {
                return 0;
            }
            out.copyAt(inIndex, b, start, len);
            inIndex += len;
            return len;
        }

        @Override
        public int read(byte[] b) {
            return read(b, 0, b.length);
        }

        @Override
        public void reset() {
            inIndex = mark;
        }

        @Override
        public long skip(long len) {
            len = Math.min(len, out.size() - inIndex);
            inIndex += len;
            return len;
        }

        @Override
        public String toString() {
            return out.toString();
        }
    }

    public static ByteBufferStream fromByteBuffer(ByteBuffer bb) {
        ByteBufferStream bbs = new ByteBufferStream(bb.remaining());
        bbs.outIndex = bbs.buf.length;
        bb.get(bbs.buf, 0, bbs.outIndex);
        return bbs;
    }

    public ByteBufferStream subbuffer(int beginIndex, int endIndex) {
        ByteBufferStream b = new ByteBufferStream(endIndex - beginIndex);
        b.write(getBuffer(), beginIndex, endIndex - beginIndex);
        return b;
    }
}
