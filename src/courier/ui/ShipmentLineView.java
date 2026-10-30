package courier.ui;

import courier.shipment.Shipment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ShipmentLineView extends JPanel {
    private final JLabel countLabel;
    private final JPanel chips;

    public ShipmentLineView(String title) {
        super(new BorderLayout(0, 4));
        setBackground(Palette.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                Palette.lineBorder(), Palette.padding(6, 8, 6, 8)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Palette.SUBTITLE);
        titleLabel.setForeground(Palette.TEXT);
        countLabel = new JLabel();
        countLabel.setFont(Palette.SMALL);
        countLabel.setForeground(Palette.MUTED);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titleLabel, BorderLayout.WEST);
        header.add(countLabel, BorderLayout.EAST);

        JLabel front = new JLabel("FRENTE \u2192");
        front.setFont(Palette.MONO_BOLD);
        front.setForeground(Palette.MUTED);
        JLabel end = new JLabel("\u2190 FINAL");
        end.setFont(Palette.MONO_BOLD);
        end.setForeground(Palette.MUTED);

        chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        chips.setOpaque(false);

        JPanel body = new JPanel(new BorderLayout(8, 0));
        body.setOpaque(false);
        body.add(front, BorderLayout.WEST);
        body.add(chips, BorderLayout.CENTER);
        body.add(end, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    public void update(List<Shipment> shipments) {
        int rows = Math.max(1, (shipments.size() + 7) / 8);
        setPreferredSize(new Dimension(240, 38 + rows * 64));
        countLabel.setText(shipments.size() + " envio(s)");
        chips.removeAll();
        if (shipments.isEmpty()) {
            JLabel empty = new JLabel("vacia");
            empty.setFont(Palette.BODY);
            empty.setForeground(Palette.MUTED);
            chips.add(empty);
        } else {
            for (int index = 0; index < shipments.size(); index++) {
                chips.add(chip(shipments.get(index), index == 0));
            }
        }
        chips.revalidate();
        chips.repaint();
    }

    private JPanel chip(Shipment shipment, boolean front) {
        JPanel chip = new JPanel();
        chip.setLayout(new BoxLayout(chip, BoxLayout.Y_AXIS));
        chip.setBackground(front ? Palette.PANEL : Palette.CHIP);
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(front ? Palette.TEXT : Palette.BORDER, front ? 2 : 1),
                Palette.padding(3, 6, 3, 6)));

        JLabel guide = new JLabel(shipment.getGuide());
        guide.setFont(Palette.MONO_BOLD);
        guide.setForeground(Palette.TEXT);
        JLabel destination = new JLabel(shipment.getDestination());
        destination.setFont(Palette.SMALL);
        destination.setForeground(Palette.MUTED);
        JLabel type = new JLabel(shipment.getType() + " \u00b7 min " + shipment.getReceivedMinute());
        type.setFont(Palette.SMALL);
        type.setForeground(Color.DARK_GRAY);

        chip.add(guide);
        chip.add(destination);
        chip.add(type);
        chip.setPreferredSize(new Dimension(Math.max(92, chip.getPreferredSize().width), 58));
        return chip;
    }
}
