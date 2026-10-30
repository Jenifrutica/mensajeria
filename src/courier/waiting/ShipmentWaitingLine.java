package courier.waiting;

import courier.shipment.Shipment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ShipmentWaitingLine {
    private static final class WaitingShipment {
        private final Shipment shipment;
        private final int sinceMinute;
        private WaitingShipment next;

        private WaitingShipment(Shipment shipment, int sinceMinute) {
            this.shipment = shipment;
            this.sinceMinute = sinceMinute;
        }
    }

    private WaitingShipment first;
    private WaitingShipment last;
    private int size;
    private final int capacity;

    protected ShipmentWaitingLine(int capacity) {
        this.capacity = capacity;
    }

    public void offerLast(Shipment shipment, int sinceMinute) {
        WaitingShipment entry = new WaitingShipment(shipment, sinceMinute);
        if (last == null) {
            first = entry;
            last = entry;
        } else {
            last.next = entry;
            last = entry;
        }
        size++;
    }

    public void offerFirst(Shipment shipment, int sinceMinute) {
        WaitingShipment entry = new WaitingShipment(shipment, sinceMinute);
        entry.next = first;
        first = entry;
        if (last == null) {
            last = entry;
        }
        size++;
    }

    public Shipment peek() {
        return first == null ? null : first.shipment;
    }

    public int peekSince() {
        return first == null ? 0 : first.sinceMinute;
    }

    public Shipment poll() {
        if (first == null) {
            return null;
        }
        Shipment shipment = first.shipment;
        first = first.next;
        if (first == null) {
            last = null;
        }
        size--;
        return shipment;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return capacity;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size >= capacity;
    }

    public boolean containsGuide(String guide) {
        for (WaitingShipment entry = first; entry != null; entry = entry.next) {
            if (entry.shipment.getGuide().equalsIgnoreCase(guide)) {
                return true;
            }
        }
        return false;
    }

    public List<Shipment> snapshot() {
        List<Shipment> shipments = new ArrayList<>(size);
        for (WaitingShipment entry = first; entry != null; entry = entry.next) {
            shipments.add(entry.shipment);
        }
        return Collections.unmodifiableList(shipments);
    }

    public void clear() {
        first = null;
        last = null;
        size = 0;
    }
}
