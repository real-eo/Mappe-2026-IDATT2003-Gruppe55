package edu.ntnu.idi.idatt2003.millions.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
}
