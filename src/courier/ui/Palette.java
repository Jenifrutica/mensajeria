package courier.ui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;

public final class Palette {
    public static final Color WHITE = new Color(0xFFFFFF);
    public static final Color PAPER = new Color(0xF4F4F4);
    public static final Color PANEL = new Color(0xE8E8E8);
    public static final Color BORDER = new Color(0xBDBDBD);
    public static final Color TEXT = new Color(0x111111);
    public static final Color MUTED = new Color(0x5E5E5E);
    public static final Color RED = new Color(0xB00020);
    public static final Color GREEN = new Color(0x1B7F3B);
    public static final Color CHIP = new Color(0xFAFAFA);

    public static final Font TITLE = new Font("SansSerif", Font.BOLD, 22);
    public static final Font SUBTITLE = new Font("SansSerif", Font.BOLD, 15);
    public static final Font BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font SMALL = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font MONO = new Font("Monospaced", Font.PLAIN, 12);
    public static final Font MONO_BOLD = new Font("Monospaced", Font.BOLD, 12);

    private Palette() {
    }

    public static Border lineBorder() {
        return BorderFactory.createLineBorder(BORDER);
    }

    public static Border thickBorder(Color color) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(4, 6, 4, 6));
    }

    public static Border padding(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    public static JButton button(String text) {
        JButton button = new JButton(text);
        button.setBackground(PANEL);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setFont(BODY);
        return button;
    }
}
