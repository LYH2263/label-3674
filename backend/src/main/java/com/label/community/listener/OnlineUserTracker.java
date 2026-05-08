package com.label.community.listener;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public final class OnlineUserTracker {
    private static final AtomicInteger ONLINE = new AtomicInteger(0);
    private static final Instant STARTED_AT = Instant.now();

    private OnlineUserTracker() {
    }

    public static void onLoginSession() {
        ONLINE.incrementAndGet();
    }

    public static void onLogoutSession() {
        int now = ONLINE.decrementAndGet();
        if (now < 0) {
            ONLINE.set(0);
        }
    }

    public static int onlineUsers() {
        return ONLINE.get();
    }

    public static long uptimeMinutes() {
        return Duration.between(STARTED_AT, Instant.now()).toMinutes();
    }
}
