package courier.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Metrics {
    private int received;
    private int assigned;
    private int delivered;
    private int returnedToPending;
    private int retryTransfers;
    private int failedDeliveries;
    private int reorganizationMoves;
    private int loadInvalidations;
    private int loads;
    private final List<WaitRecord> waits = new ArrayList<>();

    public void shipmentReceived() {
        received++;
    }

    public void shipmentAssigned() {
        assigned++;
    }

    public void shipmentDelivered() {
        delivered++;
    }

    public void shipmentReturnedToPending() {
        returnedToPending++;
    }

    public void shipmentSentToRetry() {
        retryTransfers++;
    }

    public void deliveryFailed() {
        failedDeliveries++;
    }

    public void reorganizationMoves(int moves) {
        reorganizationMoves += moves;
    }

    public void loadInvalidated() {
        loadInvalidations++;
    }

    public void loadCompleted() {
        loads++;
    }

    public void recordWait(String guide, int minutes) {
        waits.add(new WaitRecord(guide, Math.max(0, minutes)));
    }

    public MetricsSnapshot snapshot() {
        int total = 0;
        int maximum = 0;
        for (WaitRecord wait : waits) {
            total += wait.minutes();
            maximum = Math.max(maximum, wait.minutes());
        }
        double average = waits.isEmpty() ? 0.0 : (double) total / waits.size();
        return new MetricsSnapshot(
                received,
                assigned,
                delivered,
                returnedToPending,
                retryTransfers,
                failedDeliveries,
                reorganizationMoves,
                loadInvalidations,
                loads,
                average,
                maximum,
                Collections.unmodifiableList(new ArrayList<>(waits)));
    }
}
