package io.gaultier.modeling.model.data;

import java.util.*;



public final class DataList<T extends ModelData<T>> extends AbstractList<T> implements RandomAccess {

    private final DataDefinition<T> definition;
    private final List<T> list = new ArrayList<T>();

    DataList(DataDefinition<T> def) {
        definition = def;
    }

    public DataDefinition<T> getDefinition() {
        return definition;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public void add(int index, T element) {
        assert element == null || element.getDefinition() == definition;
        list.add(index, element);
    }

    @Override
    public T set(int index, T element) {
        assert element == null || element.getDefinition() == definition;
        return list.set(index, element);
    }

    @Override
    public T remove(int index) {
        return list.remove(index);
    }

    //TODO AL: Who needs this ?
    public List<T> getList() {
        return list;
    }

    public String toString(boolean deep, boolean full) {
        return toString(deep ? new IdentityHashMap<Object, Object>() : null, full);
    }

    String toString(IdentityHashMap<Object, Object> written, boolean full) {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getSimpleName()).append('@').append(Integer.toHexString(System.identityHashCode(this)));
        if (written == null) {
            return b.toString();
        }
        if (written.put(this, this) == this) {
            return b.toString();
        }
        b.append('[');
        for (int i = 0; i < size(); i++) {
            if (i != 0) {
                b.append(",\n");
            }
            T e = get(i);
            b.append(e == null ? "null" : e.toString(written, full));
        }
        b.append(']');
        return b.toString();
    }

    DataList<T> shallowCopy() {
        DataList<T> res = new DataList<T>(getDefinition());
        res.list.addAll(list);
        return res;
    }

    public DataList<T> sortIt(Comparator<T> cmp) {
        Collections.sort(list, cmp);
        return this;
    }

    public DataList<T> sort(FieldDefinition<?, T>... fields) {
        DataUtils.sort(false, this, fields);
        return this;
    }

    public DataList<T> sort(boolean desc, FieldDefinition<?, T>... fields) {
        DataUtils.sort(desc, this, fields);
        return this;
    }

    public T last() {
        return get(size() - 1);
    }
}
