package io.gaultier.modeling.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import io.gaultier.modeling.model.Id;
import io.gaultier.modeling.model.Version;
import io.gaultier.modeling.model.data.DataType;
import io.gaultier.modeling.util.base.ByteArray;
import io.gaultier.modeling.util.base.ByteBufferStream;

final class DefaultJsonConverter {

	private static final class WrappedConverter implements DataTypeConverter {
		private final DataType t;

		private WrappedConverter(DataType t) {
			this.t = t;
		}

		@Override
		public String encode(Object value) {
			return convertTo(t, value);
		}

		@Override
		public Object decode(String encoded) {
			return convertFrom(t, encoded);
		}
	}

	public static Map<DataType, DataTypeConverter> make() {
		HashMap<DataType, DataTypeConverter> res = new HashMap<>();
		for (final DataType t : DataType.values()) {
			res.put(t, new WrappedConverter(t));
		}
		return res;
	}


	private static String convertTo(DataType type, Object value) {
		switch (type) {
		case INTEGER:
		case LONG:
		case ID:
		case VERSION:
		case DOUBLE:
		case BOOLEAN:
			return value.toString();
		case DATETIME:
			//				if (dateFormater != null) {
			//					return dateFormater.format((Date) value);
			//				}
			return "" + ((Date) value).getTime();
		case STRING:
			return JSONObject.quote(value.toString());
		case BINARY:
			return JSONObject.quote(((ByteArray) value).toHexRepresentation(false));
		}
		assert false;
		return null;
	}

	private static Object convertFrom(DataType type, String in) {
		switch (type) {
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
			return new Date(Long.valueOf(in));
		case BINARY:
			return ByteBufferStream.fromHexString(in);
		case BOOLEAN:
			return Boolean.valueOf(in);
		}
		assert false;
		return null;
	}	

}