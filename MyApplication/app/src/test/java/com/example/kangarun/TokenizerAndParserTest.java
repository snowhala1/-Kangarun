package com.example.kangarun;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.kangarun.utils.Tokenizer;
import com.example.kangarun.utils.Parser;


import java.util.Map;

public class TokenizerAndParserTest {

    @Test
    public void testValidInput() {
        String input = "name=JohnDoe;email=john.doe@example.com;gender=m";
        Map<String, String> tokens = Tokenizer.tokenize(input);
        assertEquals("JohnDoe", tokens.get("name"));
        assertEquals("JohnDoe", tokens.get("name"));
        assertEquals("john.doe@example.com", tokens.get("email"));
        assertEquals("m", tokens.get("gender"));

    }

    @Test
    public void testEmptyInput() {
        String input = "";
        Map<String, String> tokens = Tokenizer.tokenize(input);
        assertNull(tokens);
    }




    @Test
    public void testValidParsing() {
        Map<String, String> tokens = Map.of(
                "name", "JohnDoe",
                "email", "john.doe@example.com",
                "gender", "m"
        );
        Map<String, String> parsedData = Parser.parse(tokens);

        assertEquals("JohnDoe", parsedData.get("username"));
        assertEquals("john.doe@example.com", parsedData.get("email"));
        assertEquals("m", parsedData.get("gender"));

    }


    @Test
    public void testInvalidGender() {
        Map<String, String> tokens = Map.of(
                "gender", "x"
        );
        assertNull(Parser.parse(tokens));
    }

    @Test
    public void mainTest() {
        try {
            String query = "name=JohnDoe;email=john.doe@example.com;gender=m";
            Map<String, String> tokens = Tokenizer.tokenize(query);
            Map<String, String> parsedData = Parser.parse(tokens);
            System.out.println("Parsed Data: " + parsedData);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    @Test
    public void testValidQuery() {
        String query = "name=JohnDoe;email=john.doe@example.com;gender=m";
        try {
            Map<String, String> parsedData = Parser.processQuery(query);
            assertEquals("JohnDoe", parsedData.get("username"));
            assertEquals("john.doe@example.com", parsedData.get("email"));
            assertEquals("m", parsedData.get("gender"));

        } catch (IllegalArgumentException e) {
            fail("Processing failed with valid input");
        }
    }
    @Test
    public void testValidShortQuery() {
        String query = "name=JohnDoe;email=john.doe";
        try {
            Map<String, String> parsedData = Parser.processQuery(query);
            assertEquals("JohnDoe", parsedData.get("username"));
            assertEquals("john.doe", parsedData.get("email"));


        } catch (IllegalArgumentException e) {
            fail("Processing failed with valid input");
        }
    }

    @Test
    public void testInvalidGenderQuery() {
        String query = "name=JohnDoe;email=john.doe@example.com;gender=x";
        assertNull(Parser.processQuery(query));
    }

    @Test
    public void testInvalidFormatQuery() {
        String query = "name=JohnDoe;email";
        assertNull(Parser.processQuery(query));
    }
}
