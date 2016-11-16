package io.gaultier.modeling.util.base;

import java.lang.reflect.*;



public class ByteArrayDataHelper {

    public static ByteBufferStream create() {
        return new ByteBufferStream(8);
    }

    public static ByteBufferStream copy(ByteArray status) {
        ByteBufferStream s = create();
        if (status != null) {
            for (int i = 0; i < status.size(); i++) {
                s.write(status.byteAt(i));
            }
        }
        return s;
    }

    private static byte getByte(ByteArray s, int b) {
        return s != null && b < s.size() ? s.byteAt(b) : 0;
    }

    public static boolean getBoolean(ByteArray s, int index) {
        assert index >= 0;
        return (getByte(s, index >>> 3) & (1 << (index & 7))) != 0;
    }

    public static void setBoolean(ByteBufferStream s, int index, boolean v) {
        assert index >= 0;
        int b = index >>> 3;
        while (b >= s.size()) {
            s.write(0);
        }
        byte bv = s.byteAt(b);
        int mask = 1 << (index & 7);
        s.setByteAt(b, (byte) (v ? bv | mask : bv & ~mask));
    }

    public static void copy(ByteArray from, ByteBufferStream to, int index) {
        setBoolean(to, index, getBoolean(from, index));
    }

    public static void update(ByteArray old, ByteArray from, ByteBufferStream to, int index) {
        setBoolean(to, index, getBoolean(old, index) || getBoolean(from, index));
    }

    public static boolean equals(ByteArray a, ByteArray b) {
        if (a == null) {
            return b == null;
        }
        if (b == null) {
            return false;
        }
        for (int i = Math.max(a.size(), b.size()); i-- > 0;) {
            if (getByte(a, i) != getByte(b, i)) {
                return false;
            }
        }
        return true;
    }

    protected static String toString(Class<?> clazz, ByteArray status) {
        if (status == null) {
            return "status:(empty)";
        }
        StringBuilder b = new StringBuilder();
        String[] names = new String[status.size() * 8];
        for (Field f : clazz.getFields()) {
            if (!Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            if (!Modifier.isPublic(f.getModifiers())) {
                continue;
            }
            if (!Integer.TYPE.equals(f.getType())) {
                continue;
            }
            int v;
            try {
                v = f.getInt(null);
            }
            catch (Exception e) {
                
                continue;
            }
            if (v >= 0 && v < names.length) {
                names[v] = f.getName().toLowerCase().replace('_', ' ');
            }
        }

        for (int i = 0; i < names.length; i++) {
            if (!getBoolean(status, i)) {
                continue;
            }
            if (b.length() != 0) {
                b.append(',');
            }
            if (names[i] == null) {
                b.append(i);
            }
            else {
                b.append(names[i]);
            }
        }

        return "status:" + (b.length() == 0 ? "(empty)" : b);
    }
}
