package org.example.ui;

import org.example.model.Task;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;
import java.awt.Font;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TaskListCellRenderer extends DefaultListCellRenderer {
    private final Font completedFont;
    private final Font normalFont;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
    private final ZoneId zoneId = ZoneId.systemDefault();

    public TaskListCellRenderer() {
        Font baseFont = getFont();
        if (baseFont == null) {
            baseFont = new Font("SansSerif", Font.PLAIN, 12);
        }
        normalFont = baseFont;
        completedFont = baseFont.deriveFont(Font.ITALIC);
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (component instanceof javax.swing.JLabel label && value instanceof Task task) {
            String status = task.completed() ? "x" : " ";
            label.setText(String.format("[%s] %s (dibuat %s)", status, task.description(), formatInstant(task.createdAt())));
            label.setFont(task.completed() ? completedFont : normalFont);
            label.setForeground(Theme.TEXT);
            if (isSelected) {
                label.setBackground(Theme.LAVENDER);
                label.setForeground(Theme.BASE);
            } else {
                label.setBackground(Theme.SURFACE0);
            }
            label.setOpaque(true);
        }
        return component;
    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "-";
        }
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, zoneId);
        return ldt.format(formatter);
    }
}
