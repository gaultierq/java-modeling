package io.gaultier.modeling.model;

import java.io.*;
import java.util.*;

public final class Id implements Comparable<Id>, Serializable {

    private static final long serialVersionUID = -6882870905710950659L;

    private final long value;

    private Id(long v) {
        value = v;
    }

    public static Id valueOf(long value) {
        return new Id(value);
    }

    public static Id valueOfFromPrimitive(long value) {
        return value == 0L ? null : new Id(value);
    }

    public static long toPrimitive(Id id) {
        return id == null ? 0L : id.value;
    }

    public static Id parseNullableId(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            long v = Long.parseLong(value);
            if (v == 0L) {
                return null;
            }
            return valueOf(v);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    public static Id parseId(String value) {
        try {
            long v = Long.parseLong(value);
            return valueOf(v);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    public static Id parseIdHex(String value) {
        try {
            long v = Long.parseLong(value, 16);
            return valueOf(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Id)) {
            return false;
        }
        return value == ((Id) obj).value;
    }

    @Override
    public int hashCode() {
        return hashCode(value);
    }

    public static int hashCode(long value) {
        return (int) (value ^ (value >> 32));
    }

    @Override
    public int compareTo(Id o) {
        long d = value - o.value;
        return d == 0L ? 0 : d < 0L ? -1 : 1;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    public String toHexString() {
        return Long.toHexString(value);
    }

    public long value() {
        return value;
    }

    public int intValue() {
        return (int) value;
    }

    public static boolean isSet(Id id) {
        return id != null && id.value > 0L;
    }

    public static boolean isNull(Id id) {
        return id == null || id.value <= 0L;
    }

    private static void addId(List<Id> ids, String val) {
        val = val.trim();
        if (!val.isEmpty()) {
            ids.add(valueOf(Long.parseLong(val)));
        }
    }

    public static List<Id> parseList(String list, String separator) {
        List<Id> ids = new ArrayList<Id>();
        if (list == null) {
            return ids;
        }
        for (int s = 0;;) {
            int e = list.indexOf(separator, s);
            if (e < 0) {
                addId(ids, list.substring(s));
                break;
            }
            addId(ids, list.substring(s, e));
            s = e + separator.length();
        }
        return ids;
    }

    public static List<Id> parseStringList(List<String> stringList) {
        List<Id> ids = new ArrayList<Id>();
        if (stringList == null) {
            return ids;
        }
        for (String stringId : stringList) {
            addId(ids, stringId);
        }
        return ids;
    }

    public static List<Id> fromNumbers(Collection<? extends Number> list) {
        if (list == null) {
            return null;
        }
        List<Id> res = new ArrayList<Id>(list.size());
        for (Number n : list) {
            res.add(n == null ? null : valueOf(n.longValue()));
        }
        return res;
    }

    public Id next() {
        return next(1);
    }

    public Id next(int delta) {
        return valueOf(value() + delta);
    }
}
