package courier.service;

import courier.shipment.ShipmentType;

import java.util.List;

// Datos de ejemplo solicitados por RFE-10.
public final class SeedData {
    public record ShipmentSeed(String guide, String destination, ShipmentType type, int receivedMinute) {
    }

    private SeedData() {
    }

    public static List<ShipmentSeed> shipments() {
        return List.of(
                new ShipmentSeed("G-1045", "Centro", ShipmentType.SOBRE, 480),
                new ShipmentSeed("G-1046", "Chapinero", ShipmentType.PAQUETE, 485),
                new ShipmentSeed("G-1047", "La Candelaria", ShipmentType.SOBRE, 490),
                new ShipmentSeed("G-1048", "Teusaquillo", ShipmentType.PAQUETE, 495),
                new ShipmentSeed("G-1049", "Kennedy", ShipmentType.SOBRE, 500),
                new ShipmentSeed("G-1050", "Suba", ShipmentType.PAQUETE, 505));
    }
}
