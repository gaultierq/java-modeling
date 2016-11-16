package io.gaultier.modeling.model.data;

import java.io.*;
import java.nio.*;
import java.sql.*;
import java.text.*;
import java.util.Date;

import io.gaultier.modeling.model.*;

import io.gaultier.modeling.util.base.*;

public enum DataType {

    INTEGER,
    LONG,
    DOUBLE,
    STRING,
    DATETIME,
    BINARY,
    ID,
    VERSION,
    BOOLEAN; //no sql yet

    void checkType(Object value) {
        switch (this) {
        case INTEGER:
            assert value instanceof Integer;
            break;
        case LONG:
            assert value instanceof Long;
            break;
        case DOUBLE:
            assert value instanceof Double;
            break;
        case STRING:
            assert value instanceof String;
            break;
        case DATETIME:
            assert value instanceof Date;
            break;
        case BINARY:
            assert value instanceof ByteArray;
            break;
        case ID:
            assert value instanceof Id;
            break;
        case VERSION:
            assert value instanceof Version;
            break;
        case BOOLEAN:
            assert value instanceof Boolean;
            break;
        default:
            assert false : this;
        }
    }

    public Object getOnResultSet(ResultSet rs, int index) throws SQLException {
        switch (this) {
        case INTEGER:
            int i = rs.getInt(index);
            return rs.wasNull() ? null : i;
        case BOOLEAN:
            i = rs.getInt(index);
            return rs.wasNull() ? null : i == 1;
        case LONG:
            long l = rs.getLong(index);
            return rs.wasNull() ? null : l;
        case ID:
            l = rs.getLong(index);
            return rs.wasNull() ? null : Id.valueOf(l);
        case VERSION:
            l = rs.getLong(index);
            return rs.wasNull() ? null : Version.valueOf(l);
        case DOUBLE:
            double d = rs.getDouble(index);
            return rs.wasNull() ? null : d;
        case STRING:
            String s = rs.getString(index);
            return rs.wasNull() ? null : s;
        case DATETIME:
            Timestamp t = rs.getTimestamp(index);
            return rs.wasNull() ? null : new Date(t.getTime());
        case BINARY:
            byte[] b = rs.getBytes(index);
            return rs.wasNull() ? null : new ByteBufferStream(b);
        }
        assert false : this;
        return null;
    }

