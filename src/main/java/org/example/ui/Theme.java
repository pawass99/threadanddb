package org.example.ui;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.Color;
import java.awt.Font;
import java.util.Enumeration;

/**
 * Handles global Swing look customization (font, colors, AA hints).
 */
public final class Theme {
    public static final Color BASE = hex("1e1e2e");
    public static final Color MANTLE = hex("181825");
    public static final Color SURFACE0 = hex("313244");
    public static final Color SURFACE1 = hex("45475a");
    public static final Color SURFACE2 = hex("585b70");
    public static final Color TEXT = hex("cdd6f4");
    public static final Color SUBTEXT = hex("a6adc8");
    public static final Color GREEN = hex("a6e3a1");
    public static final Color PEACH = hex("fab387");
    public static final Color RED = hex("f38ba8");
    public static final Color LAVENDER = hex("b4befe");

    private Theme() {
    }

    public static void apply() {
        enableAntialiasing();
        installFont();
        installColors();
    }

    private static void enableAntialiasing() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }

    private static void installFont() {
        Font baseFont = new Font("Iosevka", Font.PLAIN, 14);
        if (!"Iosevka".equals(baseFont.getFamily())) {
            baseFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        }
        FontUIResource fontResource = new FontUIResource(baseFont);
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontResource);
            }
        }
    }

    private static void installColors() {
        UIManager.put("Panel.background", BASE);
        UIManager.put("OptionPane.background", BASE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Button.background", SURFACE1);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("TextField.background", SURFACE0);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("List.background", SURFACE0);
        UIManager.put("List.foreground", TEXT);
        UIManager.put("List.selectionBackground", LAVENDER);
        UIManager.put("List.selectionForeground", BASE);
    }

    private static Color hex(String value) {
        return new Color(
                Integer.valueOf(value.substring(0, 2), 16),
                Integer.valueOf(value.substring(2, 4), 16),
                Integer.valueOf(value.substring(4, 6), 16)
        );
    }
}
