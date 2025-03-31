package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LobbyService {

    private final Logger log = LoggerFactory.getLogger(LobbyService.class);
    private final LobbyRepository lobbyRepository;

    @Autowired
    public LobbyService(@Qualifier("lobbyRepository") LobbyRepository lobbyRepository) {
        this.lobbyRepository = lobbyRepository;
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
        
        if (newLobby.getWordset() == null) {
            newLobby.setWordset("english");
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
        
        if (lobbyUpdate.getWordset() != null) {
            existingLobby.setWordset(lobbyUpdate.getWordset());
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

    // Remove a player from a lobby  ########## NOT YET TESTED #########
    public Lobby removePlayerFromLobby(Long lobbyId, Long playerId) {
        Lobby lobby = getLobbyById(lobbyId);
        
        // Check if player is in the lobby
        if (!lobby.getPlayerIds().contains(playerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player is not in the lobby");
        }
        
        // Remove player
        lobby.removePlayerId(playerId);
        
        // Save and return updated lobby
        lobby = lobbyRepository.save(lobby);
        lobbyRepository.flush();
        
        return lobby;
    }
}