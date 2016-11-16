package io.gaultier.modeling.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.gaultier.modeling.model.data.DataDefinition;
import io.gaultier.modeling.model.data.DataList;
import io.gaultier.modeling.model.data.DataType;
import io.gaultier.modeling.model.data.FieldDefinition;
import io.gaultier.modeling.model.data.FieldType;
import io.gaultier.modeling.model.data.ModelData;

public class JsonDataParser {

	private final Map<DataType, String> nullableSubstitution = new HashMap<DataType, String>();

	private final Map<Object, DataTypeConverter> substitutes = new HashMap<Object, DataTypeConverter>();


	public <D extends ModelData<D>> String dataToJson(D data) {
		return dataToJsonOnly(data, data.getDefinition().getFields());
	}

	Map<DataType, DataTypeConverter> baseConverter = DefaultJsonConverter.make();

	public JsonDataParser withNullableSubstitution(DataType t, String s) {
		nullableSubstitution.put(t, s);
		return this;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public <D extends ModelData<D>, T> String dataToJsonOnly(D data, Collection<FieldDefinition<?, D>> fields) {
		assert data != null;
		if (data != null) {
			data.prepareJsonSerialization();
		}
		StringBuilder b = new StringBuilder("{");
		boolean firstWritten = true;
		Iterator<FieldDefinition<?, D>> it = fields.iterator();
		while (it.hasNext()) {
			FieldDefinition<?, D> f = it.next();
			if (!f.isJson()) {
				continue;
			}
			Object value = data.getValue(f);
			String vString = null;
			FieldType ftype = f.getType();
			if (value == null) {
				vString = nullableSubstitution.get(ftype.getType());
			}
			else {
				if (ftype.isEnum()) {
					vString = "" + ((Enum) value).ordinal();
				}
				else {
					DataType type = ftype.getType();
					if (type != null) {
						vString = obtainConverter(f).encode(value);
					}
					else {
						DataDefinition<?> o = ftype.getObjectType();
						if (ftype.isList()) {
							DataList<?> l = (DataList<?>) value;
							vString = dataListToJson(l);
						}
						else if (o != null) {
							vString = dataToJson((ModelData) value);
						}
						else {
							assert false;
						}
					}
				}
			}
			if (vString != null) {
				if (vString != null && !firstWritten) {
					b.append(",");
				}
				b.append(JSONObject.quote(f.getName()) + ":" + vString);
				firstWritten = false;
			}
		}
		b.append("}");
		return b.toString();
	}

	@SuppressWarnings("rawtypes") 
	private DataTypeConverter obtainConverter(FieldDefinition f) {
		if (substitutes.containsKey(f)) {
			return substitutes.get(f);
		}
		DataType type = f.getType().getType();
		if (substitutes.containsKey(type)) {
			return substitutes.get(type);
		}
		return baseConverter.get(type);
	}

	public <L extends ModelData<L>> String dataListToJson(List<L> l) {
		StringBuilder b = new StringBuilder();
		b.append("[");
		Iterator<L> itl = l.iterator();
		boolean isFirst = true;
		while (itl.hasNext()) {
			L sub = itl.next();
			String dataS = dataToJson(sub);
			if (dataS != null && !isFirst) {
				b.append(",");
			}
			b.append(dataS);
			isFirst = false;
		}
		b.append("]");
		return b.toString();
	}


	public <T extends ModelData<T>> T dataFromJson(JSONObject input, DataDefinition<T> definition) throws JSONException {
		if (input == null) {
			return null;
		}
		T res = definition.createData();
		List<FieldDefinition<?, T>> fields = definition.getFields();
		ListIterator<FieldDefinition<?, T>> it = fields.listIterator();
		while (it.hasNext()) {
			FieldDefinition<?, T> f = it.next();
			if (!input.has(f.getName())) {
				continue;
			}
			final Object v = input.get(f.getName());
			Object value = null;
			if (v != null && !JSONObject.NULL.equals(v)) {
				final FieldType type = f.getType();

				if (type.isList()) {
					assert v instanceof JSONArray : v + " for " + f;
				value = dataListFromJson((JSONArray) v, f.getType().getObjectType());
				}
				else if (type.getObjectType() != null) {
					assert v instanceof JSONObject : v + " for " + f;
					value = dataFromJson((JSONObject) v, type.getObjectType());
				}
				else if (type.isEnum()) {
					value = f.getType().enumFromAmf((Integer) v);
				}
				else {
					DataType dtyp = type.getType();
					assert dtyp != null : f;
					DataTypeConverter conv = obtainConverter(f);
					assert conv != null : f;
					value = conv.decode(v + "");
				}
			}

			res.setValue(f, value);
		}
		return res;
	}

	public <L extends ModelData<L>> DataList<L> dataListFromJson(JSONArray arr, DataDefinition<L> dataDefinition) throws JSONException {
		int l = arr.length();
		DataList<L> list = dataDefinition.createList();
		for (int i = 0; i < l; i++) {
			list.add(dataFromJson(arr.getJSONObject(i), dataDefinition));
		}
		return list;
	}

	public JsonDataParser withSubstitute(Object type, DataTypeConverter converter) {
		substitutes.put(type, converter);
		return this;
	}
	

}
