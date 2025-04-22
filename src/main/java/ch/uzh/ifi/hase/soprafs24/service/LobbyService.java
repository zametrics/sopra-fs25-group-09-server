package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional // Ensure methods modifying data are transactional
public class LobbyService {

    private final Logger log = LoggerFactory.getLogger(LobbyService.class);
    private final LobbyRepository lobbyRepository;
    private final UserService userService; // Assuming you have UserService for validation

    @Autowired
    public LobbyService(@Qualifier("lobbyRepository") LobbyRepository lobbyRepository, UserService userService) {
        this.lobbyRepository = lobbyRepository;
        this.userService = userService;
    }

    // Get all lobbies
    public List<Lobby> getLobbies() {
        log.debug("Fetching all lobbies");
        return this.lobbyRepository.findAll();
    }

    // Create a new lobby
    public Lobby createLobby(Lobby newLobby) {
        // Validate required fields
        if (newLobby.getLobbyOwner() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lobby owner must be specified during creation.");
        }
        // Optional: Validate owner ID exists
        try {
            userService.getUserById(newLobby.getLobbyOwner());
        } catch (ResponseStatusException e) {
             log.error("Attempted to create lobby with non-existent owner ID {}", newLobby.getLobbyOwner());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Specified lobby owner with ID " + newLobby.getLobbyOwner() + " not found.");
        }


        // Ensure creator is in player list
        if (newLobby.getPlayerIds() == null) {
            newLobby.setPlayerIds(new ArrayList<>());
        }
        if (!newLobby.getPlayerIds().contains(newLobby.getLobbyOwner())) {
             log.debug("Adding lobby owner {} to initial player list.", newLobby.getLobbyOwner());
             newLobby.addPlayerId(newLobby.getLobbyOwner());
        }

        // Set default values ONLY if they are truly absent (null/0)
        if (newLobby.getNumOfMaxPlayers() == null) newLobby.setNumOfMaxPlayers(8L);
        if (newLobby.getLanguage() == null) newLobby.setLanguage("english");
        if (newLobby.getNumOfRounds() == null) newLobby.setNumOfRounds(3L);
        if (newLobby.getDrawTime() == 0) newLobby.setDrawTime(80); // Default if 0
        if (newLobby.getType() == null) newLobby.setType("anything");
        if (newLobby.getPainterHistoryTokens() == null) newLobby.setPainterHistoryTokens(Collections.emptySet());


        newLobby = lobbyRepository.save(newLobby);
        lobbyRepository.flush(); // Persist immediately
        log.info("Created Lobby with ID: {}", newLobby.getId());
        log.debug("Created Lobby Details: {}", newLobby);
        return newLobby;
    }

