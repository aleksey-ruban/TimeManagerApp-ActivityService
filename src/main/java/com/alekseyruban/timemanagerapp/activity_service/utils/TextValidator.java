package com.alekseyruban.timemanagerapp.activity_service.utils;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class TextValidator {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 50;

    private static final Pattern BASIC_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s\\-']+$");

    private static final List<String> FORBIDDEN_NAMES = List.of(
            "admin", "root", "test"
    );

    public boolean isValidName(String text) {
        return isValidText(text, MIN_LENGTH, MAX_LENGTH, false);
    }

    public boolean isValidCategory(String text) {
        return isValidText(text, 1, 30, true);
    }

    public boolean isValidText(String text, int minLength, int maxLength, boolean allowNumbers) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String trimmed = text.trim();

        if (trimmed.length() < minLength || trimmed.length() > maxLength) {
            return false;
        }

        return BASIC_PATTERN.matcher(trimmed).matches();
    }
}