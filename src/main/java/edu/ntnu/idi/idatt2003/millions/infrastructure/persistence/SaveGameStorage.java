package edu.ntnu.idi.idatt2003.millions.infrastructure.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides shared save-game storage paths.
 */
public final class SaveGameStorage {

    private SaveGameStorage() {
    }

    /**
     * Resolves the default database path for saved games.
     *
     * @return the database path
     * @throws IOException if the save directory cannot be created
     */
    public static Path resolveDefaultDatabasePath() throws IOException {
        Path path = Path.of("saves", "millions.db");
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        return path;
    }
}
