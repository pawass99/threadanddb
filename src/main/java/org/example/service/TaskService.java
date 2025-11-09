package org.example.service;

import org.example.dao.TaskDao;
import org.example.model.Task;

import java.sql.SQLException;
import java.util.List;

public class TaskService {
    private final TaskDao taskDao;

    public TaskService(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    public List<Task> getAllTasks() throws SQLException {
        return taskDao.findAll();
    }

    public Task createTask(String description) throws SQLException {
        return taskDao.insertTask(description);
    }

    public void setCompleted(long id, boolean completed) throws SQLException {
        taskDao.updateCompleted(id, completed);
    }

    public void deleteTask(long id) throws SQLException {
        taskDao.deleteTask(id);
    }
}
