package courier.ui;

import courier.service.DispatchService;
import courier.service.ValidationResult;
import courier.shipment.Shipment;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class TrunkScreen extends JPanel implements Refreshable {
    private final DispatchService service;
    private final ScreenHost host;
    private final TrunkColumnView trunkView;
    private final RouteListView routeView;
    private final JTextArea validationLabel;
    private final JButton departureButton;

    public TrunkScreen(DispatchService service, ScreenHost host) {
        super(new BorderLayout(0, 8));
        this.service = service;
        this.host = host;
        setBackground(Palette.PAPER);
        setBorder(Palette.padding(12, 16, 12, 16));

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        JLabel title = new JLabel("3. Maletero y validacion");
        title.setFont(Palette.TITLE);
        title.setForeground(Palette.TEXT);
        JLabel help = new JLabel("El maletero se dibuja con el tope arriba y la ruta con la primera parada arriba: deben ser inversos.");
        help.setFont(Palette.BODY);
        help.setForeground(Palette.MUTED);
        header.add(title);
        header.add(help);

        trunkView = new TrunkColumnView();
        JPanel trunkCard = new JPanel(new BorderLayout(0, 4));
        trunkCard.setOpaque(false);
        JLabel trunkTitle = new JLabel("Pila del maletero (tope arriba)");
        trunkTitle.setFont(Palette.SUBTITLE);
        trunkTitle.setForeground(Palette.TEXT);
        trunkCard.add(trunkTitle, BorderLayout.NORTH);
        trunkCard.add(trunkView, BorderLayout.CENTER);
        trunkCard.setPreferredSize(new Dimension(330, 520));

        routeView = new RouteListView();
        JScrollPane routeScroll = new JScrollPane(routeView);
        routeScroll.setBorder(Palette.lineBorder());
        routeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        routeScroll.getViewport().setBackground(Palette.WHITE);
        JPanel routeCard = new JPanel(new BorderLayout(0, 4));
        routeCard.setOpaque(false);
        JLabel routeTitle = new JLabel("Ruta (primera parada arriba)");
        routeTitle.setFont(Palette.SUBTITLE);
        routeTitle.setForeground(Palette.TEXT);
        routeCard.add(routeTitle, BorderLayout.NORTH);
        routeCard.add(routeScroll, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, trunkCard, routeCard);
        split.setDividerLocation(350);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setOpaque(false);

        validationLabel = new JTextArea(2, 20);
        validationLabel.setEditable(false);
        validationLabel.setLineWrap(true);
        validationLabel.setWrapStyleWord(true);
        validationLabel.setFont(Palette.SUBTITLE);
        validationLabel.setOpaque(false);
        validationLabel.setBorder(Palette.padding(2, 4, 2, 4));

        JButton load = Palette.button("Cargar maletero");
        load.addActionListener(event -> host.run(service::loadTrunk));
        JButton unload = Palette.button("Descargar maletero");
        unload.addActionListener(event -> host.run(service::unloadTrunk));
        JButton validate = Palette.button("Validar carga");
        validate.addActionListener(event -> host.run(service::validateLoad));
        JButton deliver = Palette.button("Entregar");
        deliver.addActionListener(event -> host.run(service::deliver));
        JButton fail = Palette.button("Registrar entrega fallida");
        fail.addActionListener(event -> host.run(service::failDelivery));
        departureButton = Palette.button("Salir (autorizar salida)");
        departureButton.addActionListener(event -> host.run(service::authorizeDeparture));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.setOpaque(false);
        buttons.add(load);
        buttons.add(unload);
        buttons.add(validate);
        buttons.add(deliver);
        buttons.add(fail);
        buttons.add(departureButton);

        JPanel south = new JPanel(new BorderLayout(0, 2));
        south.setOpaque(false);
        south.add(validationLabel, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    @Override
    public void refresh() {
        List<Shipment> stack = service.getTrunk().snapshotTopToBottom();
        ValidationResult validation = service.inspectLoad();
        boolean emptyIdle = stack.isEmpty() && !service.isLoadInvalid();
        boolean invalid = service.isLoadInvalid() || (!stack.isEmpty() && !validation.valid());
        trunkView.update(stack, invalid);
        routeView.update(service.getRoute().forwardView(), service.getRoute().getCurrent());

        if (emptyIdle) {
            validationLabel.setForeground(Palette.MUTED);
            validationLabel.setText("MALETERO SIN CARGAR. Cargue el maletero para habilitar la salida.");
        } else if (validation.valid()) {
            validationLabel.setForeground(Palette.GREEN);
            validationLabel.setText("CARGA VALIDA (R4). " + validation.message());
        } else {
            validationLabel.setForeground(Palette.RED);
            validationLabel.setText("CARGA INVALIDA. " + validation.message());
        }
        departureButton.setEnabled(validation.valid());
    }
}
