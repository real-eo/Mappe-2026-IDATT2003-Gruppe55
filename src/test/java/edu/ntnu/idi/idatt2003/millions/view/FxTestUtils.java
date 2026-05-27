package edu.ntnu.idi.idatt2003.millions.view;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class FxTestUtils {

    private static volatile boolean initialized;

    private FxTestUtils() {
    }

    public static void initToolkit() {
        if (initialized) {
            return;
        }
        synchronized (FxTestUtils.class) {
            if (initialized) {
                return;
            }
            try {
                Platform.startup(() -> {
                });
            } catch (IllegalStateException ignored) {
                // Toolkit already initialized in this JVM.
            }
            initialized = true;
        }
    }

    public static void runOnFxThreadAndWait(Runnable action) {
        initToolkit();
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                error.set(throwable);
            } finally {
                latch.countDown();
            }
        });

        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("Timed out waiting for JavaFX action");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for JavaFX action", exception);
        }

        if (error.get() != null) {
            throw new AssertionError("JavaFX action failed", error.get());
        }
    }
}
