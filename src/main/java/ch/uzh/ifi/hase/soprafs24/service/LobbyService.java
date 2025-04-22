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
@Transactional
public class LobbyService {

    private final Logger log = LoggerFactory.getLogger(LobbyService.class);
    private final LobbyRepository lobbyRepository;
    private final UserService userService;

    @Autowired
    public LobbyService(@Qualifier("lobbyRepository") LobbyRepository lobbyRepository, UserService userService) {
        this.lobbyRepository = lobbyRepository;
        this.userService = userService;
    }

    // Get all lobbies
    public List<Lobby> getLobbies() {
        return this.lobbyRepository.findAll();
    }

    // Create a new lobby with default values if not provided
    public Lobby createLobby(Lobby newLobby) {
        
        // Check if the lobbyOwner could be fetched correctly
        if (newLobby.getLobbyOwner() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lobby owner must be specified");
        }

        // Set default values if not provided
        if (newLobby.getNumOfMaxPlayers() == null) {
            newLobby.setNumOfMaxPlayers(8L);
        }
        
        if (newLobby.getLanguage() == null) {
            newLobby.setLanguage("english");
        }
        
        if (newLobby.getNumOfRounds() == null) {
            newLobby.setNumOfRounds(3L);
        }
        
        if (newLobby.getDrawTime() == 0) {
            newLobby.setDrawTime(80);
        }
        
        // Initialize playerIds if null
        if (newLobby.getPlayerIds() == null) {
            newLobby.setPlayerIds(new ArrayList<>());
        }

        if (newLobby.getType() == null) {
            newLobby.setType("anything");
        }

        
        
        // Saves the given entity but data is only persisted in the database once
        // flush() is called
        newLobby = lobbyRepository.save(newLobby);
        lobbyRepository.flush();

        log.debug("Created Lobby: {}", newLobby);
        return newLobby;
    }

