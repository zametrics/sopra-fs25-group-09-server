package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LobbyServiceTest {

    @Mock
    private LobbyRepository lobbyRepository;

    @InjectMocks
    private LobbyService lobbyService;

    private Lobby testLobby;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Create a test lobby with all required fields
        testLobby = new Lobby();
        testLobby.setId(123456L);
        testLobby.setLobbyOwner(1L);
        testLobby.setNumOfMaxPlayers(8L);
        testLobby.setPlayerIds(Arrays.asList(1L, 2L));
        testLobby.setLanguage("english");
        testLobby.setNumOfRounds(3L);
        testLobby.setDrawTime(80);
        testLobby.setType("anything");

        // When saving any lobby, return the test lobby
        Mockito.when(lobbyRepository.save(Mockito.any())).thenReturn(testLobby);
    }

    @Test
    public void getLobbies_returnsAllLobbies() {
        // given
        List<Lobby> allLobbies = Arrays.asList(testLobby);
        given(lobbyRepository.findAll()).willReturn(allLobbies);

        // when
        List<Lobby> result = lobbyService.getLobbies();

        // then
        assertEquals(1, result.size());
        assertEquals(testLobby.getId(), result.get(0).getId());
        verify(lobbyRepository, times(1)).findAll();
    }

    @Test
    public void createLobby_withDefaultValues_success() {
        // given
        Lobby inputLobby = new Lobby();
        inputLobby.setLobbyOwner(1L);
        // Note: not setting other fields to test defaults

        Lobby savedLobby = new Lobby();
        savedLobby.setId(123456L);
        savedLobby.setLobbyOwner(1L);
        savedLobby.setNumOfMaxPlayers(8L);
        savedLobby.setLanguage("english");
        savedLobby.setNumOfRounds(3L);
        savedLobby.setDrawTime(80);
        savedLobby.setType("anything");
        savedLobby.setPlayerIds(new ArrayList<>());

        given(lobbyRepository.save(Mockito.any())).willReturn(savedLobby);

        // when
        Lobby createdLobby = lobbyService.createLobby(inputLobby);

        // then
        assertEquals(8L, createdLobby.getNumOfMaxPlayers());
        assertEquals("english", createdLobby.getLanguage());
        assertEquals(3L, createdLobby.getNumOfRounds());
        assertEquals(80, createdLobby.getDrawTime());
        assertEquals("anything", createdLobby.getType());
        assertNotNull(createdLobby.getPlayerIds());

        verify(lobbyRepository, times(1)).save(Mockito.any());
        verify(lobbyRepository, times(1)).flush();
    }

    @Test
    public void createLobby_withoutLobbyOwner_throwsException() {
        // given
        Lobby inputLobby = new Lobby();
        // Not setting lobbyOwner to test the validation

        // when/then
        assertThrows(ResponseStatusException.class, () -> {
            lobbyService.createLobby(inputLobby);
        });
        
        // Verify repository was never called
        verify(lobbyRepository, times(0)).save(any());
        verify(lobbyRepository, times(0)).flush();
    }

    @Test
    public void getLobbyById_existingId_returnsLobby() {
        // given
        given(lobbyRepository.findById(testLobby.getId())).willReturn(Optional.of(testLobby));

        // when
        Lobby found = lobbyService.getLobbyById(testLobby.getId());

        // then
        assertNotNull(found);
        assertEquals(testLobby.getId(), found.getId());
        assertEquals(testLobby.getLobbyOwner(), found.getLobbyOwner());
        verify(lobbyRepository, times(1)).findById(testLobby.getId());
    }

    @Test
    public void getLobbyById_nonExistingId_throwsException() {
        // given
        Long nonExistingId = 999L;
        given(lobbyRepository.findById(nonExistingId)).willReturn(Optional.empty());

        // when/then
        assertThrows(ResponseStatusException.class, () -> {
            lobbyService.getLobbyById(nonExistingId);
        });
        
        verify(lobbyRepository, times(1)).findById(nonExistingId);
    }

    @Test
    public void updateLobby_validUpdate_success() {
        // given
        Lobby updateData = new Lobby();
        updateData.setNumOfMaxPlayers(10L);
        updateData.setLanguage("german");
        updateData.setNumOfRounds(5L);
        updateData.setDrawTime(60);
        updateData.setType("animals");

        Lobby existingLobby = new Lobby();
        existingLobby.setId(123456L);
        existingLobby.setLobbyOwner(1L);
        existingLobby.setNumOfMaxPlayers(8L); // Will be updated
        existingLobby.setLanguage("english"); // Will be updated
        existingLobby.setNumOfRounds(3L);     // Will be updated
        existingLobby.setDrawTime(80);        // Will be updated
        existingLobby.setType("anything");    // Will be updated
        existingLobby.setPlayerIds(Arrays.asList(1L, 2L));

        Lobby updatedLobby = new Lobby();
        updatedLobby.setId(123456L);
        updatedLobby.setLobbyOwner(1L);
        updatedLobby.setNumOfMaxPlayers(10L);   // Updated
        updatedLobby.setLanguage("german");     // Updated
        updatedLobby.setNumOfRounds(5L);        // Updated
        updatedLobby.setDrawTime(60);           // Updated
        updatedLobby.setType("animals");        // Updated
        updatedLobby.setPlayerIds(Arrays.asList(1L, 2L));

        given(lobbyRepository.findById(123456L)).willReturn(Optional.of(existingLobby));
        given(lobbyRepository.save(Mockito.any())).willReturn(updatedLobby);

        // when
        Lobby result = lobbyService.updateLobby(123456L, updateData);

        // then
        assertEquals(10L, result.getNumOfMaxPlayers());
        assertEquals("german", result.getLanguage());
        assertEquals(5L, result.getNumOfRounds());
        assertEquals(60, result.getDrawTime());
        assertEquals("animals", result.getType());
        assertEquals(2, result.getPlayerIds().size());
        
        verify(lobbyRepository, times(1)).findById(123456L);
        verify(lobbyRepository, times(1)).save(Mockito.any());
        verify(lobbyRepository, times(1)).flush();
    }

    @Test
    public void addPlayerToLobby_success() {
        // given
        Long lobbyId = 123456L;
        Long playerId = 3L; // New player
        
        Lobby existingLobby = new Lobby();
        existingLobby.setId(lobbyId);
        existingLobby.setLobbyOwner(1L);
        existingLobby.setNumOfMaxPlayers(8L);
        existingLobby.setPlayerIds(Arrays.asList(1L, 2L)); // Already has 2 players
        existingLobby.setLanguage("english");
        existingLobby.setNumOfRounds(3L);
        existingLobby.setDrawTime(80);
        
        Lobby updatedLobby = new Lobby();
        updatedLobby.setId(lobbyId);
        updatedLobby.setLobbyOwner(1L);
        updatedLobby.setNumOfMaxPlayers(8L);
        updatedLobby.setPlayerIds(Arrays.asList(1L, 2L, 3L)); // Now has 3 players
        updatedLobby.setLanguage("english");
        updatedLobby.setNumOfRounds(3L);
        updatedLobby.setDrawTime(80);
        
        given(lobbyRepository.findById(lobbyId)).willReturn(Optional.of(existingLobby));
        given(lobbyRepository.save(Mockito.any())).willReturn(updatedLobby);
        
        // when
        Lobby result = lobbyService.addPlayerToLobby(lobbyId, playerId);
        
        // then
        assertEquals(3, result.getPlayerIds().size());
        assertTrue(result.getPlayerIds().contains(playerId));
        
        verify(lobbyRepository, times(1)).findById(lobbyId);
        verify(lobbyRepository, times(1)).save(Mockito.any());
        verify(lobbyRepository, times(1)).flush();
    }
    
    @Test
    public void addPlayerToLobby_alreadyInLobby_throwsException() {
        // given
        Long lobbyId = 123456L;
        Long playerId = 2L; // Player already in lobby
        
        Lobby existingLobby = new Lobby();
        existingLobby.setId(lobbyId);
        existingLobby.setLobbyOwner(1L);
        existingLobby.setNumOfMaxPlayers(8L);
        existingLobby.setPlayerIds(Arrays.asList(1L, 2L)); // Player 2 already exists
        existingLobby.setLanguage("english");
        existingLobby.setNumOfRounds(3L);
        existingLobby.setDrawTime(80);
        
        given(lobbyRepository.findById(lobbyId)).willReturn(Optional.of(existingLobby));
        
        // when/then
        assertThrows(ResponseStatusException.class, () -> {
            lobbyService.addPlayerToLobby(lobbyId, playerId);
        });
        
        verify(lobbyRepository, times(1)).findById(lobbyId);
        verify(lobbyRepository, times(0)).save(Mockito.any());
    }
    
    @Test
    public void addPlayerToLobby_lobbyFull_throwsException() {
        // given
        Long lobbyId = 123456L;
        Long playerId = 9L; // New player
        
        Lobby fullLobby = new Lobby();
        fullLobby.setId(lobbyId);
        fullLobby.setLobbyOwner(1L);
        fullLobby.setNumOfMaxPlayers(8L);
        // Fill the lobby with 8 players
        fullLobby.setPlayerIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));
        fullLobby.setLanguage("english");
        fullLobby.setNumOfRounds(3L);
        fullLobby.setDrawTime(80);
        
        given(lobbyRepository.findById(lobbyId)).willReturn(Optional.of(fullLobby));
        
        // when/then
        assertThrows(ResponseStatusException.class, () -> {
            lobbyService.addPlayerToLobby(lobbyId, playerId);
        });
        
        verify(lobbyRepository, times(1)).findById(lobbyId);
        verify(lobbyRepository, times(0)).save(Mockito.any());
    }
    
    @Test
    public void removePlayerFromLobby_success() {
        // given
        Long lobbyId = 123456L;
        Long playerId = 2L; // Player to remove
        
        Lobby existingLobby = new Lobby();
        existingLobby.setId(lobbyId);
        existingLobby.setLobbyOwner(1L);
        existingLobby.setNumOfMaxPlayers(8L);
        existingLobby.setPlayerIds(Arrays.asList(1L, 2L, 3L)); // Has 3 players
        existingLobby.setLanguage("english");
        existingLobby.setNumOfRounds(3L);
        existingLobby.setDrawTime(80);
        
        Lobby updatedLobby = new Lobby();
        updatedLobby.setId(lobbyId);
        updatedLobby.setLobbyOwner(1L);
        updatedLobby.setNumOfMaxPlayers(8L);
        updatedLobby.setPlayerIds(Arrays.asList(1L, 3L)); // Player 2 removed
        updatedLobby.setLanguage("english");
        updatedLobby.setNumOfRounds(3L);
        updatedLobby.setDrawTime(80);
        
        given(lobbyRepository.findById(lobbyId)).willReturn(Optional.of(existingLobby));
        given(lobbyRepository.save(Mockito.any())).willReturn(updatedLobby);
        
        // when
        Lobby result = lobbyService.removePlayerFromLobby(lobbyId, playerId);
        
        // then
        assertEquals(2, result.getPlayerIds().size());
        assertFalse(result.getPlayerIds().contains(playerId));
        
        verify(lobbyRepository, times(1)).findById(lobbyId);
        verify(lobbyRepository, times(1)).save(Mockito.any());
        verify(lobbyRepository, times(1)).flush();
    }
    
    @Test
    public void removePlayerFromLobby_lastPlayer_deletesLobby() {
        // given
        Long lobbyId = 123456L;
        Long playerId = 1L; // Last player
        
        Lobby existingLobby = new Lobby();
        existingLobby.setId(lobbyId);
        existingLobby.setLobbyOwner(1L);
        existingLobby.setNumOfMaxPlayers(8L);
        existingLobby.setPlayerIds(Arrays.asList(1L)); // Only has 1 player
        existingLobby.setLanguage("english");
        existingLobby.setNumOfRounds(3L);
        existingLobby.setDrawTime(80);
        
        Lobby emptyLobby = new Lobby();
        emptyLobby.setId(lobbyId);
        emptyLobby.setLobbyOwner(1L);
        emptyLobby.setNumOfMaxPlayers(8L);
        emptyLobby.setPlayerIds(new ArrayList<>()); // No players left
        emptyLobby.setLanguage("english");
        emptyLobby.setNumOfRounds(3L);
        emptyLobby.setDrawTime(80);
        
        given(lobbyRepository.findById(lobbyId)).willReturn(Optional.of(existingLobby));
        
        // when
        Lobby result = lobbyService.removePlayerFromLobby(lobbyId, playerId);
        
        // then
        assertTrue(result.getPlayerIds().isEmpty());
        
        verify(lobbyRepository, times(1)).findById(lobbyId);
        verify(lobbyRepository, times(1)).delete(Mockito.any());
        verify(lobbyRepository, times(0)).save(Mockito.any()); // Should not save when deleting
    }
    
    @Test
    public void removePlayerFromLobby_playerNotInLobby_throwsException() {
        // given
        Long lobbyId = 123456L;
        Long playerId = 5L; // Player not in lobby
        
        Lobby existingLobby = new Lobby();
        existingLobby.setId(lobbyId);
        existingLobby.setLobbyOwner(1L);
        existingLobby.setNumOfMaxPlayers(8L);
        existingLobby.setPlayerIds(Arrays.asList(1L, 2L, 3L)); // Player 5 not here
        existingLobby.setLanguage("english");
        existingLobby.setNumOfRounds(3L);
        existingLobby.setDrawTime(80);
        
        given(lobbyRepository.findById(lobbyId)).willReturn(Optional.of(existingLobby));
        
        // when/then
        assertThrows(ResponseStatusException.class, () -> {
            lobbyService.removePlayerFromLobby(lobbyId, playerId);
        });
        
        verify(lobbyRepository, times(1)).findById(lobbyId);
        verify(lobbyRepository, times(0)).save(Mockito.any());
        verify(lobbyRepository, times(0)).delete(Mockito.any());
    }
}