package io.gaultier.modeling.model.data;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.gaultier.modeling.util.base.WrappedException;

public abstract class ModelData<T extends ModelData<T>> {

	private final Object[] values;

	protected ModelData(int valuesCount) {
		values = new Object[valuesCount];
	}

	protected final Object getValue(int index) {
		return values[index];
	}

	public void setValue(int index, Object value) {
		if (value != null) {
			getDefinition().getField(index).getType().checkType(value);
		}
		values[index] = value;
	}

	@SuppressWarnings("unchecked")
	public final <FT> FT getValue(FieldDefinition<FT, T> field) {
		assert field.getDefinition() == getDefinition() : field + " on " + getDefinition();
		return (FT) values[field.getIndex()];
	}

	public <FT> void setValue(FieldDefinition<FT, T> field, Object value) {
		assert field.getDefinition() == getDefinition() : field.getDefinition().getDataClass().getSimpleName() + "." + field.getName() + " not in " + getClass().getSimpleName();
		values[field.getIndex()] = value;
	}

	public abstract DataDefinition<T> getDefinition();

	public final boolean entityEquals(T o) {
		if (o == this) {
			return true;
		}
		Collection<FieldDefinition<?, T>> pk = getDefinition().getPrimaryKey();
		if (pk.isEmpty()) {
			return false;
		}
		for (FieldDefinition<?, T> f : pk) {
			Object va = getValue(f);
			Object vb = o.getValue(f);
			if (va == null || vb == null) {
				return false;
			}
			if (!f.getType().equals(va, vb)) {
				return false;
			}
		}
		return true;
	}

	public final int entityHashCode() {
		int h = 1;
		Collection<FieldDefinition<?, T>> pk = getDefinition().getPrimaryKey();
		for (FieldDefinition<?, T> f : pk) {
			Object va = getValue(f);
			h = h * 31 + f.getType().hashCode(va);
		}
		return h;
	}

	public final String toString(boolean deep) {
		return toString(deep, false);
	}

	public final String toString(boolean deep, boolean full) {
		return toString(deep ? new IdentityHashMap<Object, Object>() : null, full);
	}

	final String toString(IdentityHashMap<Object, Object> written, boolean full) {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getSimpleName()).append('@').append(Integer.toHexString(hashCode()));
		if (written == null) {
			return b.toString();
		}
		if (written.put(this, this) == this) {
			return b.toString();
		}
		b.append('{');
		List<FieldDefinition<?, T>> fields = getDefinition().getFields();
		for (int i = 0; i < fields.size(); i++) {
			FieldDefinition<?, T> f = fields.get(i);
			if (i != 0) {
				b.append(", ");
			}
			b.append(f.getName()).append('=');
			Object v = getValue(f);
			if (v == null) {
				b.append("null");
			} else {
				b.append(f.getType().toString(v, written, full));
			}
		}
		b.append('}');
		return b.toString();
	}

	public String toShortString() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getSimpleName());
		if ("Data".equals(b.substring(b.length() - 4))) {
			b.setLength(b.length() - 4);
		}
		b.append("<");
		boolean first = true;
		for (FieldDefinition<?, T> f : getDefinition().getPrimaryKey()) {
			if (first) {
				first = false;
			}
			else {
				b.append(", ");
			}
			b.append(f.getType().toString(getValue(f), null, false));
		}
		b.append(">");
		return b.toString();
	}

	/**
	 * Shallow copy of this object.
	 * @return A new object of the same type with the same values.
	 */
	public T shallowCopy() {
		T o = getDefinition().createData();
		System.arraycopy(values, 0, ((ModelData<?>) o).values, 0, values.length);
		return o;
	}

	public void copyFields(T from) {
		copyFields(from, getDefinition().getFields());
	}

	public void copyFields(T from, Collection<FieldDefinition<?, T>> fields) {
		for (FieldDefinition<?, T> f : fields) {
			setValue(f, from.getValue(f));
		}
	}

	protected final void prepareForSerializationSafe() {
		try {
			prepareForSerialization();
		}
		catch (Throwable t) {

			WrappedException.throwIt(t);
		}
	}

	/**
	 * Called before AMF serialization.
	 * Warning: runs outside of transaction, do not use DB access.
	 */
	protected void prepareForSerialization() {
	}

	public void prepareJsonSerialization() {

	}

	public <F> void writeAmf(FieldDefinition<F, T> field, ObjectOutput out) throws IOException {
		field.getType().writeAmf(out, getValue(field));
	}

	public <F> void readAmf(FieldDefinition<F, T> field, ObjectInput in) throws IOException, ClassNotFoundException {
		setValue(field, field.getType().readAmf(in));
	}

	public final Map<String, Object> valuesToMap() {
		Map<String, Object> res = new LinkedHashMap<String, Object>();
		for (FieldDefinition<?, T> f : getDefinition().getFields()) {
			res.put(f.getName(), getValue(f));
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	public final T setValues(Map<String, Object> map) {
		for (FieldDefinition<?, T> f : getDefinition().getFields()) {
			if (map.containsKey(f.getName())) {
				setValue(f, map.get(f.getName()));
			}
		}
		return (T) this;
	}


	/*protected static final void tableIsInUrlBase(DataDefinition<?> def) {
        def.setLocations(StatementLocation.URL, StatementLocation.URL);
        String db = OysterApplication.get().getServerConfig().getUrlDatabase();
        if (db != null && !db.equals(".")) {
            def.setTableName(db + '.' + def.getTableName());
        }
    }*/

	public String diff(ModelData<T> other) {
		String diff = null;
		for (FieldDefinition<?, T> f : getDefinition().getFields()) {
			Object fin = getValue(f);
			Object ini = other.getValue(f);
			if (!areEquals(fin, ini)) {
				diff = (diff == null ? "" : diff) + f.getName() + ":"+ fin + "!="+ ini + ","; 
			}
		}
		return diff;
	}

	public boolean valuesEquals(ModelData<T> other) {
		return diff(other) == null;
	}

	public static boolean areEquals(Object left, Object right) {
		return left == right || (left != null && left.equals(right));
	}
}
