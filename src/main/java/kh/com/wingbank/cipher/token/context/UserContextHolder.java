package kh.com.wingbank.cipher.token.context;

/**
 * Stub — replaced by the real wing-token library in deployed environments.
 * Thread-local holder for the cipher JWT context object.
 */
public class UserContextHolder {

    private static final ThreadLocal<Object> context = new ThreadLocal<>();

    public static Object getCurrentContext() {
        return context.get();
    }

    public static void setCurrentContext(Object ctx) {
        context.set(ctx);
    }

    public static void clear() {
        context.remove();
    }
}
