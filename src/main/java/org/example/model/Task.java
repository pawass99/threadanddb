package org.example.model;

import java.time.Instant;

public record Task(long id, String description, boolean completed, Instant createdAt) {
}