    // Get lobby by ID    ########## NOT YET TESTED #########
    public Lobby getLobbyById(Long id) {
        Optional<Lobby> lobbyOptional = lobbyRepository.findById(id);
        if (lobbyOptional.isPresent()) {
            return lobbyOptional.get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                String.format("Lobby with ID %d was not found", id));
        }
    }

    // Update an existing lobby  ########## NOT YET TESTED #########
    public Lobby updateLobby(Long id, Lobby lobbyUpdate) {
        Lobby existingLobby = getLobbyById(id);
        
        // Update fields if they are not null
        if (lobbyUpdate.getNumOfMaxPlayers() != null) {
            existingLobby.setNumOfMaxPlayers(lobbyUpdate.getNumOfMaxPlayers());
        }
        
        if (lobbyUpdate.getLanguage() != null) {
            existingLobby.setLanguage(lobbyUpdate.getLanguage());
        }
        
        if (lobbyUpdate.getNumOfRounds() != null) {
            existingLobby.setNumOfRounds(lobbyUpdate.getNumOfRounds());
        }
        
        // Always update drawTime
        existingLobby.setDrawTime(lobbyUpdate.getDrawTime());
        
        // If player IDs are provided, update them
        if (lobbyUpdate.getPlayerIds() != null) {
            existingLobby.setPlayerIds(lobbyUpdate.getPlayerIds());
        }

        if (lobbyUpdate.getType() != null) {
            existingLobby.setType(lobbyUpdate.getType());
        }
        
        // Save and return updated lobby
        existingLobby = lobbyRepository.save(existingLobby);
        lobbyRepository.flush();
        
        return existingLobby;
    }

    // Add a player to a lobby  ########## NOT YET TESTED #########
    public Lobby addPlayerToLobby(Long lobbyId, Long playerId) {
        Lobby lobby = getLobbyById(lobbyId);
        
        // Check if lobby is full
        if (lobby.getPlayerIds().size() >= lobby.getNumOfMaxPlayers()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lobby is full");
        }
        
        // Check if player is already in the lobby
        if (lobby.getPlayerIds().contains(playerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player is already in the lobby");
        }
        
        // Add player
        lobby.addPlayerId(playerId);
        
        // Save and return updated lobby
        lobby = lobbyRepository.save(lobby);
        lobbyRepository.flush();
        
        return lobby;
    }

    // Remove a player from a lobby
    public Lobby removePlayerFromLobby(Long lobbyId, Long playerId) {
        Lobby lobby = getLobbyById(lobbyId);
        
        // Check if player is in the lobby
        if (!lobby.getPlayerIds().contains(playerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player is not in the lobby");
        }
        
        // Remove player
        lobby.removePlayerId(playerId);
        
        // Check if the lobby is now empty
        if (lobby.getPlayerIds().isEmpty()) {
            // If lobby is empty, delete it
            lobbyRepository.delete(lobby);
            log.debug("Deleted empty Lobby: {}", lobby);
            return lobby; // Note: This lobby will not exist in the database anymore
        }
        
        // Save and return updated lobby
        lobby = lobbyRepository.save(lobby);
        lobbyRepository.flush();
        
        return lobby;
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
            // Save the cleaned state directly
            lobbyRepository.save(lobby);
            lobbyRepository.flush();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot select painter: Lobby is empty.");
        }

        // --- Get Current Player Tokens (Maintain Order if Possible) ---
        List<String> activePlayerTokens = new ArrayList<>();
        for (Long playerId : playerIds) {
            try {
                User player = userService.getUserById(playerId); // Fetch user by ID
                activePlayerTokens.add(player.getToken()); // Get the token

            } catch (ResponseStatusException e) {
                log.warn("Player ID {} listed in lobby {} not found in user service. Removing from rotation consideration.", playerId, lobbyId);
            }
        }
        // nach-checken if any valid players remain after fetching tokens
        if (activePlayerTokens.isEmpty()) {
            log.error("No valid players found for lobby {} after fetching tokens, though playerIds list was not empty.", lobbyId);
            lobby.setCurrentPainterToken(null);
            lobby.clearPainterHistory();
            lobbyRepository.save(lobby);
            lobbyRepository.flush();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not find tokens for any player listed in the lobby.");
        }

        //  Rotation Logic
        Set<String> history = lobby.getPainterHistoryTokens();
        String lastPainterToken = lobby.getCurrentPainterToken();
        String nextPainterToken = null;

        // Check if all currently active players have painted in this cycle
        boolean allActiveHavePainted = history.containsAll(activePlayerTokens);

        if (allActiveHavePainted) {
            log.info("Lobby {}: All active players have painted. Resetting painter history.", lobbyId);
            lobby.clearPainterHistory();
            history = lobby.getPainterHistoryTokens(); // Refresh the local history variable (now empty)
            lastPainterToken = null; // Ensures search starts from index 0 for the new cycle
        }

        // Determine starting point for the search
        int startIndex = 0;
        if (lastPainterToken != null) {
            int lastIndex = activePlayerTokens.indexOf(lastPainterToken);
            if (lastIndex != -1) { // Make sure the last painter is still in the list
                startIndex = (lastIndex + 1) % activePlayerTokens.size();
            }
        }

        // Iterate through players starting from startIndex to find the next painter
        for (int i = 0; i < activePlayerTokens.size(); i++) {
            int currentIndex = (startIndex + i) % activePlayerTokens.size(); // Wrap around the list
            String potentialPainter = activePlayerTokens.get(currentIndex);

            if (!history.contains(potentialPainter)) {
                nextPainterToken = potentialPainter;
                break; // Found the next painter
            }
        }

        // Fallback: Should ideally not happen if logic is correct, but safety net
        if (nextPainterToken == null) {
            log.error("Lobby {}: Could not determine next painter sequentially. Was history cleared correctly? Selecting first active player not in history.", lobbyId);
            for (String token : activePlayerTokens) {
                if (!history.contains(token)) {
                    nextPainterToken = token;
                    break;
                }
            }
            if (nextPainterToken == null && !activePlayerTokens.isEmpty()) {
                log.warn("Lobby {}: Fallback failed to find painter not in history. Selecting first active player: {}", lobbyId, activePlayerTokens.get(0));
                nextPainterToken = activePlayerTokens.get(0);
                if (!history.isEmpty()) {
                    log.warn("Lobby {}: Clearing history due to unexpected state in fallback.", lobbyId);
                    lobby.clearPainterHistory();
                }
            }
        }


        // --- Update Lobby State ---
        if (nextPainterToken != null) {
            log.info("Lobby {}: Selected next painter: {}", lobbyId, nextPainterToken);
            lobby.setCurrentPainterToken(nextPainterToken);
            lobby.addTokenToPainterHistory(nextPainterToken); // Add the new painter to history
        } else {
            log.error("Lobby {}: CRITICAL - Failed to select any painter even with fallbacks.", lobbyId);
            lobby.setCurrentPainterToken(null); // Set to null if no painter could be selected
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to determine next painter.");
        }


        // --- Save the updated Lobby ---
        lobbyRepository.save(lobby);
        lobbyRepository.flush(); // Ensure changes are persisted

        log.debug("Saved updated lobby {} with painter {}", lobbyId, lobby.getCurrentPainterToken());

        return lobby; // Return the updated lobby object
    }
}