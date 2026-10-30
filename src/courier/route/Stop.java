package courier.route;

import courier.shipment.Shipment;

public class Stop {
    private final Shipment shipment;
    private StopStatus status;
    private Stop previous;
    private Stop next;

    Stop(Shipment shipment) {
        this.shipment = shipment;
        this.status = StopStatus.PENDIENTE;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public StopStatus getStatus() {
        return status;
    }

    public void setStatus(StopStatus status) {
        this.status = status;
    }

    Stop getPrevious() {
        return previous;
    }

    void setPrevious(Stop previous) {
        this.previous = previous;
    }

    Stop getNext() {
        return next;
    }

    void setNext(Stop next) {
        this.next = next;
    }
}
