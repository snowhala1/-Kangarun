package com.example.kangarun;


import java.util.ArrayList;
import java.util.List;

/**
 * @author Heng Sun u7611510
 */
public class LoginState {
    private static LoginState instance;
    private String id;

    private LoginState() {

    }

    /**
     * Singleton instance accessor
     * @return instance
     */
    public static LoginState getInstance() {
        if (instance == null) {
            instance = new LoginState();
        }
        return instance;
    }

    /**
     * Getter for user ID
     * @return id user id
     */
    public String getUserId() {
        return id;
    }

    /**
     * Setter for user ID
     * @param id user id
     */
    public void setUserId(String id) {
        this.id = id;
    }

}

