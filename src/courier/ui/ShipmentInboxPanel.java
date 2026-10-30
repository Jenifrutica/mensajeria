package courier.ui;

import courier.service.DispatchService;
import courier.service.OperationResult;
import courier.shipment.ShipmentType;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class ShipmentInboxPanel extends JPanel implements Refreshable {
    private final DispatchService service;
    private final ScreenHost host;
    private final ShipmentLineView pendingView;
    private final ShipmentLineView retryView;
    private final JTextField guideField;
    private final JTextField destinationField;
    private final JComboBox<ShipmentType> typeBox;
    private final JSpinner minuteSpinner;

    public ShipmentInboxPanel(DispatchService service, ScreenHost host) {
        super(new BorderLayout(0, 10));
        this.service = service;
        this.host = host;
        setBackground(Palette.PAPER);
        setBorder(Palette.padding(12, 16, 12, 16));

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        JLabel title = new JLabel("1. Bandeja de envios");
        title.setFont(Palette.TITLE);
        title.setForeground(Palette.TEXT);
        JLabel help = new JLabel("Los envios se atienden de izquierda a derecha: primero el FRENTE de cada cola.");
        help.setFont(Palette.BODY);
        help.setForeground(Palette.MUTED);
        header.add(title);
        header.add(help);

        pendingView = new ShipmentLineView("Cola de pendientes");
        retryView = new ShipmentLineView("Cola de reintentos (dia siguiente)");

        JPanel queues = new JPanel();
        queues.setLayout(new BoxLayout(queues, BoxLayout.Y_AXIS));
        queues.setOpaque(false);
        queues.add(pendingView);
        queues.add(javax.swing.Box.createVerticalStrut(8));
        queues.add(retryView);

        guideField = new JTextField(9);
        destinationField = new JTextField(18);
        typeBox = new JComboBox<>(ShipmentType.values());
        minuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1439, 1));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        form.setBackground(Palette.WHITE);
        form.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                Palette.lineBorder(), Palette.padding(4, 6, 4, 6)));
        form.add(label("Guia"));
        form.add(guideField);
        form.add(label("Destino"));
        form.add(destinationField);
        form.add(label("Tipo"));
        form.add(typeBox);
        form.add(label("Minuto"));
        form.add(minuteSpinner);

        JButton receive = Palette.button("Recibir envio");
        receive.addActionListener(event -> receiveShipment());
        JButton assemble = Palette.button("Armar ruta");
        assemble.addActionListener(event -> host.run(service::assembleRoute));
        JButton seed = Palette.button("Cargar datos de ejemplo");
        seed.addActionListener(event -> host.run(service::loadExampleData));
        form.add(receive);
        form.add(assemble);
        form.add(seed);

        JLabel capacity = new JLabel("Capacidad de la cola: " + service.getConfig().getPendingCapacity()
                + " envios \u00b7 Capacidad del maletero: " + service.getConfig().getTrunkCapacity() + " paradas");
        capacity.setFont(Palette.SMALL);
        capacity.setForeground(Palette.MUTED);

        JPanel south = new JPanel(new BorderLayout(0, 4));
        south.setOpaque(false);
        south.add(form, BorderLayout.NORTH);
        south.add(capacity, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(queues, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Palette.BODY);
        label.setForeground(Palette.TEXT);
        return label;
    }

    private void receiveShipment() {
        String guide = guideField.getText();
        String destination = destinationField.getText();
        ShipmentType type = (ShipmentType) typeBox.getSelectedItem();
        int minute = (Integer) minuteSpinner.getValue();
        OperationResult result = host.run(() -> service.receiveShipment(guide, destination, type, minute));
        if (result.success()) {
            guideField.setText("");
            destinationField.setText("");
        }
    }

    @Override
    public void refresh() {
        pendingView.update(service.getPending().snapshot());
        retryView.update(service.getRetries().snapshot());
    }
}