    // Get lobby by ID
    public Lobby getLobbyById(Long id) {
        log.debug("Attempting to fetch lobby with ID: {}", id);
        return lobbyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Lobby not found with ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Lobby with ID %d was not found", id));
                });
    }

    /**
     * Updates an existing lobby with data provided in the lobbyUpdatesFromDTO.
     * Only fields present (non-null for Objects, > 0 for drawTime) in the DTO
     * and different from the existing value will be updated.
     * Player list management is handled separately via join/leave methods.
     *
     * @param id The ID of the lobby to update.
     * @param lobbyUpdatesFromDTO Lobby object containing potential updates (usually mapped from LobbyPutDTO).
     * @throws ResponseStatusException if lobby not found or validation fails.
     */
    public void updateLobby(Long id, Lobby lobbyUpdatesFromDTO) {
        // The 'lobbyUpdatesFromDTO' object is the result of mapping LobbyPutDTO.
        // Fields *not* included in the PUT request body will be null (for Objects)
        // or default values (for primitives like int) after mapping.

        Lobby existingLobby = getLobbyById(id); // Fetches or throws NOT_FOUND
        log.info("Attempting to update lobby {} with data: owner={}, maxPlayers={}, lang={}, rounds={}, drawTime={}, type={}",
                id, lobbyUpdatesFromDTO.getLobbyOwner(), lobbyUpdatesFromDTO.getNumOfMaxPlayers(),
                lobbyUpdatesFromDTO.getLanguage(), lobbyUpdatesFromDTO.getNumOfRounds(),
                lobbyUpdatesFromDTO.getDrawTime(), lobbyUpdatesFromDTO.getType());

        boolean updated = false; // Track if any changes are made

        // 1. Update Lobby Owner (if provided and different)
        if (lobbyUpdatesFromDTO.getLobbyOwner() != null &&
            !Objects.equals(existingLobby.getLobbyOwner(), lobbyUpdatesFromDTO.getLobbyOwner())) {
            // Validate if the new owner is actually in the lobby
            if (!existingLobby.getPlayerIds().contains(lobbyUpdatesFromDTO.getLobbyOwner())) {
                 log.warn("Attempted to set owner {} who is not currently in lobby {}. Ignoring owner update.", lobbyUpdatesFromDTO.getLobbyOwner(), id);
                // Depending on requirements, you might throw an exception instead:
                // throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proposed new owner ID " + lobbyUpdatesFromDTO.getLobbyOwner() + " is not in the lobby's player list.");
            } else {
                log.info("Updating Lobby {} Owner from {} to {}", id, existingLobby.getLobbyOwner(), lobbyUpdatesFromDTO.getLobbyOwner());
                existingLobby.setLobbyOwner(lobbyUpdatesFromDTO.getLobbyOwner());
                updated = true;
            }
        }

        // 2. Update Max Players (if provided and different)
        if (lobbyUpdatesFromDTO.getNumOfMaxPlayers() != null &&
            !Objects.equals(existingLobby.getNumOfMaxPlayers(), lobbyUpdatesFromDTO.getNumOfMaxPlayers())) {
             // Validation: must be >= current number of players
             if (lobbyUpdatesFromDTO.getNumOfMaxPlayers() < existingLobby.getPlayerIds().size()) {
                 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Max players cannot be set lower than the current number of players.");
             }
             // Validation: maybe a sensible upper limit? (e.g., <= 10)
             if (lobbyUpdatesFromDTO.getNumOfMaxPlayers() > 10) { // Example limit
                  throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum number of players cannot exceed 10.");
             }
            log.info("Updating Lobby {} Max Players from {} to {}", id, existingLobby.getNumOfMaxPlayers(), lobbyUpdatesFromDTO.getNumOfMaxPlayers());
            existingLobby.setNumOfMaxPlayers(lobbyUpdatesFromDTO.getNumOfMaxPlayers());
            updated = true;
        }

        // 3. Update Language (if provided and different)
        if (lobbyUpdatesFromDTO.getLanguage() != null &&
            !existingLobby.getLanguage().equals(lobbyUpdatesFromDTO.getLanguage())) {
            log.info("Updating Lobby {} Language from '{}' to '{}'", id, existingLobby.getLanguage(), lobbyUpdatesFromDTO.getLanguage());
            // Optional: Validate language value if needed (e.g., against a predefined list)
            existingLobby.setLanguage(lobbyUpdatesFromDTO.getLanguage());
            updated = true;
        }

        // 4. Update Rounds (if provided and different)
        if (lobbyUpdatesFromDTO.getNumOfRounds() != null &&
            !Objects.equals(existingLobby.getNumOfRounds(), lobbyUpdatesFromDTO.getNumOfRounds())) {
             // Validation: must be > 0
             if (lobbyUpdatesFromDTO.getNumOfRounds() <= 0) {
                  throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Number of rounds must be positive.");
             }
             // Optional: Sensible upper limit? (e.g., <= 20)
             if (lobbyUpdatesFromDTO.getNumOfRounds() > 20) {
                   throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Number of rounds cannot exceed 20.");
             }
            log.info("Updating Lobby {} Num Rounds from {} to {}", id, existingLobby.getNumOfRounds(), lobbyUpdatesFromDTO.getNumOfRounds());
            existingLobby.setNumOfRounds(lobbyUpdatesFromDTO.getNumOfRounds());
            updated = true;
        }

        // 5. Update Draw Time (if provided AND different - check primitive default)
        // Only update if a value > 0 is given via DTO and it's different from current.
        if (lobbyUpdatesFromDTO.getDrawTime() > 0 &&
            existingLobby.getDrawTime() != lobbyUpdatesFromDTO.getDrawTime()) {
             // Validation: must be within a reasonable range (e.g., 10-300 seconds)
             if (lobbyUpdatesFromDTO.getDrawTime() < 10 || lobbyUpdatesFromDTO.getDrawTime() > 300) {
                 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Draw time must be between 10 and 300 seconds.");
             }
            log.info("Updating Lobby {} Draw Time from {} to {}", id, existingLobby.getDrawTime(), lobbyUpdatesFromDTO.getDrawTime());
            existingLobby.setDrawTime(lobbyUpdatesFromDTO.getDrawTime());
            updated = true;
        }

        // 6. Update Type (if provided and different)
         if (lobbyUpdatesFromDTO.getType() != null &&
            !existingLobby.getType().equals(lobbyUpdatesFromDTO.getType())) {
            log.info("Updating Lobby {} Type from '{}' to '{}'", id, existingLobby.getType(), lobbyUpdatesFromDTO.getType());
             // Optional: Validate type value if needed (e.g., against a predefined list)
            existingLobby.setType(lobbyUpdatesFromDTO.getType());
            updated = true;
        }

        // 7. *** IMPORTANT: DO NOT UPDATE playerIds here ***
        // Player list management MUST happen via join/leave methods.

        // 8. Save to DB only if actual changes were made
        if (updated) {
            lobbyRepository.save(existingLobby);
            lobbyRepository.flush(); // Ensure changes hit the DB
            log.info("Lobby {} successfully updated.", id);
        } else {
            log.info("No effective changes detected for lobby {}. Skipping database save.", id);
        }
        // Controller returns void (204 No Content), so no return needed.
    }

    // Add a player to a lobby
    public Lobby addPlayerToLobby(Long lobbyId, Long playerId) {
        Lobby lobby = getLobbyById(lobbyId);

        // Validate lobby capacity
        if (lobby.getPlayerIds().size() >= lobby.getNumOfMaxPlayers()) {
             log.warn("Failed to add player {} to lobby {}: Lobby is full ({} players, max {}).", playerId, lobbyId, lobby.getPlayerIds().size(), lobby.getNumOfMaxPlayers());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Lobby is full.");
        }
        // Check if player already present
        if (lobby.getPlayerIds().contains(playerId)) {
             log.warn("Player {} already in lobby {}. No action taken.", playerId, lobbyId);
             return lobby; // Return current state is fine, no change needed
        }

        // Validate player ID exists
        try {
            userService.getUserById(playerId); // Check if user exists
             log.debug("Validated player ID {} exists.", playerId);
        } catch (ResponseStatusException e) {
             log.error("Attempted to add non-existent player ID {} to lobby {}", playerId, lobbyId);
            // Rethrow with potentially more specific message if needed, or just the original
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player with ID " + playerId + " not found.");
        }

        // Add player and save
        lobby.addPlayerId(playerId);
        lobby = lobbyRepository.save(lobby);
        lobbyRepository.flush();
        log.info("Player {} added to lobby {}", playerId, lobbyId);
        return lobby;
    }

    // Remove a player from a lobby
    public Lobby removePlayerFromLobby(Long lobbyId, Long playerId) {
        Lobby lobby = getLobbyById(lobbyId);

        // Check if player is actually in the lobby
        if (!lobby.getPlayerIds().contains(playerId)) {
            log.warn("Attempted to remove player {} who is not in lobby {}. No action taken.", playerId, lobbyId);
             return lobby; // Return current state if player already gone
        }

        // Remove player
        lobby.removePlayerId(playerId);
        log.info("Player {} removed from lobby {} player list.", playerId, lobbyId);

        // --- Lobby Deletion / Saving Logic ---
        if (lobby.getPlayerIds().isEmpty()) {
            // Lobby is now empty, delete it
            log.info("Lobby {} is now empty after removing player {}. Deleting lobby.", lobbyId, playerId);
            lobbyRepository.delete(lobby);
            lobbyRepository.flush();
             // Return the lobby object *as it was before deletion*
             // The controller DTO mapping will still work.
             return lobby;
        } else {
            // Lobby still has players.
            // Owner transfer logic is handled by the socket server calling updateLobby separately.
            // We just save the state with the player removed.
            lobby = lobbyRepository.save(lobby);
            lobbyRepository.flush();
            log.info("Saved lobby {} after removing player {}. Remaining players: {}.", lobbyId, playerId, lobby.getPlayerIds().size());
            return lobby;
        }
    }

    /**
     * Selects the next player to be the painter based on sequential rotation.
     * Ensures each player paints once per cycle. Uses player tokens.
     * Updates the lobby state (current painter, history) and saves it.
     *
     * @param lobbyId The ID of the lobby.
     * @return The updated Lobby object.
     * @throws ResponseStatusException if lobby not found or no players are present.
     */
    public Lobby selectNextPainter(Long lobbyId) {
        Lobby lobby = getLobbyById(lobbyId); // Fetches lobby or throws NOT_FOUND

        List<Long> playerIds = lobby.getPlayerIds();

        // --- Handle Empty Lobby ---
        if (playerIds.isEmpty()) {
            log.warn("Attempted to select painter in empty lobby: {}", lobbyId);
            lobby.setCurrentPainterToken(null);
            lobby.clearPainterHistory();
            lobbyRepository.save(lobby); // Save the cleaned state
            lobbyRepository.flush();
            // Consider if throwing is appropriate or just returning the empty state
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot select painter: Lobby is empty.");
        }

        // --- Get Current Player Tokens (Maintain Order if Possible) ---
        List<String> activePlayerTokens = new ArrayList<>();
        List<Long> validPlayerIdsInOrder = new ArrayList<>(); // Keep track of IDs corresponding to tokens
        for (Long playerId : playerIds) {
            try {
                User player = userService.getUserById(playerId); // Fetch user by ID
                activePlayerTokens.add(player.getToken()); // Get the token
                validPlayerIdsInOrder.add(playerId); // Add ID in the same order
                 log.trace("Found token {} for player {}", player.getToken(), playerId);
            } catch (ResponseStatusException e) {
                // Log if a player ID in the lobby list doesn't correspond to a valid user
                log.warn("Player ID {} listed in lobby {} not found via UserService. Skipping for painter rotation.", playerId, lobbyId);
            }
        }

        // Check if any valid players remain after fetching tokens
        if (activePlayerTokens.isEmpty()) {
            log.error("No valid players found for lobby {} after fetching tokens, though playerIds list was not empty.", lobbyId);
            lobby.setCurrentPainterToken(null);
            lobby.clearPainterHistory();
            lobbyRepository.save(lobby);
            lobbyRepository.flush();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not find tokens for any player currently listed in the lobby.");
        }
        log.debug("Lobby {}: Active players for rotation (tokens): {}", lobbyId, activePlayerTokens);


        // --- Painter Rotation Logic ---
        Set<String> history = lobby.getPainterHistoryTokens();
        String lastPainterToken = lobby.getCurrentPainterToken();
        String nextPainterToken = null;
        log.debug("Lobby {}: Current painter history: {}", lobbyId, history);
        log.debug("Lobby {}: Last painter was: {}", lobbyId, lastPainterToken);


        // Check if all currently active players have painted in this cycle
        boolean allActiveHavePainted = history.containsAll(activePlayerTokens);

        if (allActiveHavePainted) {
            log.info("Lobby {}: All active players {} have painted in this cycle. Resetting painter history.", lobbyId, activePlayerTokens);
            lobby.clearPainterHistory();
            history = lobby.getPainterHistoryTokens(); // Refresh the local history variable (now empty)
            lastPainterToken = null; // Force search to start from index 0 for the new cycle
        }

        // Determine starting point for the search based on the last painter's token
        int startIndex = 0;
        if (lastPainterToken != null) {
            int lastIndex = activePlayerTokens.indexOf(lastPainterToken);
            if (lastIndex != -1) { // Make sure the last painter is still in the active list
                startIndex = (lastIndex + 1) % activePlayerTokens.size();
                log.debug("Lobby {}: Last painter {} found at index {}. Starting next search from index {}.", lobbyId, lastPainterToken, lastIndex, startIndex);
            } else {
                 log.warn("Lobby {}: Last painter token {} not found in current active player tokens {}. Starting search from index 0.", lobbyId, lastPainterToken, activePlayerTokens);
            }
        } else {
             log.debug("Lobby {}: No last painter recorded or new cycle. Starting search from index 0.", lobbyId);
        }

        // Iterate through players starting from startIndex to find the next eligible painter
        log.debug("Lobby {}: Searching for next painter starting from index {}.", lobbyId, startIndex);
        for (int i = 0; i < activePlayerTokens.size(); i++) {
            int currentIndex = (startIndex + i) % activePlayerTokens.size(); // Wrap around the list
            String potentialPainterToken = activePlayerTokens.get(currentIndex);
            log.trace("Lobby {}: Checking player at index {} (token {}). History contains? {}", lobbyId, currentIndex, potentialPainterToken, history.contains(potentialPainterToken));

            if (!history.contains(potentialPainterToken)) {
                nextPainterToken = potentialPainterToken;
                log.info("Lobby {}: Found next painter at index {}: {}", lobbyId, currentIndex, nextPainterToken);
                break; // Found the next painter
            }
        }

        // Fallback: Should ideally not happen if history clearing is correct.
        if (nextPainterToken == null && !allActiveHavePainted) {
            // This might occur if the history contains tokens of players no longer active
            log.error("Lobby {}: Could not determine next painter sequentially, but not all active players have painted. History might contain inactive players. Selecting first active player not in history.", lobbyId);
            for (String token : activePlayerTokens) {
                if (!history.contains(token)) {
                    nextPainterToken = token;
                    log.warn("Lobby {}: Fallback selected painter: {}", lobbyId, nextPainterToken);
                    break;
                }
            }
            // If still null, something is very wrong
            if (nextPainterToken == null) {
                 log.error("Lobby {}: CRITICAL FALLBACK FAILURE - Cannot find any active player not in history {}. Selecting first active player {} and clearing history.", lobbyId, history, activePlayerTokens.get(0));
                 nextPainterToken = activePlayerTokens.get(0);
                 lobby.clearPainterHistory(); // Clear history as a recovery measure
            }
        } else if (nextPainterToken == null && allActiveHavePainted) {
             // Should have been caught by the history reset, but as a safety net
             log.error("Lobby {}: CRITICAL - History should have been reset, but still couldn't find next painter. Selecting first active player {} and clearing history.", lobbyId, activePlayerTokens.get(0));
             nextPainterToken = activePlayerTokens.get(0);
             lobby.clearPainterHistory();
        }

        // --- Update Lobby State ---
        if (nextPainterToken != null) {
            lobby.setCurrentPainterToken(nextPainterToken);
            lobby.addTokenToPainterHistory(nextPainterToken); // Add the new painter to history
             log.info("Lobby {}: Set current painter to {} and added to history.", lobbyId, nextPainterToken);
             log.debug("Lobby {}: Updated painter history: {}", lobbyId, lobby.getPainterHistoryTokens());
        } else {
            // Should be unreachable if fallbacks work, but safety first
            log.error("Lobby {}: CRITICAL - Failed to select any painter even with fallbacks. Setting current painter to null.", lobbyId);
            lobby.setCurrentPainterToken(null);
            // Do not modify history here, as the state is uncertain
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to determine next painter due to an unexpected state.");
        }

        // --- Save the updated Lobby ---
        lobby = lobbyRepository.save(lobby);
        lobbyRepository.flush(); // Ensure changes are persisted

        return lobby; // Return the updated lobby object
    }
}