package io.gaultier.modeling.model.data;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import io.gaultier.modeling.util.base.ByteArray;

public final class DataUtils {

    public static <F, D extends ModelData<D>> Map<F, D> index(Collection<D> list, FieldDefinition<F, D> field) {
        Map<F, D> res = new HashMap<F, D>();
        for (D o : list) {
            res.put(o.getValue(field), o);
        }
        return res;
    }


    public static final String toString(Object o) {
        return toString(o, false, ToStringFormatter.STANDARD);
    }

    public static final String toString(Object o, boolean full, ToStringFormatter formatter) {
        return toString(o, formatter, new IdentityHashMap<Object, Object>(), full);
    }

    public static final String toStringNoLf(Object o) {
        return toString(o).replace("\n", " / ");
    }

    static final String toString(Object o, ToStringFormatter formatter, IdentityHashMap<Object, Object> written, boolean full) {
        if (o == null) {
            return "null";
        }
        if (o instanceof ModelData<?>) {
            return ((ModelData<?>) o).toString(written, full);
        }
        if (o instanceof DataList<?>) {
            return ((DataList<?>) o).toString(written, full);
        }
        if (o instanceof ByteArray) {
            return ((ByteArray) o).toStringRepresentation();
        }

        if (o.getClass().isArray()) {
            if (written.put(o, o) == o) {
                return o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o));
            }
            //Object[] a = (Object[]) o;Array.
            StringBuilder b = new StringBuilder();
            b.append("[");
            int length = Array.getLength(o);
            if (o instanceof byte[]) {
                for (int i = 0; i < length; i++) {
                    int l = ((byte[]) o)[i];
                    int h = l;
                    l &= 0xf;
                    h >>= 4;
                    h &= 0xf;
                    b.append((char) (h < 10 ? h + '0' : h - 10 + 'a'));
                    b.append((char) (l < 10 ? l + '0' : l - 10 + 'a'));
                }
            }
            else {
                int l = 0;
                for (int i = 0; i < length; i++) {
                    String s = toString(Array.get(o, i), formatter, written, full);
                    if (s == null) {
                        s = "null";
                    }
                    if (i != 0) {
                        b.append(",");
                        l++;
                        if (l + s.length() + 2 <= 75) {
                            b.append(' ');
                            l++;
                        }
                        else {
                            b.append('\n');
                            l = 0;
                        }
                    }
                    b.append(s);
                    l += s.length();
                }
            }
            b.append("]");
            return b.toString();
        }

        if (o instanceof Collection) {
            if (written.put(o, o) == o) {
                return o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o));
            }
            Collection<?> a = (Collection<?>) o;
            StringBuilder b = new StringBuilder();
            b.append("size=").append(a.size());
            b.append("[");
            int i = 0;
            for (Object v : a) {
                if (i != 0) {
                    b.append(",\n");
                }
                b.append(toString(v, formatter, written, full));
                i++;
            }
            b.append("]");
            return b.toString();
        }

        if (o instanceof Iterator) {
            if (written.put(o, o) == o) {
                return o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o));
            }
            Iterator<?> it = (Iterator<?>) o;
            StringBuilder b = new StringBuilder();
            b.append(o.getClass().getName());
            b.append("[");
            boolean first = true;
            for (; it.hasNext();) {
                if (first) {
                    first = false;
                }
                else {
                    b.append(",\n");
                }
                b.append(toString(it.next(), formatter, written, full));
            }
            b.append("]");
            return b.toString();
        }

        if (o instanceof Map) {
            if (written.put(o, o) == o) {
                return o.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(o));
            }
            Map<?, ?> a = (Map<?, ?>) o;
            StringBuilder b = new StringBuilder();
            b.append(o.getClass().getSimpleName()).append("@").append(Integer.toHexString(System.identityHashCode(o)));
            b.append("{");
            int i = 0;
            for (Map.Entry<?, ?> e : a.entrySet()) {
                if (i != 0) {
                    b.append(",\n");
                }
                b.append(toString(e.getKey(), formatter, written, full)).append('=').append(toString(e.getValue(), formatter, written, full));
                i++;
            }
            b.append("}");
            return b.toString();
        }


        return formatter.format(o, written, full);
    }

    public static class ToStringFormatter {

        static final ToStringFormatter STANDARD = new ToStringFormatter();

        protected String isWritten(Object o, IdentityHashMap<Object, Object> written) {
            if (written.put(o, o) == o) {
                return o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o));
            }
            return null;
        }

        protected String toString(Object o, IdentityHashMap<Object, Object> written, boolean full) {
            return DataUtils.toString(o, this, written, full);
        }

        public String format(Object o, IdentityHashMap<Object, Object> written, boolean full) {
            return String.valueOf(o);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T o) {
        if (o == null) {
            return null;
        }
        IdentityHashMap<Object, Object> translations = new IdentityHashMap<Object, Object>();
        List<SubstitutableReference> substitutions = new ArrayList<SubstitutableReference>();
        LinkedList<Object> pending = new LinkedList<Object>();

        pending.add(o);
        for (;;) {
            // Copy each object, while keeping track of references
            Object p = pending.pollFirst();
            if (p == null) {
                break;
            }
            if (translations.containsKey(p)) {
                continue;
            }
            Object c = copy(substitutions, pending, p);
            assert c != null;
            if (translations.put(p, c) != null) {
                assert false;
            }
        }

        // Substitute all references
        for (SubstitutableReference sr : substitutions) {
            Object v = translations.get(sr.value);
            assert v != null;
            if (sr.target instanceof List<?>) {
                ((List<Object>) sr.target).set(sr.index, v);
            }
            else if (sr.target instanceof Object[]) {
                ((Object[]) sr.target)[sr.index] = v;
            }
            else {
                ((ModelData<?>) sr.target).setValue(sr.index, v);
            }
        }

        o = (T) translations.get(o);
        assert o != null;
        return o;
    }

    private static Object copy(List<SubstitutableReference> substitutions, List<Object> pending, Object o) {
        if (o instanceof List<?>) {
            List<?> l;
            if (o instanceof DataList<?>) {
                l = ((DataList<?>) o).shallowCopy();
            }
            else {
                l = copyList((List<?>) o);
            }
            for (int i = 0; i < l.size(); i++) {
                Object v = l.get(i);
                if (v == null) {
                    continue;
                }
                SubstitutableReference sr = new SubstitutableReference();
                sr.target = l;
                sr.value = v;
                sr.index = i;
                substitutions.add(sr);
                pending.add(v);
            }
            return l;
        }

        if (o instanceof Object[]) {
            Object[] l = ((Object[]) o).clone();
            for (int i = 0; i < l.length; i++) {
                Object v = l[i];
                if (v == null) {
                    continue;
                }
                SubstitutableReference sr = new SubstitutableReference();
                sr.target = l;
                sr.value = v;
                sr.index = i;
                substitutions.add(sr);
                pending.add(v);
            }
            return l;
        }

        if (o instanceof ModelData<?>) {
            ModelData<?> d = ((ModelData<?>) o).shallowCopy();
            for (FieldDefinition<?, ?> f : d.getDefinition().getFields()) {
                if (f.getType().isMutable()) {
                    Object v = d.getValue(f.getIndex());
                    if (v == null) {
                        continue;
                    }
                    SubstitutableReference sr = new SubstitutableReference();
                    sr.target = d;
                    sr.value = v;
                    sr.index = f.getIndex();
                    substitutions.add(sr);
                    pending.add(v);
                }
            }
            return d;
        }

        if (o.getClass().isArray()) {
            assert o.getClass().getComponentType().isPrimitive(); // Should have fallen into Object[] case
            int l = Array.getLength(o);
            Object a = Array.newInstance(o.getClass().getComponentType(), l);
            System.arraycopy(o, 0, a, 0, l);
            return a;
        }

        assert false : o.getClass();
        return null;
    }

    private static <T> List<T> copyList(List<T> l) {
        return new ArrayList<T>(l);
    }

    static final class SubstitutableReference {

        /**
         *  Copied object which contain the reference.
         */
        Object target;
        /**
         *  Origin value of the reference.
         */
        Object value;
        /**
         *  Index of the value if in a collection.
         */
        int index;
    }

    public static <D extends ModelData<D>, F> ArrayList<F> extractListedValues(Collection<D> list, FieldDefinition<F, D> field) {
        ArrayList<F> res = new ArrayList<F>();
        for (D o : list) {
            res.add(o.getValue(field));
        }
        return res;
    }

    public static <D extends ModelData<D>, F> void sortListByFieldInCustomOrder(DataList<D> list, final FieldDefinition<F, D> field, List<F> fieldValueListInOrder) {
        final HashMap<F, Integer> fieldToPositionMap = new HashMap<F, Integer>();
        for (int i = 0; i < fieldValueListInOrder.size(); i++) {
            F value = fieldValueListInOrder.get(i);
            fieldToPositionMap.put(value, i);
        }
        int currentPos = 0;
        while (currentPos < list.size()) {
            D item = list.get(currentPos);
            if (item == null) {
                currentPos++;
            }
            else {
                int newPos = fieldToPositionMap.get(item.getValue(field));
                if (newPos == currentPos) {
                    currentPos ++;
                }
                else {
                    D temp = list.get(newPos);
                    list.set(newPos, item);
                    list.set(currentPos, temp);
                }
            }
        }
    }

    public static <D extends ModelData<D>, F> Set<F> extractHashedValues(Collection<D> list, FieldDefinition<F, D> field) {
        Set<F> res = new HashSet<F>();
        for (D o : list) {
            F v = o.getValue(field);
            if (v != null) {
                res.add(v);
            }
        }
        return res;
    }

    public static <D extends ModelData<D>, F> TreeSet<F> extractSortedValues(Collection<D> list, FieldDefinition<F, D> field) {
        TreeSet<F> res = new TreeSet<F>(field);
        for (D o : list) {
            F v = o.getValue(field);
            if (v != null) {
                res.add(v);
            }
        }
        return res;
    }

    /**
     * Remove rows with dup value in field. Uses HashSet, so only supports "native" types.
     * Row kept is the first encountered.
     * @param list List.
     * @param field Field to become unique.
     */
    public static <D extends ModelData<D>> void undup(Collection<D> list, FieldDefinition<?, D> field) {
        if (list.size() <= 1) {
            return;
        }
        Set<Object> set = new HashSet<Object>();
        for (Iterator<D> it = list.iterator(); it.hasNext();) {
            Object v = it.next().getValue(field);
            if (!set.add(v)) {
                it.remove();
            }
        }
    }

    /**
     * Filter elements satisfying "field in values".
     * @param list List.
     * @param field Field to filter on.
     * @param values Values Desired values of field (null value accepted).
     * @return List of source elements having field in values.
     */
    public static <D extends ModelData<D>, F> DataList<D> filter(Collection<D> list, FieldDefinition<F, D> field, F... values) {
        return filter(list, field, false, values);
    }

    @SuppressWarnings({"unchecked"})
    public static <D extends ModelData<D>, F> DataList<D> filterNotNull(Collection<D> list, FieldDefinition<F, D> field) {
        return filter(list, field, true, (F) null);
    }

    public static <D extends ModelData<D>, F> DataList<D> filter(Collection<D> list, FieldDefinition<F, D> field, boolean not, F... values) {
        DataList<D> res = field.getDefinition().createList();
        for (D o : list) {
            F v = o.getValue(field);
            for (F value : values) {
                if (field.equals(value, v) ^ not) {
                    res.add(o);
                    break;
                }
            }
        }
        return res;
    }

    public static <D extends ModelData<D>, F, L extends Collection<D>> L fill(L list, FieldDefinition<F, D> field, F value) {
        for (D d : list) {
            d.setValue(field, value);
        }
        return list;
    }

    public static void limit(List<?> list, int limit) {
        while (list.size() > limit) {
            list.remove(list.size() - 1);
        }
    }

    public static void extractSubList(List<?> list, int start, int end) {
        if (start < list.size()) {
            int loop = 0;
            while (loop < start) {
                list.remove(0);
                loop++;
            }
            limit(list, end - start);
        }
        else {
            list.clear();
        }
    }

    public static <D extends ModelData<D>> void sort(boolean desc, List<D> list, FieldDefinition<?, D>... fields) {
        Collections.sort(list, new FieldsComparator<D>(desc, fields));
    }

    public static class FieldsComparator<D extends ModelData<D>> implements Comparator<D> {

        private final boolean desc;
        private final FieldDefinition<?, D>[] fields;

        public FieldsComparator(boolean dsc, FieldDefinition<?, D>... flds) {
            desc = dsc;
            fields = flds;
        }

        @Override
        public int compare(D a, D b) {
            for (FieldDefinition<?, D> field : fields) {
                int r;
                if (desc) {
                    r = compareField(field, b, a);
                }
                else {
                    r = compareField(field, a, b);
                }
                if (r != 0) {
                    return r;
                }
            }
            return 0;
        }

        private static <F, D extends ModelData<D>> int compareField(FieldDefinition<F, D> field, D a, D b) {
            return field.compare(a.getValue(field), b.getValue(field));
        }
    }

    public static <T> List<List<T>> partitionCollection(Collection<T> collection, int partitionSize) {
        List<List<T>> res = new ArrayList<List<T>>();
        Iterator<T> it = collection.iterator();
        List<T> currentPartition = null;
        while (it.hasNext()) {
            if (currentPartition == null || currentPartition.size() >= partitionSize) {
                currentPartition = new ArrayList<T>();
                res.add(currentPartition);
            }
            currentPartition.add(it.next());
        }
        return res;
    }

    public static <D extends ModelData<D>> List<DataList<D>> partitionDataCollection(DataList<D> collection, int partitionSize) {
        List<DataList<D>> res = new ArrayList<DataList<D>>();
        Iterator<D> it = collection.iterator();
        DataList<D> currentPartition = null;
        while (it.hasNext()) {
            if (currentPartition == null || currentPartition.size() >= partitionSize) {
                currentPartition = collection.getDefinition().createList();
                res.add(currentPartition);
            }
            currentPartition.add(it.next());
        }
        return res;
    }

    public static <T> List<List<T>> partitionCollectionEvenly(Collection<T> collection, int partitionSize) {
        List<List<T>> res = new ArrayList<List<T>>();
        int n = collection.size();
        int np = (n + partitionSize - 1) / partitionSize;
        for (int i = 0; i < np; i++) {
            res.add(new ArrayList<T>());
        }

        Iterator<T> it = collection.iterator();
        long i = 0L;
        while (it.hasNext()) {
            res.get((int) (i * np / n)).add(it.next()); // Use long because int overflow
            i++;
        }
        return res;
    }

    public static <K, V> List<Map<K, V>> partitionMapEvenly(Map<K, V> collection, int partitionSize) {
        List<Map<K, V>> res = new ArrayList<Map<K, V>>();
        int n = collection.size();
        int np = (n + partitionSize - 1) / partitionSize;
        for (int i = 0; i < np; i++) {
            res.add(new HashMap<K, V>());
        }

        Iterator<Entry<K, V>> it = collection.entrySet().iterator();
        long i = 0L;
        while (it.hasNext()) {
            Entry<K, V> e = it.next();
            res.get((int) (i * np / n)).put(e.getKey(), e.getValue()); // Use long because int overflow
            i++;
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    public static <D extends ModelData<D>, F, L extends Collection<D>> L removeDoubloons(L list, FieldDefinition<F, D> field) {
        L filteredList = (L) new ArrayList<D>();
        Collection<F> alreadyList = new ArrayList<F>();
        for (D o : list) {
            F v = o.getValue(field);
            if (!alreadyList.contains(v)) {
                filteredList.add(o);
                alreadyList.add(v);
            }
        }
        return filteredList;
    }


}
