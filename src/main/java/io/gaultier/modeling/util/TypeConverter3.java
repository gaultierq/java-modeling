package io.gaultier.modeling.util;

import io.gaultier.modeling.model.data.DataType;

public interface TypeConverter3 {

	String toJson(Object value);

	Object fromJson(DataType type, String json);
}