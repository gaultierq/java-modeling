package io.gaultier.modeling.model.data;

import java.io.*;
import java.nio.*;
import java.sql.*;
import java.util.*;

import io.gaultier.modeling.model.data.enumtype.*;

public final class FieldType {

    private DataType type;
    private final Class<? extends Enum<?>> enumType;
    private PrimitiveSubstitution substitution;
    private boolean isList;
    private DataDefinition<? extends ModelData<?>> objectType;
    private EnumValues persistentValues;
    private EnumValues amfValues;

    FieldType(DataType t, Class<? extends Enum<?>> e, boolean list, DataDefinition<? extends ModelData<?>> objCl, PrimitiveSubstitution subst) {
        type = t;
        enumType = e;
        isList = list;
        objectType = objCl;
        substitution = subst;
        if (e == null) {
            return;
        }
        /*if (!persisted) {
            return;
        }*/
        assert type == null;
        persistentValues = new PersistentEnumValues();
        type = persistentValues.init(e);
        amfValues = new AmfEnumValues();
        amfValues.init(e);
    }

    public static FieldType getData(DataType data) {
        return new FieldType(data, null, false, null, null);
    }

    public static FieldType getEnum(Class<? extends Enum<?>> e) {
        return new FieldType(null, e, false, null, PrimitiveSubstitution.DEFAULT);
    }

    public boolean isEnum() {
        return enumType != null;
    }

    public DataType getType() {
        return type;
    }

    public boolean isList() {
        return isList;
    }

    public DataDefinition<?> getObjectType() {
        return objectType;
    }

    void checkType(Object value) {
        if (objectType != null) {
            if (isList) {
                assert value instanceof DataList<?>;
                assert objectType.equals(((DataList<?>) value).getDefinition());
            }
            else {
                assert objectType.getDataClass().equals(value.getClass()); //TODO inheritance ?
            }
        }
        else if (enumType != null) {
            assert enumType.isInstance(value);
        }
        else {
            type.checkType(value);
        }
    }

    public Object getOnResultSet(ResultSet rs, int index) throws SQLException {
        if (enumType == null) {
            return type.getOnResultSet(rs, index);
        }
        Object value = type.getOnResultSet(rs, index);
        if (value == null) {
            return null;
        }
        Object v = persistentValues.valueToEnum(value);
        assert v != null : index  + ": " + enumType.getName() + " = " + value;
        return v;
    }

    public void setOnPreparedStatement(PreparedStatement ps, int index, Object value) throws SQLException {
        if (enumType == null) {
            type.setOnPreparedStatement(ps, index, value);
        }
        else {
            if (value != null) {
                value = persistentValues.enumToValue(value);
                assert value != null;
            }
            type.setOnPreparedStatement(ps, index, value);
        }
    }

    String getSqlForValue(Object value) {
        if (enumType == null) {
            return type.getSqlForValue(value);
        }
        if (value != null) {
            value = persistentValues.enumToValue(value);
            assert value != null;
        }
        return type.getSqlForValue(value);
    }

    public boolean equals(Object va, Object vb) {
        if (enumType != null) {
            return va == vb;
        }
        if (type == null) {
            return va.equals(vb); //TODO
        }
        return type.equals(va, vb);
    }

    public int hashCode(Object v) {
        if (enumType != null) {
            return v.hashCode();
        }
        return type.hashCode(v);
    }

    public <T> int compare(T va, T vb) {
        if (va == null) {
            return vb == null ? 0 : -1;
        }
        if (vb == null) {
            return 1;
        }
        if (enumType != null) {
            return ((Enum<?>) va).ordinal() - ((Enum<?>) vb).ordinal();
        }
        return type.compare(va, vb);
    }

    public <T> int compareForUser(T va, T vb) {
        if (va == null) {
            return vb == null ? 0 : -1;
        }
        if (vb == null) {
            return 1;
        }
        if (enumType != null) {
            return ((Enum<?>) va).ordinal() - ((Enum<?>) vb).ordinal();
        }
        return type.compareForUser(va, vb);
    }

    public String toString(Object value, IdentityHashMap<Object, Object> written, boolean full) {
        if (value == null) {
            return "null";
        }
        if (objectType != null) {
            if (isList) {
                return ((DataList<?>) value).toString(written, full);
            }
            return ((ModelData<?>) value).toString(written, full);
        }
        if (enumType != null) {
            return value.toString();
        }
        return type.toString(value, full);
    }

    void writeAmf(ObjectOutput out, Object value) throws IOException {
        if (objectType != null) {
            if (isList) {
                out.writeObject(value == null ? null : ((List<?>) value).toArray());
            }
            else {
                out.writeObject(value);
            }
        }
        else if (enumType != null) {
            out.writeInt(value == null ? 0 : ((Integer) amfValues.enumToValue(value)).intValue());
        }
        else {
            type.writeAmf(out, value);
        }
    }

    Object readAmf(ObjectInput in) throws IOException, ClassNotFoundException {
        if (objectType != null) {
            if (isList) {
                Object[] o = (Object[]) in.readObject();
                if (o == null) {
                    return null;
                }
                return objectType.createList(o);
            }
            return in.readObject();
        }
        else if (enumType != null) {
            return amfValues.valueToEnum(in.readInt());
        }
        return type.readAmf(in, substitution);
    }


    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T enumFromAmf(Integer v) {
        return (T) amfValues.valueToEnum(v);
    }

    public Integer amfToEnum(Object enumValue) {
        Integer result = null;
        if (enumValue != null) {
            return ((Integer) amfValues.enumToValue(enumValue));
        }
        return result;
    }

    public boolean isMutable() {
        return objectType != null;
    }

    public void serialize(ByteBuffer b, Object value) {
        assert value != null;
        if (enumType == null) {
            type.serialize(b, value);
        }
        else {
            value = persistentValues.enumToValue(value);
            assert value != null;
            type.serialize(b, value);
        }
    }

    public Object deserialize(ByteBuffer b) {
        if (enumType == null) {
            return type.deserialize(b);
        }
        Object value = type.deserialize(b);
        assert value != null;
        Object v = persistentValues.valueToEnum(value);
        assert v != null;
        return v;
    }

    public boolean isDataType() {
        return objectType != null;
    }

    public Class<? extends Enum<?>> getEnumType() {
        return enumType;
    }
}
