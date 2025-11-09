package org.example.dao;

import org.example.model.Task;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskDao {
    private final DataSource dataSource;

    public TaskDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Task insertTask(String description) throws SQLException {
        String sql = "INSERT INTO tasks(description) VALUES (?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, description);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    return findById(id).orElseThrow(() -> new SQLException("Inserted task not found"));
                }
            }
        }
        throw new SQLException("Insert succeeded but no key returned");
    }

    public List<Task> findAll() throws SQLException {
        String sql = "SELECT id, description, completed, created_at FROM tasks ORDER BY id";
        List<Task> tasks = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tasks.add(mapRow(rs));
            }
        }
        return tasks;
    }

    public Optional<Task> findById(long id) throws SQLException {
        String sql = "SELECT id, description, completed, created_at FROM tasks WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        }
    }

    public void updateCompleted(long id, boolean completed) throws SQLException {
        String sql = "UPDATE tasks SET completed = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, completed);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void deleteTask(long id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String description = rs.getString("description");
        boolean completed = rs.getBoolean("completed");
        Timestamp created = rs.getTimestamp("created_at");
        Instant createdAt = created == null ? Instant.now() : created.toInstant();
        return new Task(id, description, completed, createdAt);
    }
}
