package io.gaultier.modeling.model.data;

import java.lang.reflect.Array;

@SuppressWarnings("rawtypes") class EnumEncoder<T> {
	
	private T[] allValues;
	private int res = 0;

	EnumEncoder(T[] allValues) {
		this.allValues = allValues;
	}
	
	public int make() {
		return res;
	}

	EnumEncoder add(T added) {
		if (added != null) {
			for (int i = 0; i < allValues.length; i++) {
				T v = allValues[i];
				if (v == added) {
					res |= ( 1 << i );
					break;
				}
			}
		}
		
		return this;
	}	
	
	public T[] decode(int toDecode) {
		@SuppressWarnings("unchecked")
		T[] result = (T[]) Array.newInstance(allValues.getClass().getComponentType(), allValues.length);
		
		int count = 0;
		for (int i = 0; i < allValues.length; i++) {
			T t = allValues[i];
			if ((toDecode & ( 1 << i) ) == 1) {
				result[count++] = t;
			}
		}
		return result;
	}
}