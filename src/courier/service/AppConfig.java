package courier.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AppConfig {
    private static final int DEFAULT_TRUNK_CAPACITY = 6;
    private static final int DEFAULT_PENDING_CAPACITY = 20;

    private final int trunkCapacity;
    private final int pendingCapacity;

    public AppConfig(int trunkCapacity, int pendingCapacity) {
        this.trunkCapacity = trunkCapacity;
        this.pendingCapacity = pendingCapacity;
    }

    public static AppConfig load() {
        Properties properties = new Properties();
        Path path = Path.of("config.properties");
        if (Files.isRegularFile(path)) {
            try (InputStream input = Files.newInputStream(path)) {
                properties.load(input);
            } catch (Exception ignored) {
            }
        }
        int trunk = readPositive(properties.getProperty("trunk.capacity"), DEFAULT_TRUNK_CAPACITY);
        int pending = readPositive(properties.getProperty("pending.capacity"), DEFAULT_PENDING_CAPACITY);
        return new AppConfig(trunk, pending);
    }

    private static int readPositive(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    public int getTrunkCapacity() {
        return trunkCapacity;
    }

    public int getPendingCapacity() {
        return pendingCapacity;
    }
}
