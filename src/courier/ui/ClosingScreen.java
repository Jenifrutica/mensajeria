package courier.ui;

import courier.service.DispatchService;
import courier.service.LogEntry;
import courier.service.MetricsSnapshot;
import courier.service.OperationResult;
import courier.service.WaitRecord;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class ClosingScreen extends JPanel implements Refreshable {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final DispatchService service;
    private final ScreenHost host;
    private final JTextArea metricsArea;
    private final JTextArea logArea;
    private final JLabel closeResult;
    private final JScrollPane logScroll;

    public ClosingScreen(DispatchService service, ScreenHost host) {
        super(new BorderLayout(0, 8));
        this.service = service;
        this.host = host;
        setBackground(Palette.PAPER);
        setBorder(Palette.padding(12, 16, 12, 16));

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        JLabel title = new JLabel("4. Cierre y reportes");
        title.setFont(Palette.TITLE);
        title.setForeground(Palette.TEXT);
        JLabel help = new JLabel("Cierre la ruta para devolver lo no entregado y generar las metricas y la bitacora.");
        help.setFont(Palette.BODY);
        help.setForeground(Palette.MUTED);
        header.add(title);
        header.add(help);

        metricsArea = textArea();
        logArea = textArea();
        JScrollPane metricsScroll = new JScrollPane(metricsArea);
        metricsScroll.setBorder(Palette.lineBorder());
        logScroll = new JScrollPane(logArea);
        logScroll.setBorder(Palette.lineBorder());

        JPanel metricsCard = card("Panel de metricas", metricsScroll);
        JPanel logCard = card("Bitacora cronologica", logScroll);
        JPanel center = new JPanel(new GridLayout(1, 2, 10, 0));
        center.setOpaque(false);
        center.add(metricsCard);
        center.add(logCard);

        JButton report = Palette.button("Actualizar reporte");
        report.addActionListener(event -> host.run(service::generateReport));
        JButton close = Palette.button("Cerrar ruta");
        closeResult = new JLabel("Sin cierre ejecutado en esta sesion.");
        closeResult.setFont(Palette.BODY);
        closeResult.setForeground(Palette.MUTED);
        close.addActionListener(event -> {
            OperationResult result = host.run(service::closeRoute);
            closeResult.setText(result.message());
            closeResult.setForeground(result.success() ? Palette.GREEN : Palette.RED);
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actions.setOpaque(false);
        actions.add(report);
        actions.add(close);
        actions.add(closeResult);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private JTextArea textArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(Palette.MONO);
        area.setBackground(Palette.WHITE);
        area.setForeground(Palette.TEXT);
        area.setBorder(Palette.padding(6, 6, 6, 6));
        return area;
    }

    private JPanel card(String title, JScrollPane scroll) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(Palette.SUBTITLE);
        label.setForeground(Palette.TEXT);
        card.add(label, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    @Override
    public void refresh() {
        MetricsSnapshot metrics = service.getMetrics();
        StringBuilder report = new StringBuilder();
        report.append("Envios recibidos: ").append(metrics.received()).append('\n');
        report.append("Asignados a ruta: ").append(metrics.assigned()).append('\n');
        report.append("Entregados: ").append(metrics.delivered()).append('\n');
        report.append("Devueltos a pendientes al cerrar: ").append(metrics.returnedToPending()).append('\n');
        report.append("Entregas fallidas: ").append(metrics.failedDeliveries()).append('\n');
        report.append("Movimientos de reorganizacion: ").append(metrics.reorganizationMoves()).append('\n');
        report.append("Cargas invalidadas por R5: ").append(metrics.loadInvalidations()).append('\n');
        report.append("Cargas/recargas realizadas: ").append(metrics.loads()).append('\n');
        report.append("Espera promedio en pendientes: ")
                .append(String.format("%.1f", metrics.averageWait())).append(" min\n");
        report.append("Espera maxima en pendientes: ").append(metrics.maximumWait()).append(" min\n");
        report.append("Enviados a reintentos (2 intentos): ").append(metrics.retryTransfers()).append('\n');
        report.append('\n').append("Espera por envio:\n");
        if (metrics.waits().isEmpty()) {
            report.append("  (sin envios asignados todavia)\n");
        } else {
            for (WaitRecord wait : metrics.waits()) {
                report.append("  ").append(wait.guide()).append(": ")
                        .append(wait.minutes()).append(" min\n");
            }
        }
        metricsArea.setText(report.toString());
        metricsArea.setCaretPosition(0);

        StringBuilder bitacora = new StringBuilder();
        for (LogEntry entry : service.getLog()) {
            bitacora.append(entry.time().format(TIME_FORMAT))
                    .append(entry.success() ? " [OK]    " : " [ERROR] ")
                    .append(entry.message())
                    .append('\n');
        }
        if (bitacora.length() == 0) {
            bitacora.append("(sin operaciones registradas)\n");
        }
        logArea.setText(bitacora.toString());
        SwingUtilities.invokeLater(() -> logScroll.getVerticalScrollBar().setValue(
                logScroll.getVerticalScrollBar().getMaximum()));
    }
}
