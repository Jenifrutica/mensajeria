package courier.trunk;

import courier.shipment.Shipment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Trunk {
    private static final class TrunkPackage {
        private final Shipment shipment;
        private TrunkPackage below;

        private TrunkPackage(Shipment shipment) {
            this.shipment = shipment;
        }
    }

    private TrunkPackage top;
    private int size;

    public void push(Shipment shipment) {
        TrunkPackage item = new TrunkPackage(shipment);
        item.below = top;
        top = item;
        size++;
    }

    public Shipment pop() {
        if (top == null) {
            return null;
        }
        Shipment shipment = top.shipment;
        top = top.below;
        size--;
        return shipment;
    }

    public Shipment peek() {
        return top == null ? null : top.shipment;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsGuide(String guide) {
        for (TrunkPackage item = top; item != null; item = item.below) {
            if (item.shipment.getGuide().equalsIgnoreCase(guide)) {
                return true;
            }
        }
        return false;
    }

    public List<Shipment> snapshotTopToBottom() {
        List<Shipment> shipments = new ArrayList<>(size);
        for (TrunkPackage item = top; item != null; item = item.below) {
            shipments.add(item.shipment);
        }
        return Collections.unmodifiableList(shipments);
    }

    public int moveTopToBottom() {
        if (top == null || top.below == null) {
            return 0;
        }
        Shipment failed = pop();
        int moves = 1;
        Trunk auxiliary = new Trunk();
        while (!isEmpty()) {
            auxiliary.push(pop());
            moves += 2;
        }
        push(failed);
        moves++;
        while (!auxiliary.isEmpty()) {
            push(auxiliary.pop());
            moves += 2;
        }
        return moves;
    }

    public List<Shipment> drain() {
        List<Shipment> shipments = new ArrayList<>(size);
        while (!isEmpty()) {
            shipments.add(pop());
        }
        return shipments;
    }
}
