package io.gaultier.modeling.util.base;

import org.apache.commons.codec.binary.*;

public class Base64Encoder {

    public static String encodeString(byte[] buffer) {
        return Base64.encodeBase64String(buffer);
    }

    public static byte[] decode(String str) {
        return Base64.decodeBase64(str);
    }

    public static byte[] decode(byte[] str) {
        return Base64.decodeBase64(str);
    }
}
