package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.LobbyDTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.LobbyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class LobbyController {

    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @GetMapping("/lobbies")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<LobbyGetDTO> getAllLobbies() {
        // Fetch all lobbies
        List<Lobby> lobbies = lobbyService.getLobbies();
        List<LobbyGetDTO> lobbyGetDTOs = new ArrayList<>();

        // Convert each entity to a DTO
        for (Lobby lobby : lobbies) {
            lobbyGetDTOs.add(LobbyDTOMapper.INSTANCE.convertEntityToLobbyGetDTO(lobby));
        }
        return lobbyGetDTOs;
    }

    @PostMapping("/lobbies")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public LobbyGetDTO createLobby(@RequestBody LobbyPostDTO lobbyPostDTO) {
        // Convert API input to entity
        Lobby lobbyInput = LobbyDTOMapper.INSTANCE.convertLobbyPostDTOtoEntity(lobbyPostDTO);

        // Create lobby
        Lobby createdLobby = lobbyService.createLobby(lobbyInput);

        // Convert entity to API output
        return LobbyDTOMapper.INSTANCE.convertEntityToLobbyGetDTO(createdLobby);
    }

    @GetMapping("/lobbies/{lobbyId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public LobbyGetDTO getLobby(@PathVariable("lobbyId") Long lobbyId) {
        Lobby lobby = lobbyService.getLobbyById(lobbyId);
        return LobbyDTOMapper.INSTANCE.convertEntityToLobbyGetDTO(lobby);
    }

    @PutMapping("/lobbies/{lobbyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void updateLobby(@PathVariable("lobbyId") Long lobbyId, @RequestBody LobbyPutDTO lobbyPutDTO) {
        Lobby lobbyInput = LobbyDTOMapper.INSTANCE.convertLobbyPutDTOtoEntity(lobbyPutDTO);
        lobbyService.updateLobby(lobbyId, lobbyInput);
    }

    @PutMapping("/lobbies/{lobbyId}/join")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public LobbyGetDTO joinLobby(@PathVariable("lobbyId") Long lobbyId, @RequestParam Long playerId) {
        Lobby updatedLobby = lobbyService.addPlayerToLobby(lobbyId, playerId);
        return LobbyDTOMapper.INSTANCE.convertEntityToLobbyGetDTO(updatedLobby);
    }

    @PutMapping("/lobbies/{lobbyId}/leave")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public LobbyGetDTO leaveLobby(@PathVariable("lobbyId") Long lobbyId, @RequestParam Long playerId) {
        Lobby updatedLobby = lobbyService.removePlayerFromLobby(lobbyId, playerId);
        
        // If the lobby is empty (no more players), it will be deleted
        // We can still return the DTO for the client to know the state before deletion
        return LobbyDTOMapper.INSTANCE.convertEntityToLobbyGetDTO(updatedLobby);
    }

    @PostMapping("/lobbies/{lobbyId}/nextPainter")
    @ResponseStatus(HttpStatus.OK) // OK because we return the updated state
    @ResponseBody
    public LobbyGetDTO selectNextPainter(@PathVariable("lobbyId") Long lobbyId) {
        Lobby updatedLobby = lobbyService.selectNextPainter(lobbyId);

        // Convert the updated entity to DTO and return it
        return LobbyDTOMapper.INSTANCE.convertEntityToLobbyGetDTO(updatedLobby);
    }


    @PutMapping("/lobbies/{lobbyId}/word")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public LobbyGetDTO setLobbyWord(@PathVariable("lobbyId") Long lobbyId, @RequestBody String word) {
        Lobby updatedLobby = lobbyService.setLobbyWord(lobbyId, word);
        return LobbyDTOMapper.INSTANCE.convertEntityToLobbyGetDTO(updatedLobby);
        
    }

    @GetMapping("/lobbies/{lobbyId}/word")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String getLobbyWord(@PathVariable("lobbyId") Long lobbyId) {
        Lobby lobby = lobbyService.getLobbyById(lobbyId);
        String currentWord = lobby.getCurrentWord();
        return currentWord;
    }
}