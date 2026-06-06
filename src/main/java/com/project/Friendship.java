package com.project;

public class Friendship {
    private final String firstUsername;
    private final String secondUsername;

    public Friendship(String firstUsername, String secondUsername) {
        this.firstUsername = firstUsername.trim().toLowerCase();
        this.secondUsername = secondUsername.trim().toLowerCase();
    }

    public String getFirstUsername() {
        return firstUsername;
    }

    public String getSecondUsername() {
        return secondUsername;
    }
}
