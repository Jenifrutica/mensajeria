package courier.service;

import java.util.List;

public record MetricsSnapshot(
        int received,
        int assigned,
        int delivered,
        int returnedToPending,
        int retryTransfers,
        int failedDeliveries,
        int reorganizationMoves,
        int loadInvalidations,
        int loads,
        double averageWait,
        int maximumWait,
        List<WaitRecord> waits) {
}
