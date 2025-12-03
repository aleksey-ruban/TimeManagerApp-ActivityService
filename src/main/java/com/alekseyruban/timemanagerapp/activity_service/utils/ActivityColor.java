package com.alekseyruban.timemanagerapp.activity_service.utils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum ActivityColor {
    RED,
    CORAL,
    ORANGE,
    AMBER,
    GREEN,
    TEAL,
    LIGHT_BLUE,
    BLUE,
    INDIGO,
    PURPLE,
    LILAC,
    PINK,
    GRAY,
    OLIVE,
    BROWN;

    private static final Set<String> COLORS = Arrays.stream(ActivityColor.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    public static boolean isValidColor(String color) {
        return color != null && COLORS.contains(color.toUpperCase());
    }
}
