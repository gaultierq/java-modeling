package io.gaultier.modeling.util;

public interface DataTypeConverter {

	String encode(Object value);

	Object decode(String encoded);
}