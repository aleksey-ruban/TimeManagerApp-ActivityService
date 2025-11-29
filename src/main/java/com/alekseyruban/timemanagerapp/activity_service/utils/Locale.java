package com.alekseyruban.timemanagerapp.activity_service.utils;

public enum Locale {
    RU("ru", "Russian"),
    EN("en", "English");

    private final String code;
    private final String displayName;

    Locale(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Locale fromCode(String code) {
        for (Locale locale : values()) {
            if (locale.code.equalsIgnoreCase(code)) {
                return locale;
            }
        }
        throw new IllegalArgumentException("Unsupported locale: " + code);
    }
}