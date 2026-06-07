package com.example.persona.config;

import com.example.persona.dto.UserContext;

public class UserContextHolder {
    private static final ThreadLocal<UserContext> userContext = new ThreadLocal<>();

    public static UserContext getCurrentContext() {
        return userContext.get();
    }

    public static void setUserContext(UserContext context) {
        userContext.set(context);
    }

    public static void clearUserContext() {
        userContext.remove();
    }
}
