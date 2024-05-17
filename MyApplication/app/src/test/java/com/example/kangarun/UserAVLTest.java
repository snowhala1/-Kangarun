package com.example.kangarun;

import com.example.kangarun.utils.UserAVLTree;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAVLTest {

    private UserAVLTree tree;

    @Before
    public void setup() {
        tree = new UserAVLTree();
        tree.insert(new User("John Doe", "john.doe@example.com", "male"));
        tree.insert(new User("Jane Doe", "jane.doe@example.com", "female"));
        tree.insert(new User("Alice Johnson", "alice@example.com", "female"));
        tree.insert(new User("Bob Smith", "bob@example.com", "male"));
        tree.insert(new User("zzzz", "zzzz@dad.com", "alien"));
    }

    @Test
    public void testSearchPartialWithMatch() {
        List<User> results = tree.searchPartial("doe");
        assertEquals(2, results.size());
    }

    @Test
    public void testSearchPartialNoMatch() {
        List<User> results = tree.searchPartial("xyz");
        assertTrue(results.isEmpty());
    }

    @Test
    public void testSearchTokenMultipleCriteria() {
        Map<String, String> query = new HashMap<>();
        query.put("username", "doe");
        query.put("email", "jane.do");
        query.put("gender", "f");
        List<User> results = tree.searchToken(query);
        assertEquals(1, results.size());
        assertEquals("Jane Doe", results.get(0).getUsername());
    }

    @Test
    public void testSearchTokenSingleCriteria() {
        Map<String, String> query = new HashMap<>();
        query.put("gender", "m");
        List<User> results = tree.searchToken(query);
        assertEquals(2, results.size());
        query.clear();
        query.put("gender", "o");
        results = tree.searchToken(query);
        assertEquals(1, results.size());
    }

    @Test
    public void testSearchTokenNoCriteria() {
        Map<String, String> query = new HashMap<>();
        List<User> results = tree.searchToken(query);
        assertEquals(5, results.size());
    }

    @Test
    public void testSearchTokenNoMatch() {
        Map<String, String> query = new HashMap<>();
        query.put("username", "xyz");
        List<User> results = tree.searchToken(query);
        assertTrue(results.isEmpty());
    }
}
