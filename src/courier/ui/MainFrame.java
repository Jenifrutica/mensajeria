package courier.ui;

import courier.service.DispatchService;
import courier.service.OperationResult;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MainFrame extends JFrame implements ScreenHost {
    private final DispatchService service;
    private final CardLayout cards;
    private final JPanel cardPanel;
    private final List<Refreshable> refreshables;
    private final List<JButton> navigationButtons;
    private final JLabel statusLabel;

    public MainFrame(DispatchService service) {
        super("Despacho de mensajeria urbana en moto");
        this.service = service;
        this.cards = new CardLayout();
        this.cardPanel = new JPanel(cards);
        this.refreshables = new ArrayList<>();
        this.navigationButtons = new ArrayList<>();
        this.statusLabel = new JLabel("Sistema listo. Comience recibiendo envios o cargando los datos de ejemplo.");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1366, 768);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Palette.PAPER);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCards(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
        setContentPane(root);

        showCard(0);
        refreshAll();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(Palette.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Palette.BORDER),
                Palette.padding(8, 16, 8, 16)));

        JLabel brand = new JLabel("Despacho urbano en moto");
        brand.setFont(Palette.TITLE);
        brand.setForeground(Palette.TEXT);
        brand.setBorder(Palette.padding(0, 0, 0, 16));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        nav.setOpaque(false);
        String[] labels = {"1. Bandeja de envios", "2. Editor de ruta", "3. Maletero", "4. Cierre y reportes"};
        for (int index = 0; index < labels.length; index++) {
            JButton button = Palette.button(labels[index]);
            int target = index;
            button.addActionListener((ActionEvent event) -> showCard(target));
            navigationButtons.add(button);
            nav.add(button);
        }

        header.add(brand, BorderLayout.WEST);
        header.add(nav, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildCards() {
        cardPanel.setBackground(Palette.PAPER);
        ShipmentInboxPanel inbox = new ShipmentInboxPanel(service, this);
        RouteEditorPanel route = new RouteEditorPanel(service, this);
        TrunkScreen trunk = new TrunkScreen(service, this);
        ClosingScreen closing = new ClosingScreen(service, this);

        addCard(inbox, "inbox");
        addCard(route, "route");
        addCard(trunk, "trunk");
        addCard(closing, "closing");
        return cardPanel;
    }

    private void addCard(JPanel panel, String name) {
        cardPanel.add(panel, name);
        refreshables.add((Refreshable) panel);
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new GridLayout(1, 1));
        bar.setBackground(Palette.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Palette.BORDER),
                Palette.padding(5, 16, 5, 16)));
        statusLabel.setFont(Palette.BODY);
        statusLabel.setForeground(Palette.MUTED);
        bar.add(statusLabel);
        return bar;
    }

    private void showCard(int index) {
        String[] names = {"inbox", "route", "trunk", "closing"};
        cards.show(cardPanel, names[index]);
        for (int buttonIndex = 0; buttonIndex < navigationButtons.size(); buttonIndex++) {
            navigationButtons.get(buttonIndex).setBackground(
                    buttonIndex == index ? Palette.PAPER : Palette.WHITE);
        }
        refreshAll();
    }

    @Override
    public OperationResult run(Supplier<OperationResult> operation) {
        OperationResult result;
        try {
            result = operation.get();
        } catch (RuntimeException exception) {
            result = new OperationResult(false, "Error inesperado: " + exception.getMessage());
        }
        statusLabel.setText(result.message());
        statusLabel.setForeground(result.success() ? Palette.TEXT : Palette.RED);
        refreshAll();
        return result;
    }

    @Override
    public void refreshAll() {
        for (Refreshable refreshable : refreshables) {
            refreshable.refresh();
        }
    }

    public DispatchService getService() {
        return service;
    }
}
