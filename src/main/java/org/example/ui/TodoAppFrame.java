package org.example.ui;

import org.example.model.Task;
import org.example.service.TaskService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TodoAppFrame extends JFrame {
    private final TaskService taskService;
    private final ExecutorService executor;
    private final ScheduledExecutorService clockExecutor;
    private final DateTimeFormatter clockFormatter = DateTimeFormatter.ofPattern("HH:mm:ss - dd MMM yyyy");

    private final DefaultListModel<Task> listModel = new DefaultListModel<>();
    private final JList<Task> taskList = new JList<>(listModel);
    private final JTextField inputField = new JTextField();
    private final JButton addButton = new JButton("Tambah");
    private final JButton refreshButton = new JButton("Muat ulang");
    private final JButton toggleButton = new JButton("Tandai Selesai");
    private final JButton deleteButton = new JButton("Hapus");
    private final JLabel clockLabel = new JLabel("Waktu sekarang: -");

    public TodoAppFrame(TaskService taskService) {
        super("Todo App - Thread & JDBC Demo");
        this.taskService = taskService;
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "todo-worker");
            t.setDaemon(true);
            return t;
        });
        this.clockExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "clock-worker");
            t.setDaemon(true);
            return t;
        });
        initUi();
        loadTasksAsync();
        startClock();
    }

    private void initUi() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(600, 450);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.MANTLE);

        taskList.setCellRenderer(new TaskListCellRenderer());
        taskList.setBackground(Theme.SURFACE0);
        taskList.setForeground(Theme.TEXT);
        taskList.setSelectionBackground(Theme.LAVENDER);
        taskList.setSelectionForeground(Theme.BASE);
        taskList.setBorder(BorderFactory.createLineBorder(Theme.SURFACE2));
        taskList.setOpaque(true);

        JPanel topPanel = new JPanel(new BorderLayout(8, 0));
        topPanel.setBackground(Theme.MANTLE);
        inputField.setBackground(Theme.SURFACE0);
        inputField.setForeground(Theme.TEXT);
        inputField.setCaretColor(Theme.GREEN);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.SURFACE2),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        topPanel.add(inputField, BorderLayout.CENTER);
        styleButton(addButton, Theme.GREEN, Theme.BASE);
        topPanel.add(addButton, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Theme.MANTLE);
        styleButton(refreshButton, Theme.LAVENDER, Theme.BASE);
        styleButton(toggleButton, Theme.PEACH, Theme.BASE);
        styleButton(deleteButton, Theme.RED, Theme.BASE);
        buttonPanel.add(refreshButton);
        buttonPanel.add(toggleButton);
        buttonPanel.add(deleteButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Theme.MANTLE);
        clockLabel.setForeground(Theme.SUBTEXT);
        bottomPanel.add(clockLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        setLayout(new BorderLayout(8, 8));
        add(topPanel, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.SURFACE2));
        scrollPane.getViewport().setBackground(Theme.SURFACE0);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> handleAdd());
        refreshButton.addActionListener(e -> loadTasksAsync());
        toggleButton.addActionListener(e -> handleToggle());
        deleteButton.addActionListener(e -> handleDelete());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                executor.shutdownNow();
                clockExecutor.shutdownNow();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                executor.shutdownNow();
                clockExecutor.shutdownNow();
            }
        });
    }

    private void styleButton(JButton button, java.awt.Color background, java.awt.Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    }

    private void handleAdd() {
        String description = inputField.getText().trim();
        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi deskripsi terlebih dahulu", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        inputField.setText("");
        runInBackground(() -> {
            taskService.createTask(description);
            refreshList();
        });
    }

    private void handleToggle() {
        Task selected = taskList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Pilih task yang mau diubah.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        runInBackground(() -> {
            taskService.setCompleted(selected.id(), !selected.completed());
            refreshList();
        });
    }

    private void handleDelete() {
        Task selected = taskList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Pilih task yang mau dihapus.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Hapus task \"" + selected.description() + "\"?",
                "Konfirmasi",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            runInBackground(() -> {
                taskService.deleteTask(selected.id());
                refreshList();
            });
        }
    }

    private void loadTasksAsync() {
        runInBackground(this::refreshList);
    }

    private void refreshList() throws SQLException {
        List<Task> tasks = taskService.getAllTasks();
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            tasks.forEach(listModel::addElement);
        });
    }

    private void startClock() {
        clockExecutor.scheduleAtFixedRate(() -> {
            String text = "Waktu sekarang: " + LocalDateTime.now().format(clockFormatter);
            SwingUtilities.invokeLater(() -> clockLabel.setText(text));
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void runInBackground(BackgroundTask task) {
        setControlsEnabled(false);
        executor.submit(() -> {
            try {
                task.run();
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE));
            } finally {
                SwingUtilities.invokeLater(() -> setControlsEnabled(true));
            }
        });
    }

    private void setControlsEnabled(boolean enabled) {
        addButton.setEnabled(enabled);
        refreshButton.setEnabled(enabled);
        toggleButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
    }

    @FunctionalInterface
    private interface BackgroundTask {
        void run() throws SQLException;
    }
}
