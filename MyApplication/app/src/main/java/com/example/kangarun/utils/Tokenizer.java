package com.example.kangarun.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Runyao Wang u6812566
 */
public class Tokenizer {
    // Tokenize the query and return null if invalid
    public static Map<String, String> tokenize(String input) {
        // If query doesn't contains =, it is a normal search
        if (!input.contains("=")) {
            return null;
        }
        // Tokenize by semicolon
        Map<String, String> tokens = new HashMap<>();
        String[] parts = input.split(";");
        for (String part : parts) {
            String[] keyValue = part.split("=");
            if (keyValue.length == 2) {
                tokens.put(keyValue[0].trim(), keyValue[1].trim());
            } else {
                return null;
            }
        }
        return tokens;
    }

}
