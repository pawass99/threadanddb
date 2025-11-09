package org.example;

import org.example.config.DataSourceFactory;
import org.example.dao.TaskDao;
import org.example.db.DatabaseInitializer;
import org.example.service.TaskService;
import org.example.ui.Theme;
import org.example.ui.TodoAppFrame;

import javax.sql.DataSource;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) throws Exception {
        DataSource dataSource = DataSourceFactory.getDataSource();
        new DatabaseInitializer(dataSource).ensureSchema();

        TaskDao taskDao = new TaskDao(dataSource);
        TaskService taskService = new TaskService(taskDao);

        Theme.apply();
        SwingUtilities.invokeLater(() -> {
            TodoAppFrame frame = new TodoAppFrame(taskService);
            frame.setVisible(true);
        });
    }
}
