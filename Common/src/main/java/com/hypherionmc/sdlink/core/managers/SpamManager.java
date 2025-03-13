package com.hypherionmc.sdlink.core.managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author HypherionSA
 * Basic Message Spam Detector
 */
public final class SpamManager {

    private final ConcurrentHashMap<String, List<Long>> messageTimestamps = new ConcurrentHashMap<>();
    private final Set<String> blockedMessages = new HashSet<>();

    private final int threshold;
    private final int timeWindowMillis;
    private final int blockMillis;
    private final ScheduledExecutorService executor;

    public SpamManager(int threshold, int timeWindowMillis, int blockMillis, ScheduledExecutorService executor) {
        this.threshold = threshold;
        this.timeWindowMillis = timeWindowMillis;
        this.blockMillis = blockMillis;
        this.executor = executor;
        startSpamChecker();
    }

    public void receiveMessage(String message) {
        long currentTime = System.currentTimeMillis();

        messageTimestamps.compute(message, (msg, timestamps) -> {
            if (timestamps == null)
                timestamps = new ArrayList<>();

            timestamps.add(currentTime);
            return new ArrayList<>(
                    timestamps.stream()
                            .filter(timestamp -> currentTime - timestamp <= timeWindowMillis)
                            .toList()
            );
        });

        if (messageTimestamps.get(message).size() >= threshold) {
            blockedMessages.add(message);
        }
    }

    public boolean isBlocked(String message) {
        return blockedMessages.contains(message);
    }

    private void startSpamChecker() {
        executor.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            blockedMessages.removeIf(message -> {
                List<Long> timestamps = messageTimestamps.getOrDefault(message, new ArrayList<>());
                timestamps = new ArrayList<>(
                        timestamps.stream()
                                .filter(timestamp -> currentTime - timestamp <= timeWindowMillis)
                                .toList()
                );
                messageTimestamps.put(message, timestamps);
                return timestamps.size() < threshold;
            });
        }, blockMillis, blockMillis, TimeUnit.MILLISECONDS);
    }

}
