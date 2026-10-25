package com.uniswap.gui;

/** Remembers who is currently logged into the desktop app. */
public final class Session {
    private static Long userId;
    private static String fullName;

    private Session() {
    }

    public static void set(Long id, String name) {
        userId = id;
        fullName = name;
    }

    public static void clear() {
        userId = null;
        fullName = null;
        ApiClient.clearAuthToken();
    }

    public static Long getUserId() { return userId; }
    public static String getFullName() { return fullName; }
    public static boolean isLoggedIn() { return userId != null; }
}
