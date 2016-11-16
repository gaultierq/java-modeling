package io.gaultier.modeling.util.base;

public class WrappedException extends RuntimeException {

    public WrappedException(Throwable t) {
        super(t);
    }

    public WrappedException(String msg, Throwable t) {
        super(msg, t);
    }

    public WrappedException(Throwable t, Object attachment) {
        super(t.getMessage() + "\nAttachment: " + attachment, t);
    }

    public static void throwIt(Throwable t) {
        if (t == null) {
            return;
        }
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
        if (t instanceof Error) {
            throw (Error) t;
        }
        throw new WrappedException(t);
    }
}
