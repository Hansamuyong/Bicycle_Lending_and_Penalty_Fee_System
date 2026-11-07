package com.scc.model;

public record ActivityLog(int log_id, int user_id, String action_type,
                          String description, String timestamp) {
}