package io.gaultier.modeling.model;


public final class Version implements Comparable<Version> {

    private final long value;

    private Version(long v) {
        value = v;
    }

    public static Version valueOf(long value) {
        return new Version(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Version)) {
            return false;
        }
        return value == ((Version) obj).value;
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >> 32));
    }

    @Override
    public int compareTo(Version o) {
        long d = value - o.value;
        return d == 0L ? 0 : d < 0L ? -1 : 1;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
    
    public long value() {
        return value;
    }
    
    public int intValue() {
        return (int) value;
    }
    
    public Version increment() {
        return new Version(value + 1);
    }
    
    public static Version getInitialVersion() {
        return valueOf(1);
    }
    
}
