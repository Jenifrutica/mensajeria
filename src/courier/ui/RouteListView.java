package courier.ui;

import courier.route.Stop;
import courier.route.StopStatus;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class RouteListView extends JPanel {
    public RouteListView() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Palette.WHITE);
        setBorder(Palette.padding(4, 4, 4, 4));
    }

    public void update(List<Stop> stops, Stop current) {
        removeAll();
        if (stops.isEmpty()) {
            JLabel empty = new JLabel("La ruta esta vacia.");
            empty.setFont(Palette.BODY);
            empty.setForeground(Palette.MUTED);
            empty.setBorder(Palette.padding(8, 8, 8, 8));
            add(empty);
        } else {
            for (int index = 0; index < stops.size(); index++) {
                Stop stop = stops.get(index);
                add(row(index + 1, stop, stop == current));
                if (index < stops.size() - 1) {
                    JLabel arrow = new JLabel("\u2193", SwingConstants.CENTER);
                    arrow.setFont(Palette.MONO_BOLD);
                    arrow.setForeground(Palette.MUTED);
                    arrow.setAlignmentX(CENTER_ALIGNMENT);
                    add(arrow);
                }
            }
        }
        revalidate();
        repaint();
    }

    private JPanel row(int position, Stop stop, boolean current) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(current ? Palette.PANEL : Palette.CHIP);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(current ? Palette.TEXT : Palette.BORDER, current ? 2 : 1),
                Palette.padding(5, 7, 5, 7)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JLabel badge = new JLabel(String.valueOf(position), SwingConstants.CENTER);
        badge.setFont(Palette.MONO_BOLD);
        badge.setForeground(Palette.TEXT);
        badge.setPreferredSize(new Dimension(28, 28));
        badge.setBorder(Palette.lineBorder());
        badge.setOpaque(true);
        badge.setBackground(Palette.WHITE);

        JPanel text = new JPanel(new GridLayout(2, 1));
        text.setOpaque(false);
        JLabel guide = new JLabel(stop.getShipment().getGuide() + "  \u00b7  " + stop.getShipment().getDestination());
        guide.setFont(Palette.MONO_BOLD);
        guide.setForeground(Palette.TEXT);
        JLabel detail = new JLabel(stop.getShipment().getType() + " \u00b7 intentos: "
                + stop.getShipment().getAttempts());
        detail.setFont(Palette.SMALL);
        detail.setForeground(Palette.MUTED);
        text.add(guide);
        text.add(detail);

        JLabel status = new JLabel(stop.getStatus() + (current ? "  \u00b7  ACTUAL" : ""));
        status.setFont(Palette.MONO_BOLD);
        status.setForeground(colorFor(stop.getStatus()));
        status.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(badge, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(status, BorderLayout.EAST);
        return row;
    }

    private java.awt.Color colorFor(StopStatus status) {
        return switch (status) {
            case ENTREGADA -> Palette.GREEN;
            case FALLIDA -> Palette.RED;
            case PENDIENTE -> Palette.TEXT;
        };
    }
}
