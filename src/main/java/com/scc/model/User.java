package com.scc.model;

public record User(int user_id, String first_name, String last_name, String username,
                   String password_hash, String role, String status) {

    public String fullName() {
        return first_name + " " + last_name;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - Role: %s | Status: %s",
                user_id, fullName(), username, role, status);
    }
}