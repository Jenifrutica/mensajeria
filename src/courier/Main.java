package courier;

import courier.service.AppConfig;
import courier.service.DispatchService;
import courier.ui.MainFrame;
import courier.ui.Palette;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        UIManager.put("Panel.background", Palette.PAPER);
        UIManager.put("Button.background", Palette.PANEL);
        UIManager.put("Button.foreground", Palette.TEXT);
        UIManager.put("Button.select", Palette.BORDER);
        UIManager.put("Button.focus", Palette.BORDER);
        UIManager.put("Label.foreground", Palette.TEXT);
        UIManager.put("ComboBox.background", Palette.WHITE);
        UIManager.put("ComboBox.foreground", Palette.TEXT);
        UIManager.put("ComboBox.selectionBackground", Palette.PANEL);
        UIManager.put("ComboBox.selectionForeground", Palette.TEXT);
        UIManager.put("TextField.background", Palette.WHITE);
        UIManager.put("TextField.foreground", Palette.TEXT);
        UIManager.put("TextField.selectionBackground", Palette.PANEL);
        UIManager.put("TextField.selectionForeground", Palette.TEXT);
        UIManager.put("TextArea.background", Palette.WHITE);
        UIManager.put("TextArea.foreground", Palette.TEXT);
        UIManager.put("TextArea.selectionBackground", Palette.PANEL);
        UIManager.put("TextArea.selectionForeground", Palette.TEXT);
        UIManager.put("Spinner.background", Palette.WHITE);
        UIManager.put("Viewport.background", Palette.WHITE);
        UIManager.put("ScrollPane.background", Palette.WHITE);
        UIManager.put("SplitPane.background", Palette.PAPER);
        UIManager.put("SplitPane.dividerSize", 6);
        UIManager.put("OptionPane.background", Palette.WHITE);
        UIManager.put("OptionPane.messageForeground", Palette.TEXT);

        SwingUtilities.invokeLater(() -> {
            AppConfig config = AppConfig.load();
            DispatchService service = new DispatchService(config);
            MainFrame frame = new MainFrame(service);
            frame.setVisible(true);
        });
    }
}
