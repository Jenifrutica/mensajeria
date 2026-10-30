package courier.ui;

import courier.route.Stop;
import courier.service.DispatchService;
import courier.service.OperationResult;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

public class RouteEditorPanel extends JPanel implements Refreshable {
    private final DispatchService service;
    private final ScreenHost host;
    private final RouteListView routeView;
    private final JLabel currentLabel;
    private final DefaultComboBoxModel<String> guideModel;
    private final JComboBox<String> guideBox;
    private final JSpinner movePosition;
    private final JSpinner insertPosition;
    private final JTextArea traversalArea;

    public RouteEditorPanel(DispatchService service, ScreenHost host) {
        super(new BorderLayout(12, 8));
        this.service = service;
        this.host = host;
        setBackground(Palette.PAPER);
        setBorder(Palette.padding(12, 16, 12, 16));

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        JLabel title = new JLabel("2. Editor de ruta");
        title.setFont(Palette.TITLE);
        title.setForeground(Palette.TEXT);
        currentLabel = new JLabel();
        currentLabel.setFont(Palette.BODY);
        currentLabel.setForeground(Palette.MUTED);
        header.add(title);
        header.add(currentLabel);

        routeView = new RouteListView();
        JScrollPane routeScroll = new JScrollPane(routeView);
        routeScroll.setBorder(Palette.lineBorder());
        routeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        routeScroll.getViewport().setBackground(Palette.WHITE);

        JPanel routeCard = new JPanel(new BorderLayout(0, 4));
        routeCard.setOpaque(false);
        JLabel routeTitle = new JLabel("Lista doblemente enlazada \u00b7 primera parada arriba");
        routeTitle.setFont(Palette.SUBTITLE);
        routeTitle.setForeground(Palette.TEXT);
        routeCard.add(routeTitle, BorderLayout.NORTH);
        routeCard.add(routeScroll, BorderLayout.CENTER);

        guideModel = new DefaultComboBoxModel<>();
        guideBox = new JComboBox<>(guideModel);
        guideBox.setPreferredSize(new Dimension(120, 26));
        movePosition = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
        insertPosition = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));

        JButton move = Palette.button("Mover");
        move.addActionListener(event -> moveSelected());
        JButton remove = Palette.button("Eliminar parada");
        remove.addActionListener(event -> removeSelected());
        JButton insert = Palette.button("Insertar siguiente pendiente");
        insert.addActionListener(event -> insertPending());

        JButton forward = Palette.button("Recorrer hacia adelante");
        forward.addActionListener(event -> traverse(true));
        JButton backward = Palette.button("Recorrer hacia atras");
        backward.addActionListener(event -> traverse(false));

        traversalArea = new JTextArea(11, 46);
        traversalArea.setEditable(false);
        traversalArea.setFont(Palette.MONO);
        traversalArea.setBackground(Palette.WHITE);
        traversalArea.setForeground(Palette.TEXT);
        traversalArea.setBorder(Palette.padding(6, 6, 6, 6));
        JScrollPane traversalScroll = new JScrollPane(traversalArea);
        traversalScroll.setBorder(Palette.lineBorder());

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setOpaque(false);
        controls.add(actionRow("Guia", guideBox, "a posicion", movePosition, move));
        controls.add(actionRow("Eliminar la guia seleccionada", null, "", null, remove));
        controls.add(actionRow("Insertar frente de pendientes en posicion", null, "", insertPosition, insert));
        controls.add(Box.createVerticalStrut(4));
        controls.add(actionRow("", forward, "", null, backward));
        JLabel traversalTitle = new JLabel("Salida del recorrido");
        traversalTitle.setFont(Palette.SUBTITLE);
        traversalTitle.setForeground(Palette.TEXT);
        traversalTitle.setAlignmentX(LEFT_ALIGNMENT);
        controls.add(traversalTitle);
        traversalScroll.setAlignmentX(LEFT_ALIGNMENT);
        controls.add(traversalScroll);

        add(header, BorderLayout.NORTH);
        add(routeCard, BorderLayout.CENTER);
        add(controls, BorderLayout.EAST);
    }

    private JPanel actionRow(String firstText, java.awt.Component first, String secondText,
            JSpinner spinner, JButton button) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        if (firstText != null && !firstText.isEmpty()) {
            row.add(label(firstText));
        }
        if (first != null) {
            row.add(first);
        }
        if (secondText != null && !secondText.isEmpty()) {
            row.add(label(secondText));
        }
        if (spinner != null) {
            row.add(spinner);
        }
        if (button != null) {
            row.add(button);
        }
        return row;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Palette.BODY);
        label.setForeground(Palette.TEXT);
        return label;
    }

    private void moveSelected() {
        String guide = (String) guideBox.getSelectedItem();
        if (guide == null) {
            host.run(() -> new OperationResult(false, "R2: seleccione una parada para mover."));
            return;
        }
        int position = (Integer) movePosition.getValue();
        host.run(() -> service.moveStop(guide, position));
    }

    private void removeSelected() {
        String guide = (String) guideBox.getSelectedItem();
        if (guide == null) {
            host.run(() -> new OperationResult(false, "R2: seleccione una parada para eliminar."));
            return;
        }
        host.run(() -> service.removeStop(guide));
    }

    private void insertPending() {
        int position = (Integer) insertPosition.getValue();
        host.run(() -> service.insertStop(position));
    }

    private void traverse(boolean forward) {
        List<String> lines = forward ? service.forwardTraversal() : service.backwardTraversal();
        traversalArea.setText(String.join("\n", lines));
        traversalArea.setCaretPosition(0);
        host.run(() -> service.registerTraversal(forward));
    }

    @Override
    public void refresh() {
        List<Stop> stops = service.getRoute().forwardView();
        Stop current = service.getRoute().getCurrent();
        routeView.update(stops, current);

        String selected = (String) guideBox.getSelectedItem();
        guideModel.removeAllElements();
        for (Stop stop : stops) {
            guideModel.addElement(stop.getShipment().getGuide());
        }
        if (selected != null && stops.stream()
                .anyMatch(stop -> stop.getShipment().getGuide().equals(selected))) {
            guideBox.setSelectedItem(selected);
        } else if (guideModel.getSize() > 0) {
            guideBox.setSelectedIndex(0);
        }

        int moveMax = Math.max(1, stops.size());
        movePosition.setModel(new SpinnerNumberModel(
                Math.min((Integer) movePosition.getValue(), moveMax), 1, moveMax, 1));
        int insertMax = Math.max(1, stops.size() + 1);
        insertPosition.setModel(new SpinnerNumberModel(
                Math.min((Integer) insertPosition.getValue(), insertMax), 1, insertMax, 1));

        if (current == null) {
            currentLabel.setText("Parada actual: ninguna. La ruta no tiene paradas pendientes.");
        } else {
            currentLabel.setText("Parada actual: " + current.getShipment().getGuide()
                    + " en la posicion " + service.getRoute().positionOf(current)
                    + " \u00b7 estado " + current.getStatus());
        }
    }
}
