package io.gaultier.modeling.model.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.gaultier.modeling.util.base.WrappedException;

public final class DataDefinition<T extends ModelData<T>> {

    private final Class<T> dataClass;
    private final List<FieldDefinition<?, T>> fields = new ArrayList<FieldDefinition<?, T>>();
    private final Map<String, FieldDefinition<?, T>> fieldsByName = new LinkedHashMap<String, FieldDefinition<?, T>>();
    private final Collection<FieldDefinition<?, T>> persistedFields = new ArrayList<FieldDefinition<?, T>>();
    private final Collection<FieldDefinition<?, T>> primaryKey = new ArrayList<FieldDefinition<?, T>>();
    private String tableName;

    private DataDefinition(Class<T> clazz) {
        dataClass = clazz;
    }

    public static <T extends ModelData<T>> DataDefinition<T> create(Class<T> clazz, String tableName) {
        DataDefinition<T> d = new DataDefinition<T>(clazz);
        d.tableName = tableName;
        return d;
    }

    public <FT> FieldDefinition<FT, T> createField(int index, String name, DataType type, Class<? extends Enum<?>> enumType, boolean list, DataDefinition<? extends ModelData<?>> objCl, boolean persisted, String columnName, boolean amf, boolean json, boolean html, PrimitiveSubstitution substitution, int syntaxesEncoded) {
		FieldDefinition<FT, T> f = new FieldDefinition<FT, T>();
        f.init(this, index, name, type, enumType, list, objCl, persisted, columnName, amf, json, html, substitution, syntaxesEncoded);
        assert index == fields.size();
        if (fieldsByName.put(f.getName(), f) != null) {
            assert false : name;
        }
        fields.add(f);
        if (f.isPersisted()) {
            persistedFields.add(f);
        }
        return f;
    }

    <FT> void addPrimaryKey(FieldDefinition<FT, T> f) {
        primaryKey.add(f);
    }

    public Class<T> getDataClass() {
        return dataClass;
    }

    public String getTableName() {
        return tableName;
    }

    public String getEntityName() {
        String n = dataClass.getSimpleName();
        return n.substring(0, n.length() - 4);
    }

    public void setTableName(String table) {
        tableName = table;
    }

    public List<FieldDefinition<?, T>> getFields() {
        return fields;
    }

    public Collection<FieldDefinition<?, T>> getPersistedFields() {
        return persistedFields;
    }

    public Collection<FieldDefinition<?, T>> getPrimaryKey() {
        return primaryKey;
    }

    public FieldDefinition<?, T> getField(int index) {
        return fields.get(index);
    }

    public FieldDefinition<?, T> getField(String name) {
        FieldDefinition<?, T> f = fieldsByName.get(name);
        assert f != null : name;
        return f;
    }

    public T createData() {
        try {
            return dataClass.newInstance();
        } catch (InstantiationException e) {
            throw new WrappedException(e);
        } catch (IllegalAccessException e) {
            throw new WrappedException(e);
        }
    }

    public DataList<T> createList() {
        return new DataList<T>(this);
    }

    @SuppressWarnings("unchecked")
    public DataList<T> createList(Object[] values) {
        DataList<T> res = new DataList<T>(this);
        for (Object v : values) {
            res.add((T) v);
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    public DataList<T> createModelList(Collection<? extends ModelData<?>> values) {
        DataList<T> res = new DataList<T>(this);
        for (Object v : values) {
            res.add((T) v);
        }
        return res;
    }

    @Override
    public String toString() {
        return super.toString() + '(' + getDataClass().getSimpleName() + ')';
    }

    public FieldDefinition<?, ?>[] getNonAmfFields() {
        List<FieldDefinition<?, T>> res = new ArrayList<FieldDefinition<?,T>>();
        for (FieldDefinition<?, T> f : fields) {
            if (!f.isAmf()) {
                res.add(f);
            }
        }
        return res.toArray(new FieldDefinition<?, ?>[res.size()]);
    }

    public Map<String, FieldDefinition<?, T>> getFieldsByName() {
        return fieldsByName;
    }
}
