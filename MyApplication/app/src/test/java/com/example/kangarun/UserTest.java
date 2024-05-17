package com.example.kangarun;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

public class UserTest {
    private User user;

    @Before
    public void setUp() {
        user = new User();
        user.setUsername("testUsername");
        user.setEmail("testEmail");
        user.setHeight(133);
        user.setWeight(70);
    }

    @Test
    public void testAttributes() {
        Assert.assertEquals("testUsername", user.getUsername());
        Assert.assertEquals("testEmail", user.getEmail());
        Assert.assertEquals(133, user.getHeight(), 0.0);
        Assert.assertEquals(70, user.getWeight(), 0.0);
    }

    @Test
    public void testConstructor() {
        User user1 = new User("username", "email", "Female");
        Assert.assertEquals("username", user1.getUsername());
        Assert.assertEquals("email", user1.getEmail());
        Assert.assertEquals("Female", user1.getGender());
    }

    @Test
    public void testCompareGender() {
        user.setGender("Male");
        Assert.assertTrue(user.compareGender("Male"));
        Assert.assertFalse(user.compareGender("Female"));
        Assert.assertTrue(user.compareGender("All Genders"));
        Assert.assertFalse(user.compareGender("Other"));
        user.setGender("AndroidStudio");
        Assert.assertTrue(user.compareGender("All Genders"));
        Assert.assertTrue(user.compareGender("Other"));

        try {
            user.compareGender(null);
            Assert.fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            // Expected exception
        }
    }

    @Test
    public void testCompareToLesser() {
        User otherUser = new User();
        otherUser.setUsername("aaaUsername");
        Assert.assertTrue("Expected to be greater", user.compareTo(otherUser) > 0);
        otherUser.setUsername("zzzUsername");
        Assert.assertTrue("Expected to be lesser", user.compareTo(otherUser) < 0);
    }

}
