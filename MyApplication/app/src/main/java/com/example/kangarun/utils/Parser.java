package com.example.kangarun.utils;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Runyao Wang u6812566
 */
public class Parser {
    private static final String GENDER_REGEX = "^[mfo]$";

    // Parse the query into map and return null if invalid
    public static Map<String, String> parse(Map<String, String> tokens) throws IllegalArgumentException {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || value == null){
                return null;
            }
            switch (key) {
                case "name":
                    result.put("username", value);
                    break;
                case "email":
                    result.put("email", value);
                    break;
                case "gender":
                    if (validateGender(value)) {
                        result.put("gender", value);
                    } else {
                        return null;
                    }
                    break;
                default:
                    return null;
            }
        }

        if (result.isEmpty()) {
            return null;
        }

        return result;
    }

    private static boolean validateGender(String gender) {
        return gender.matches(GENDER_REGEX);
    }

    // used for test
    public static Map<String, String> processQuery(String query) {
        Map<String, String> tokens = Tokenizer.tokenize(query);
        if (tokens == null) {
            return null;
        } else {
            return Parser.parse(tokens);
        }
    }
}
