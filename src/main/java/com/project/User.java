package com.project;

import java.util.Objects;

public class User {
    private final int id;
    private final String name;
    private final String username;
    private final String major;
    private final String interest;

    public User(int id, String name, String username, String major, String interest) {
        this.id = id;
        this.name = name.trim();
        this.username = username.trim().toLowerCase();
        this.major = major.trim();
        this.interest = interest.trim();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getMajor() {
        return major;
    }

    public String getInterest() {
        return interest;
    }

    @Override
    public String toString() {
        return id + ". " + name + " (@" + username + ") - " + major + ", likes " + interest;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof User user)) {
            return false;
        }

        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
