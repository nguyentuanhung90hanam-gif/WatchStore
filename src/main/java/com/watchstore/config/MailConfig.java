package com.watchstore.config;

public final class MailConfig {
    public static final String HOST = env("MAIL_HOST", "WATCHSTORE_MAIL_HOST", "smtp.gmail.com");
    public static final int PORT = Integer.parseInt(env("MAIL_PORT", "WATCHSTORE_MAIL_PORT", "587"));
    public static final String USERNAME = env("MAIL_USERNAME", "WATCHSTORE_MAIL_USER", "");
    public static final String PASSWORD = env("MAIL_PASSWORD", "WATCHSTORE_MAIL_PASSWORD", "");
    public static final String FROM = env("MAIL_FROM", "WATCHSTORE_MAIL_FROM", USERNAME);
    private MailConfig() {}

    public static boolean isConfigured() {
        return USERNAME != null && !USERNAME.isBlank() && PASSWORD != null && !PASSWORD.isBlank();
    }

    public static jakarta.mail.Session createMailSession() {
        java.util.Properties props = new java.util.Properties();
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", String.valueOf(PORT));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.ssl.trust", HOST);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        return jakarta.mail.Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
    }

    private static String env(String primary, String secondary, String fallback) {
        String value = System.getenv(primary);
        if (value != null && !value.isBlank()) return value;
        value = System.getenv(secondary);
        if (value != null && !value.isBlank()) return value;
        value = System.getProperty(primary);
        if (value != null && !value.isBlank()) return value;
        value = System.getProperty(secondary);
        if (value != null && !value.isBlank()) return value;
        return fallback;
    }
}