    public void setOnPreparedStatement(PreparedStatement ps, int index, Object value) throws SQLException {
        switch (this) {
        case INTEGER:
            if (value == null) {
                ps.setNull(index, Types.INTEGER);
            } else {
                ps.setInt(index, (Integer) value);
            }
            break;
        case BOOLEAN:
            if (value == null) {
                ps.setNull(index, Types.INTEGER);
            }
            else {
                ps.setInt(index, ((Boolean) value) ? 1 : 0);
            }
            break;
        case LONG:
            if (value == null) {
                ps.setNull(index, Types.BIGINT);
            } else {
                ps.setLong(index, (Long) value);
            }
            break;
        case ID:
            if (value == null) {
                ps.setNull(index, Types.BIGINT);
            } else {
                ps.setLong(index, ((Id) value).value());
            }
            break;
        case VERSION:
            if (value == null) {
                ps.setNull(index, Types.BIGINT);
            } else {
                ps.setLong(index, ((Version) value).value());
            }
            break;
        case DOUBLE:
            if (value == null) {
                ps.setNull(index, Types.DOUBLE);
            } else {
                ps.setDouble(index, (Double) value);
            }
            break;
        case STRING:
            if (value == null) {
                ps.setNull(index, Types.VARCHAR);
            } else {
                ps.setString(index, (String) value);
            }
            break;
        case DATETIME:
            if (value == null) {
                ps.setNull(index, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(index, new Timestamp(((Date) value).getTime()));
            }
            break;
        case BINARY:
            if (value == null) {
                ps.setNull(index, Types.VARBINARY);
            } else {
                ps.setBytes(index, ((ByteArray) value).toByteArray());
            }
            break;
        default:
            assert false : this;
        }
    }

    public String getSqlForValue(Object value) {
        assert value != null;
        switch (this) {
        case INTEGER:
        case LONG:
        case ID:
        case DOUBLE:
        case VERSION:
            return value.toString();
        case BOOLEAN:
            return value == null ? "0" : ((Boolean) value ? "1" : "0");
        case STRING:
            return quote((String) value);
        case DATETIME:
            return "date '" + new SimpleDateFormat("yyyyMMddHHmmss").format((Date) value) + "'";
        }
        assert false : this;
        return null;
    }

    public boolean equals(Object va, Object vb) {
        if (va == null) {
            return vb == null;
        }
        if (vb == null) {
            return false;
        }
        switch (this) {
        case INTEGER:
        case LONG:
        case ID:
        case VERSION:
        case DOUBLE:
        case STRING:
        case BINARY:
        case BOOLEAN:
            return va.equals(vb);
        case DATETIME:
            return ((Date) va).getTime() == ((Date) vb).getTime();
        }
        assert false : this;
        return false;
    }

    public int hashCode(Object v) {
        switch (this) {
        case INTEGER:
        case LONG:
        case ID:
        case VERSION:
        case DOUBLE:
        case STRING:
        case BINARY:
        case BOOLEAN:
            return v.hashCode();
        case DATETIME:
            long t = ((Date) v).getTime();
            return (int) (t ^ (t >>> 32));
        }
        assert false : this;
        return 0;
    }

    @SuppressWarnings("unchecked")
    <T> int compare(T va, T vb) {
        switch (this) {
        case INTEGER:
        case LONG:
        case ID:
        case VERSION:
        case DOUBLE:
        case STRING:
        case DATETIME:
        case BOOLEAN:
            return ((Comparable<T>) va).compareTo(vb);
        }
        assert false : this;
        return 0;
    }

    @SuppressWarnings("unchecked")
    <T> int compareForUser(T va, T vb) {
        switch (this) {
        case INTEGER:
        case LONG:
        case ID:
        case VERSION:
        case DOUBLE:
        case DATETIME:
        case BOOLEAN:
            return ((Comparable<T>) va).compareTo(vb);
        case STRING:
            return ((String) va).compareToIgnoreCase((String) vb);
        case BINARY:
            return ((Comparable<T>) va).compareTo(vb);
        }
        assert false : this;
        return 0;
    }

    public String toString(Object value, boolean full) {
        return toString(value, full, null/*new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")*/); //FIX ME Total shit: heavy object created each time
    }

    public String toString(Object value, boolean full, SimpleDateFormat format) {
        switch (this) {
        case INTEGER:
        case LONG:
        case ID:
        case VERSION:
        case DOUBLE:
        case BOOLEAN:
            return value.toString();
        case STRING:
            String s = value.toString();
            if (s.length() > 0x1000 && !full) {
                s = s.substring(0, 0x1000) + "… (" + s.length() + " total chars)";
            }
            return s;
        case DATETIME:
            if (format == null) {
                format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }
            return format.format((Date) value);
            //return String.valueOf(((Date) value).getTime());
        case BINARY:
            return ((ByteArray) value).toHexRepresentation(false, full);
        }
        assert false : this;
        return null;
    }

    public Object fromString(String in) {
        return fromString(in, null);
    }

    public Object fromString(String in, SimpleDateFormat format) {
        switch (this) {
        case INTEGER:
            return Integer.valueOf(in);
        case LONG:
            return Long.valueOf(in);
        case ID:
            return Id.parseNullableId(in);
        case VERSION:
            return Version.valueOf(Long.valueOf(in));
        case DOUBLE:
            return Double.valueOf(in);
        case STRING:
            return in.length() == 0 ? null : in;
        case DATETIME:
            if (format == null) {
                return new Date(Long.valueOf(in));
            }
            try {
                return format.parse(in);
            }
            catch (ParseException e) {
                
                return null;
            }
        case BINARY:
            return ByteBufferStream.fromHexString(in);
        case BOOLEAN:
            return Boolean.valueOf(in);
        }
        assert false : this;
        return null;
    }

    public void writeAmf(ObjectOutput out, Object value) throws IOException {
        switch (this) {
        case INTEGER:
            out.writeInt(value == null ? 0 : ((Integer) value).intValue());
            break;
        case LONG:
            out.writeInt(value == null ? 0 : ((Long) value).intValue());
            break;
        case ID:
            out.writeInt(value == null ? 0 : (int) ((Id) value).value());
            break;
        case VERSION:
            out.writeInt(value == null ? 0 : (int) ((Version) value).value());
            break;
        case DOUBLE:
            out.writeDouble(value == null ? 0. : ((Double) value).doubleValue());
            break;
        case STRING:
            out.writeObject(value);
            break;
        case DATETIME:
            out.writeDouble(value == null ? 0. : ((Date) value).getTime());
            break;
        case BINARY:
            out.writeObject(value == null ? null : ((ByteArray) value).toByteArray());
            break;
        default:
            assert false : this;
        }
    }

    public Object readAmf(ObjectInput in, PrimitiveSubstitution substitution) throws IOException, ClassNotFoundException {
        switch (this) {
        case INTEGER:
            int i = in.readInt();
            return substitution == PrimitiveSubstitution.SUBST && i == 0 ? null : Integer.valueOf(i);
        case LONG:
            int l = in.readInt();
            return substitution == PrimitiveSubstitution.SUBST && l == 0 ? null : Long.valueOf(l);
        case ID:
            l = in.readInt();
            return substitution != PrimitiveSubstitution.VERBATIM && l == 0 ? null : Id.valueOf(l);
        case VERSION:
            l = in.readInt();
            return substitution != PrimitiveSubstitution.VERBATIM && l == 0 ? null : Version.valueOf(l);
        case DOUBLE:
            double d = in.readDouble();
            return substitution == PrimitiveSubstitution.SUBST && d == 0. ? null : Double.valueOf(d);
        case STRING:
            return in.readObject();
        case DATETIME:
            d = in.readDouble();
            return substitution == PrimitiveSubstitution.SUBST && (d == 0. || Double.isNaN(d)) ? null : new Date((long) d);
        case BINARY:
            byte[] o = (byte[]) in.readObject();
            return o == null ? null : new ByteBufferStream(o);
        }
        assert false : this;
        return null;
    }

    public Object readJson(String in, PrimitiveSubstitution substitution) {
        if (in == null) {
            return null;
        }
        if ("null".equals(in)) {
            return null;
        }
        return fromString(in);
    }

    public static String quote(String n) {
        for (int i = 0; i < n.length(); i++) {
            if (n.charAt(i) == '\'') {
                n = n.substring(0, i) + '\\' + n.substring(i);
                i++;
            } else if (n.charAt(i) == '\\') {
                n = n.substring(0, i) + '\\' + n.substring(i);
                i++;
            }
        }
        return '\'' + n + '\'';
    }

    public void serialize(ByteBuffer b, Object value) {
        switch (this) {
        case ID:
            b.putLong(((Id) value).value());
            break;
        case STRING:
            byte[] bytes = FileUtils.stringToBytes((String) value, FileUtils.UTF_8);
            b.putInt(bytes.length);
            b.put(bytes);
            break;
        default:
            assert false : this;
        }
    }

    public Object deserialize(ByteBuffer b) {
        switch (this) {
        case ID:
            return Id.valueOf(b.getLong());
        case STRING:
            byte[] bytes = new byte[b.getInt()];
            b.get(bytes);
            return FileUtils.bytesToString(bytes, FileUtils.UTF_8);
        }
        assert false : this;
        return null;
    }
}
