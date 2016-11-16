package io.gaultier.modeling.model.data;

public enum Syntax {
	JSON,
	HTML,
	AMF;
	
	public final static int ALL = encode(JSON);

	public static int encode(Syntax... enums) {
		EnumEncoder<Syntax> builder = new EnumEncoder<Syntax>(values());
		if (enums != null) {
			for (Syntax json2:enums) {
				builder.add(json2);
			}
		}
		return builder.make();
	}

	public static Syntax[] decode(int syntaxesEncoded) {
		return new EnumEncoder<Syntax>(values()).decode(syntaxesEncoded);
	}
}
