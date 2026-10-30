package courier.service;

import java.time.LocalDateTime;

public record LogEntry(LocalDateTime time, boolean success, String message) {
}
