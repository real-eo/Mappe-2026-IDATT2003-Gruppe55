package edu.ntnu.idi.idatt2003.millions.infrastructure.persistence;

import edu.ntnu.idi.idatt2003.millions.model.GameState;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Abstraction for storing and loading game state.
 */
public interface GameRepository {

    /**
     * Initializes the repository schema if needed.
     *
     * @throws SQLException if initialization fails
     */
    void initialize() throws SQLException;

    /**
     * Saves the provided game state.
     *
     * @param state the game state to persist
     * @return the database identifier of the save
     * @throws SQLException if the save fails
     */
    long save(GameState state) throws SQLException;

    /**
     * Loads a previously saved game state.
     *
     * @param saveId the save identifier
     * @return an Optional containing the game state if found
     * @throws SQLException if loading fails
     */
    Optional<GameState> load(long saveId) throws SQLException;
}
