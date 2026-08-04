package com.watchstore.config;

public final class MailConfig {
    public static final String HOST = System.getenv().getOrDefault("WATCHSTORE_MAIL_HOST", "smtp.gmail.com");
    public static final int PORT = Integer.parseInt(System.getenv().getOrDefault("WATCHSTORE_MAIL_PORT", "587"));
    public static final String USERNAME = System.getenv().getOrDefault("WATCHSTORE_MAIL_USER", "");
    public static final String PASSWORD = System.getenv().getOrDefault("WATCHSTORE_MAIL_PASSWORD", "");
    private MailConfig() {}
}
