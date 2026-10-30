package courier.ui;

import courier.shipment.Shipment;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class TrunkColumnView extends JPanel {
    private final JPanel content;

    public TrunkColumnView() {
        super(new BorderLayout());
        setBackground(Palette.WHITE);
        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(Palette.padding(6, 6, 6, 6));
        add(content, BorderLayout.NORTH);
    }

    public void update(List<Shipment> topToBottom, boolean invalid) {
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(invalid ? Palette.RED : Palette.BORDER, invalid ? 3 : 1),
                Palette.padding(6, 6, 6, 6)));
        content.removeAll();

        JLabel top = new JLabel("\u25b2 TOPE", SwingConstants.CENTER);
        top.setFont(Palette.MONO_BOLD);
        top.setForeground(invalid ? Palette.RED : Palette.TEXT);
        top.setAlignmentX(CENTER_ALIGNMENT);
        content.add(top);

        if (topToBottom.isEmpty()) {
            JLabel empty = new JLabel("Maletero vacio");
            empty.setFont(Palette.BODY);
            empty.setForeground(Palette.MUTED);
            empty.setAlignmentX(CENTER_ALIGNMENT);
            empty.setBorder(Palette.padding(20, 4, 20, 4));
            content.add(empty);
        } else {
            for (int index = 0; index < topToBottom.size(); index++) {
                Shipment shipment = topToBottom.get(index);
                JPanel item = new JPanel(new BorderLayout());
                item.setBackground(index == 0 ? Palette.PANEL : Palette.CHIP);
                item.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(index == 0 ? Palette.TEXT : Palette.BORDER, index == 0 ? 2 : 1),
                        Palette.padding(5, 8, 5, 8)));
                item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

                JLabel guide = new JLabel((index == 0 ? "[TOPE] " : "") + shipment.getGuide());
                guide.setFont(Palette.MONO_BOLD);
                guide.setForeground(Palette.TEXT);
                JLabel destination = new JLabel(shipment.getDestination() + " \u00b7 " + shipment.getType());
                destination.setFont(Palette.SMALL);
                destination.setForeground(Palette.MUTED);
                item.add(guide, BorderLayout.NORTH);
                item.add(destination, BorderLayout.SOUTH);
                content.add(item);
                if (index < topToBottom.size() - 1) {
                    JLabel gap = new JLabel(" ");
                    gap.setFont(Palette.SMALL);
                    gap.setAlignmentX(CENTER_ALIGNMENT);
                    content.add(gap);
                }
            }
        }

        JLabel bottom = new JLabel("FONDO", SwingConstants.CENTER);
        bottom.setFont(Palette.MONO_BOLD);
        bottom.setForeground(Palette.MUTED);
        bottom.setAlignmentX(CENTER_ALIGNMENT);
        bottom.setBorder(Palette.padding(6, 0, 0, 0));
        content.add(bottom);

        content.revalidate();
        content.repaint();
    }
}
