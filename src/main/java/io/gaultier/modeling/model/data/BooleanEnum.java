package io.gaultier.modeling.model.data;

public enum BooleanEnum {
    FALSE,
    TRUE;

    public static BooleanEnum get(Boolean value) {
        if (value == null) {
            return FALSE;
        }
        return value ? TRUE : FALSE;
    }

    public boolean get() {
        return TRUE.equals(this);
    }

    public int toInt() {
        return get() ? 1 : 0;
    }
}