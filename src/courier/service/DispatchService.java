package courier.service;

import courier.route.Route;
import courier.route.Stop;
import courier.route.StopStatus;
import courier.shipment.Shipment;
import courier.shipment.ShipmentType;
import courier.trunk.Trunk;
import courier.waiting.PendingShipments;
import courier.waiting.RetryShipments;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DispatchService {
    private final AppConfig config;
    private final PendingShipments pending;
    private final RetryShipments retries;
    private final Route route;
    private final Trunk trunk;
    private final Metrics metrics;
    private final List<LogEntry> log;

    private int currentMinute;
    private boolean loadInvalid;
    private String loadInvalidReason;

    public DispatchService(AppConfig config) {
        this.config = config;
        this.pending = new PendingShipments(config.getPendingCapacity());
        this.retries = new RetryShipments(config.getPendingCapacity());
        this.route = new Route();
        this.trunk = new Trunk();
        this.metrics = new Metrics();
        this.log = new ArrayList<>();
        this.loadInvalidReason = "";
    }

    public OperationResult receiveShipment(String rawGuide, String rawDestination, ShipmentType type, int minute) {
        String guide = rawGuide == null ? "" : rawGuide.trim();
        String destination = rawDestination == null ? "" : rawDestination.trim();
        if (guide.isEmpty()) {
            return result(false, "RF-01: la guia no puede estar vacia.");
        }
        if (destination.isEmpty()) {
            return result(false, "RF-01: el destino no puede estar vacio.");
        }
        if (type == null) {
            return result(false, "RF-01: debe indicar el tipo de envio.");
        }
        if (minute < 0) {
            return result(false, "RF-01: el minuto de recepcion no puede ser negativo.");
        }
        if (isGuideKnown(guide)) {
            return result(false, "RF-01/R9: la guia " + guide + " ya existe en el sistema.");
        }
        if (pending.isFull()) {
            return result(false, "RF-01/R1: la cola de pendientes alcanzo su capacidad de "
                    + pending.capacity() + " envios.");
        }
        Shipment shipment = new Shipment(guide, destination, type, minute);
        pending.offerLast(shipment, minute);
        currentMinute = Math.max(currentMinute, minute);
        metrics.shipmentReceived();
        return result(true, "R1: envio " + guide + " recibido y encolado al final de pendientes.");
    }

    public OperationResult loadExampleData() {
        int added = 0;
        int skipped = 0;
        for (SeedData.ShipmentSeed seed : SeedData.shipments()) {
            if (isGuideKnown(seed.guide())) {
                skipped++;
                continue;
            }
            if (pending.isFull()) {
                skipped++;
                continue;
            }
            Shipment shipment = new Shipment(seed.guide(), seed.destination(), seed.type(), seed.receivedMinute());
            pending.offerLast(shipment, seed.receivedMinute());
            currentMinute = Math.max(currentMinute, seed.receivedMinute());
            metrics.shipmentReceived();
            added++;
        }
        if (added == 0) {
            return result(false, "RFE-10: no se cargaron datos de ejemplo (ya existian o la cola esta llena).");
        }
        return result(true, "RFE-10: " + added + " envios de ejemplo cargados"
                + (skipped > 0 ? "; " + skipped + " omitidos." : "."));
    }

    public OperationResult assembleRoute() {
        if (!route.isEmpty()) {
            return result(false, "R2: la ruta ya tiene paradas; cierrela antes de armar otra.");
        }
        if (!trunk.isEmpty()) {
            return result(false, "R2/R3: hay paquetes en el maletero; descarguelo antes de armar otra ruta.");
        }
        if (pending.isEmpty()) {
            return result(false, "R2: no hay envios pendientes para armar la ruta.");
        }
        int assigned = 0;
        while (assigned < config.getTrunkCapacity() && !pending.isEmpty()) {
            int since = pending.peekSince();
            Shipment shipment = pending.poll();
            route.addLast(shipment);
            metrics.shipmentAssigned();
            metrics.recordWait(shipment.getGuide(), currentMinute - since);
            assigned++;
        }
        loadInvalid = false;
        loadInvalidReason = "";
        return result(true, "R2: ruta armada con " + assigned + " paradas, en el orden de llegada.");
    }

    public OperationResult moveStop(String guide, int position) {
        if (route.isEmpty()) {
            return result(false, "R2: la ruta esta vacia.");
        }
        if (!route.moveToPosition(guide, position)) {
            return result(false, "R2: no se pudo mover la guia " + guide + " a la posicion " + position + ".");
        }
        boolean invalidated = invalidateForRouteChange();
        return result(true, "R2: parada " + guide + " movida a la posicion " + position + "."
                + invalidatedSuffix(invalidated));
    }

    public OperationResult removeStop(String guide) {
        if (route.isEmpty()) {
            return result(false, "R2: la ruta esta vacia.");
        }
        Stop target = route.find(guide);
        if (target == null) {
            return result(false, "R2: la guia " + guide + " no esta en la ruta.");
        }
        if (target.getStatus() == StopStatus.ENTREGADA) {
            return result(false, "R2/R6: no se puede eliminar una parada ya entregada.");
        }
        Shipment removed = route.removeByGuide(guide);
        boolean invalidated = invalidateForRouteChange();
        pending.offerFirst(removed, currentMinute);
        return result(true, "R2: parada " + guide + " eliminada y devuelta al frente de pendientes."
                + invalidatedSuffix(invalidated));
    }

    public OperationResult insertStop(int position) {
        if (pending.isEmpty()) {
            return result(false, "R2: no hay envios pendientes para insertar.");
        }
        if (position < 1 || position > route.size() + 1) {
            return result(false, "R2: posicion de insercion invalida: " + position + ".");
        }
        if (route.size() >= config.getTrunkCapacity()) {
            return result(false, "R9: la ruta alcanzo la capacidad del maletero ("
                    + config.getTrunkCapacity() + " paradas).");
        }
        int since = pending.peekSince();
        Shipment shipment = pending.poll();
        if (!route.insertAt(shipment, position)) {
            pending.offerFirst(shipment, since);
            return result(false, "R2: no se pudo insertar la parada en la posicion " + position + ".");
        }
        metrics.shipmentAssigned();
        metrics.recordWait(shipment.getGuide(), currentMinute - since);
        boolean invalidated = invalidateForRouteChange();
        return result(true, "R2: envio " + shipment.getGuide() + " insertado en la posicion " + position + "."
                + invalidatedSuffix(invalidated));
    }

    public OperationResult loadTrunk() {
        if (!trunk.isEmpty()) {
            return result(false, "R3/RF-05: el maletero no esta vacio; descarguelo antes de volver a cargar.");
        }
        List<Stop> remaining = route.remainingForward();
        if (remaining.isEmpty()) {
            return result(false, "R3: no hay paradas pendientes en la ruta para cargar.");
        }
        if (route.size() > config.getTrunkCapacity()) {
            return result(false, "R9: la ruta supera la capacidad del maletero.");
        }
        for (int index = remaining.size() - 1; index >= 0; index--) {
            trunk.push(remaining.get(index).getShipment());
        }
        loadInvalid = false;
        loadInvalidReason = "";
        metrics.loadCompleted();
        return result(true, "R3: maletero cargado de la ultima parada hacia la primera; tope = "
                + remaining.get(0).getShipment().getGuide() + ".");
    }

    public OperationResult unloadTrunk() {
        if (trunk.isEmpty()) {
            return result(false, "R3: el maletero ya esta vacio.");
        }
        int count = trunk.size();
        trunk.drain();
        loadInvalid = true;
        loadInvalidReason = "Maletero descargado manualmente; debe recargarse antes de salir.";
        return result(true, "R3: maletero descargado (" + count + " paquetes).");
    }

    public OperationResult validateLoad() {
        ValidationResult validation = inspectLoad();
        return result(validation.valid(), validation.message());
    }

    public ValidationResult inspectLoad() {
        if (loadInvalid) {
            return ValidationResult.invalid("R5: carga invalida. " + loadInvalidReason);
        }
        if (trunk.isEmpty()) {
            return ValidationResult.invalid("R4: el maletero esta vacio; no hay carga que validar.");
        }
        List<Stop> remaining = route.remainingForward();
        if (remaining.isEmpty()) {
            return ValidationResult.invalid("R4: la ruta no tiene paradas pendientes.");
        }
        List<Shipment> stack = trunk.snapshotTopToBottom();
        int common = Math.min(remaining.size(), stack.size());
        for (int index = 0; index < common; index++) {
            String expected = remaining.get(index).getShipment().getGuide();
            String actual = stack.get(index).getGuide();
            if (!expected.equalsIgnoreCase(actual)) {
                return ValidationResult.mismatch(index + 1, expected, actual,
                        "R4: la carga no coincide en la posicion " + (index + 1)
                                + " (ruta espera " + expected + ", maletero tiene " + actual + ").");
            }
        }
        if (remaining.size() != stack.size()) {
            String expected = remaining.size() > common
                    ? remaining.get(common).getShipment().getGuide()
                    : "(sin parada)";
            String actual = stack.size() > common ? stack.get(common).getGuide() : "(sin paquete)";
            return ValidationResult.mismatch(common + 1, expected, actual,
                    "R4: la carga no coincide en la posicion " + (common + 1)
                            + " (ruta espera " + expected + ", maletero tiene " + actual + ").");
        }
        return ValidationResult.valid("R4: la carga coincide exactamente con la ruta (inversion correcta).");
    }

    public OperationResult deliver() {
        Stop current = route.getCurrent();
        if (current == null) {
            return result(false, "R6: no hay una parada actual sobre la cual entregar.");
        }
        if (current.getStatus() == StopStatus.ENTREGADA) {
            return result(false, "R6: la parada actual ya fue entregada.");
        }
        if (trunk.isEmpty()) {
            return result(false, "R6: el maletero esta vacio; no hay paquete en el tope.");
        }
        Shipment top = trunk.peek();
        if (!top.getGuide().equalsIgnoreCase(current.getShipment().getGuide())) {
            return result(false, "R6/R4: el tope del maletero es " + top.getGuide()
                    + " y la parada actual es " + current.getShipment().getGuide() + ".");
        }
        trunk.pop();
        top.registerAttempt();
        current.setStatus(StopStatus.ENTREGADA);
        metrics.shipmentDelivered();
        route.advanceAfterCurrent();
        return result(true, "R6: entrega realizada de " + top.getGuide() + "; apuntador avanzado.");
    }

    public OperationResult failDelivery() {
        Stop current = route.getCurrent();
        if (current == null) {
            return result(false, "R7: no hay una parada actual para registrar la falla.");
        }
        if (current.getStatus() == StopStatus.ENTREGADA) {
            return result(false, "R7: la parada actual ya fue entregada.");
        }
        if (trunk.isEmpty()) {
            return result(false, "R7: el maletero esta vacio; no hay paquete para reorganizar.");
        }
        Shipment top = trunk.peek();
        if (!top.getGuide().equalsIgnoreCase(current.getShipment().getGuide())) {
            return result(false, "R7/R4: el tope del maletero no corresponde a la parada actual.");
        }
        top.registerAttempt();
        int moves = trunk.moveTopToBottom();
        current.setStatus(StopStatus.FALLIDA);
        metrics.deliveryFailed();
        metrics.reorganizationMoves(moves);
        route.relocateCurrentToEnd();
        return result(true, "R7: entrega fallida de " + top.getGuide()
                + "; parada al final de la ruta y " + moves + " movimientos de reorganizacion.");
    }

    public OperationResult closeRoute() {
        if (route.isEmpty() && trunk.isEmpty()) {
            return result(false, "R8: no hay una ruta abierta para cerrar.");
        }
        int delivered = 0;
        int returned = 0;
        int retried = 0;
        for (Stop stop : route.forwardView()) {
            if (stop.getStatus() == StopStatus.ENTREGADA) {
                delivered++;
                continue;
            }
            Shipment shipment = stop.getShipment();
            if (shipment.getAttempts() >= 2) {
                retries.offerLast(shipment, currentMinute);
                metrics.shipmentSentToRetry();
                retried++;
            } else {
                pending.offerLast(shipment, currentMinute);
                metrics.shipmentReturnedToPending();
                returned++;
            }
        }
        trunk.drain();
        route.clear();
        loadInvalid = false;
        loadInvalidReason = "";
        return result(true, "R8: ruta cerrada. Entregados: " + delivered
                + "; devueltos a pendientes: " + returned
                + "; enviados a reintentos: " + retried + ".");
    }

    public OperationResult generateReport() {
        MetricsSnapshot report = metrics.snapshot();
        return result(true, "RF-10: reporte generado. Recibidos: " + report.received()
                + "; asignados: " + report.assigned()
                + "; entregados: " + report.delivered()
                + "; devueltos: " + report.returnedToPending()
                + "; reintentos: " + report.retryTransfers() + ".");
    }

    public OperationResult registerTraversal(boolean forward) {
        int size = route.size();
        if (size == 0) {
            return result(false, "R4: no hay paradas para recorrer; la ruta esta vacia.");
        }
        return result(true, "R4: recorrido " + (forward ? "hacia adelante" : "hacia atras")
                + " de " + size + " paradas.");
    }

    public OperationResult authorizeDeparture() {
        ValidationResult validation = inspectLoad();
        if (!validation.valid()) {
            return result(false, validation.message());
        }
        return result(true, "R4: salida autorizada; la carga coincide con la ruta.");
    }

    public List<String> forwardTraversal() {
        List<String> lines = new ArrayList<>();
        int position = 1;
        for (Stop stop : route.forwardView()) {
            lines.add(formatStop(position, stop));
            position++;
        }
        if (lines.isEmpty()) {
            lines.add("(ruta vacia)");
        }
        return Collections.unmodifiableList(lines);
    }

    public List<String> backwardTraversal() {
        List<String> lines = new ArrayList<>();
        int position = route.size();
        for (Stop stop : route.backwardView()) {
            lines.add(formatStop(position, stop));
            position--;
        }
        if (lines.isEmpty()) {
            lines.add("(ruta vacia)");
        }
        return Collections.unmodifiableList(lines);
    }

    private String formatStop(int position, Stop stop) {
        String mark = stop == route.getCurrent() ? " <== ACTUAL" : "";
        return position + ". " + stop.getShipment().getGuide()
                + " | " + stop.getShipment().getDestination()
                + " | " + stop.getStatus()
                + " | intentos: " + stop.getShipment().getAttempts()
                + mark;
    }

    private boolean invalidateForRouteChange() {
        if (trunk.isEmpty()) {
            return false;
        }
        trunk.drain();
        loadInvalid = true;
        loadInvalidReason = "la ruta cambio despues de cargar el maletero; descarguelo y recarguelo.";
        metrics.loadInvalidated();
        return true;
    }

    private String invalidatedSuffix(boolean invalidated) {
        return invalidated ? " R5: carga invalidada y maletero descargado." : "";
    }

    private boolean isGuideKnown(String guide) {
        return pending.containsGuide(guide)
                || retries.containsGuide(guide)
                || route.containsGuide(guide)
                || trunk.containsGuide(guide);
    }

    private OperationResult result(boolean success, String message) {
        log.add(new LogEntry(LocalDateTime.now(), success, message));
        return new OperationResult(success, message);
    }

    public PendingShipments getPending() {
        return pending;
    }

    public RetryShipments getRetries() {
        return retries;
    }

    public Route getRoute() {
        return route;
    }

    public Trunk getTrunk() {
        return trunk;
    }

    public AppConfig getConfig() {
        return config;
    }

    public MetricsSnapshot getMetrics() {
        return metrics.snapshot();
    }

    public List<LogEntry> getLog() {
        return Collections.unmodifiableList(new ArrayList<>(log));
    }

    public boolean isLoadInvalid() {
        return loadInvalid;
    }

    public String getLoadInvalidReason() {
        return loadInvalidReason;
    }
}
