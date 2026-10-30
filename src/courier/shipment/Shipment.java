package courier.shipment;

import java.util.Objects;

public class Shipment {
    private final String guide;
    private final String destination;
    private final ShipmentType type;
    private final int receivedMinute;
    private int attempts;

    public Shipment(String guide, String destination, ShipmentType type, int receivedMinute) {
        this.guide = Objects.requireNonNull(guide, "guide");
        this.destination = Objects.requireNonNull(destination, "destination");
        this.type = Objects.requireNonNull(type, "type");
        this.receivedMinute = receivedMinute;
    }

    public String getGuide() {
        return guide;
    }

    public String getDestination() {
        return destination;
    }

    public ShipmentType getType() {
        return type;
    }

    public int getReceivedMinute() {
        return receivedMinute;
    }

    public int getAttempts() {
        return attempts;
    }

    public void registerAttempt() {
        attempts++;
    }

    @Override
    public String toString() {
        return guide + " (" + type + ") -> " + destination;
    }
}
