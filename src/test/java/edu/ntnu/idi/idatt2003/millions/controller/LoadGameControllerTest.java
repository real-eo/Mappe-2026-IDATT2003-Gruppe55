package edu.ntnu.idi.idatt2003.millions.controller;

import edu.ntnu.idi.idatt2003.millions.infrastructure.persistence.GameSaveSummary;
import edu.ntnu.idi.idatt2003.millions.model.GameState;
import edu.ntnu.idi.idatt2003.millions.view.FxTestUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadGameControllerTest {

    @Test
    void loadSaves_allCallbacksNull_doesNotThrow() {
        LoadGameController controller = new LoadGameController();

        assertDoesNotThrow(() -> controller.loadSaves(null, null));
    }

    @Test
    void loadSave_allCallbacksNull_doesNotThrow() {
        LoadGameController controller = new LoadGameController();

        assertDoesNotThrow(() -> controller.loadSave(1L, null, null));
    }

    @Test
    void loadSaves_invokesSuccessCallback() throws Exception {
        FxTestUtils.initToolkit();

        LoadGameController controller = new LoadGameController();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<GameSaveSummary>> result = new AtomicReference<>();

        controller.loadSaves(saves -> {
            result.set(saves);
            latch.countDown();
        }, error -> latch.countDown());

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertNotNull(result.get());
    }

    @Test
    void loadSave_missingId_invokesErrorCallback() throws Exception {
        FxTestUtils.initToolkit();

        LoadGameController controller = new LoadGameController();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorMessage = new AtomicReference<>();
        AtomicReference<GameState> loaded = new AtomicReference<>();

        controller.loadSave(-1L, state -> {
            loaded.set(state);
            latch.countDown();
        }, error -> {
            errorMessage.set(error);
            latch.countDown();
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertTrue(loaded.get() == null);
        assertNotNull(errorMessage.get());
    }
}
