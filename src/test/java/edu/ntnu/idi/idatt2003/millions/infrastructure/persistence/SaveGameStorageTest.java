package edu.ntnu.idi.idatt2003.millions.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SaveGameStorageTest {

    @Test
    void resolveDefaultDatabasePath_returnsDbPath_andEnsuresParentDirectory() throws Exception {
        Path path = SaveGameStorage.resolveDefaultDatabasePath();

        assertNotNull(path);
        assertEquals("millions.db", path.getFileName().toString());
        assertNotNull(path.getParent());
        assertTrue(Files.exists(path.getParent()));
        assertTrue(Files.isDirectory(path.getParent()));
    }
}
