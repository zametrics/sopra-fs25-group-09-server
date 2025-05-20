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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.times;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.User;

public class LobbyServiceTest {

    @Mock
    private LobbyRepository lobbyRepository;

    @InjectMocks
    private LobbyService lobbyService;

    private Lobby testLobby;

    @Mock
    private UserService userService;

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
void createLobby_setsDefaultValuesIfNullOrZero() {
    Lobby lobby = new Lobby();
    lobby.setLobbyOwner(1L);
    lobby.setDrawTime(0); // explicitly zero
    // rest are null by default

    when(userService.getUserById(1L)).thenReturn(new User());
    when(lobbyRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    
    Lobby result = lobbyService.createLobby(lobby);

    assertEquals(8L, result.getNumOfMaxPlayers());
    assertEquals("english", result.getLanguage());
    assertEquals(3L, result.getNumOfRounds());
    assertEquals(80, result.getDrawTime());
    assertEquals("anything", result.getType());
    assertNotNull(result.getPainterHistoryTokens());
    assertTrue(result.getPainterHistoryTokens().isEmpty());
}


@Test
void updateLobby_updatesOwnerIfInPlayerList() {
    Lobby existingLobby = new Lobby();
    existingLobby.setId(1L);
    existingLobby.setLobbyOwner(1L);
    existingLobby.setPlayerIds(new ArrayList<>(List.of(1L, 2L)));

    Lobby updateDto = new Lobby();
    updateDto.setLobbyOwner(2L); // valid new owner

    when(lobbyRepository.findById(1L)).thenReturn(Optional.of(existingLobby));
    when(lobbyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    Lobby updated = lobbyService.updateLobby(1L, updateDto);

    assertEquals(2L, updated.getLobbyOwner());
}

@Test
void updateLobby_throwsWhenMaxPlayersTooHigh() {
    Lobby existing = new Lobby();
    existing.setId(1L);
    existing.setPlayerIds(List.of(1L, 2L)); // 2 players
    existing.setNumOfMaxPlayers(5L);

    Lobby updateDto = new Lobby();
    updateDto.setNumOfMaxPlayers(11L); // over limit

    when(lobbyRepository.findById(1L)).thenReturn(Optional.of(existing));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
        () -> lobbyService.updateLobby(1L, updateDto));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    assertTrue(ex.getReason().contains("cannot exceed 10"));
}


@Test
void addPlayerToLobby_throwsWhenUserNotFound() {
    Lobby lobby = new Lobby();
    lobby.setId(1L);
    lobby.setNumOfMaxPlayers(5L);
    lobby.setPlayerIds(new ArrayList<>());

    when(lobbyRepository.findById(1L)).thenReturn(Optional.of(lobby));
    when(userService.getUserById(99L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
        () -> lobbyService.addPlayerToLobby(1L, 99L));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    assertTrue(ex.getReason().contains("Player with ID 99 not found"));
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
    public void addPlayerToLobby_playerAlreadyExists_noDuplicateAdded() {
        Long lobbyId = 123456L;
        Long playerId = 2L;

        Lobby lobbyWithPlayer = new Lobby();
        lobbyWithPlayer.setId(lobbyId);
        lobbyWithPlayer.setLobbyOwner(1L);
        lobbyWithPlayer.setNumOfMaxPlayers(8L);
        lobbyWithPlayer.setPlayerIds(new ArrayList<>(Arrays.asList(1L, 2L, 3L)));
        lobbyWithPlayer.setLanguage("english");
        lobbyWithPlayer.setNumOfRounds(3L);
        lobbyWithPlayer.setDrawTime(80);

        given(lobbyRepository.findById(lobbyId)).willReturn(Optional.of(lobbyWithPlayer));

        Lobby result = lobbyService.addPlayerToLobby(lobbyId, playerId);

        assertEquals(3, result.getPlayerIds().size()); // no duplicate added
        assertTrue(result.getPlayerIds().contains(playerId));

        verify(lobbyRepository, times(1)).findById(lobbyId);
        verify(lobbyRepository, times(0)).save(Mockito.any());  // Changed to 0
        verify(lobbyRepository, times(0)).flush();              // Changed to 0
    }

    @Test
    public void addPlayerToLobby_nonExistentLobby_throwsException() {
        Long lobbyId = 9999L;
        Long playerId = 10L;

        given(lobbyRepository.findById(lobbyId)).willReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            lobbyService.addPlayerToLobby(lobbyId, playerId);
        });

        verify(lobbyRepository, times(1)).findById(lobbyId);
        verify(lobbyRepository, times(0)).save(Mockito.any());
    }

    @Test
    public void removePlayerFromLobby_nonExistentLobby_throwsException() {
        Long lobbyId = 9999L;
        Long playerId = 1L;

        given(lobbyRepository.findById(lobbyId)).willReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            lobbyService.removePlayerFromLobby(lobbyId, playerId);
        });

        verify(lobbyRepository, times(1)).findById(lobbyId);
        verify(lobbyRepository, times(0)).save(Mockito.any());
        verify(lobbyRepository, times(0)).delete(Mockito.any());
    }

    @Test
    public void updateLobby_lobbyDoesNotExist_throwsException() {
        Long lobbyId = 9999L;

        Lobby updateData = new Lobby();
        updateData.setNumOfMaxPlayers(6L);

        given(lobbyRepository.findById(lobbyId)).willReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            lobbyService.updateLobby(lobbyId, updateData);
        });

        verify(lobbyRepository, times(1)).findById(lobbyId);
        verify(lobbyRepository, times(0)).save(Mockito.any());
        verify(lobbyRepository, times(0)).flush();
    }

    @Test
    public void updateLobby_lobbyNotFound_throwsException() {
        Long nonExistingId = 999L;
        given(lobbyRepository.findById(nonExistingId)).willReturn(Optional.empty());

        Lobby updateData = new Lobby();
        updateData.setLanguage("spanish");

        assertThrows(ResponseStatusException.class, () -> {
            lobbyService.updateLobby(nonExistingId, updateData);
        });

        verify(lobbyRepository, times(1)).findById(nonExistingId);
    }

    @Test
    public void addPlayerToLobby_lobbyNotFound_throwsException() {
        Long lobbyId = 999L;
        Long playerId = 5L;

        given(lobbyRepository.findById(lobbyId)).willReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            lobbyService.addPlayerToLobby(lobbyId, playerId);
        });

        verify(lobbyRepository, times(1)).findById(lobbyId);
    }

    @Test
    public void removePlayerFromLobby_lobbyNotFound_throwsException() {
        Long lobbyId = 999L;
        Long playerId = 5L;

        given(lobbyRepository.findById(lobbyId)).willReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            lobbyService.removePlayerFromLobby(lobbyId, playerId);
        });

        verify(lobbyRepository, times(1)).findById(lobbyId);
    }

    @Test
    public void selectNextPainter_emptyLobby_throwsException() {
        Long lobbyId = 1L;
        Lobby emptyLobby = new Lobby();
        emptyLobby.setId(lobbyId);
        emptyLobby.setPlayerIds(new ArrayList<>());

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(emptyLobby));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> lobbyService.selectNextPainter(lobbyId));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        verify(lobbyRepository).save(emptyLobby);
        verify(lobbyRepository).flush();
    }

    @Test
    public void selectNextPainter_noValidTokens_throwsException() {
        Long lobbyId = 1L;
        Lobby lobby = new Lobby();
        lobby.setId(lobbyId);
        lobby.setPlayerIds(Arrays.asList(10L, 20L));

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));
        when(userService.getUserById(anyLong())).thenThrow(
            new ResponseStatusException(HttpStatus.NOT_FOUND)
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> lobbyService.selectNextPainter(lobbyId));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
        verify(lobbyRepository).save(lobby);
        verify(lobbyRepository).flush();
    }

    @Test
    public void selectNextPainter_normalRotation_success() {
        Long lobbyId = 1L;
        Lobby lobby = new Lobby();
        lobby.setId(lobbyId);
        lobby.setPlayerIds(Arrays.asList(10L, 20L, 30L));
        lobby.setPainterHistoryTokens(new HashSet<>(List.of("token1")));
        lobby.setCurrentPainterToken("token1");

        User u2 = new User(); u2.setId(20L); u2.setToken("token2");
        User u3 = new User(); u3.setId(30L); u3.setToken("token3");
        User u1 = new User(); u1.setId(10L); u1.setToken("token1");

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));
        when(userService.getUserById(10L)).thenReturn(u1);
        when(userService.getUserById(20L)).thenReturn(u2);
        when(userService.getUserById(30L)).thenReturn(u3);
        when(lobbyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Lobby result = lobbyService.selectNextPainter(lobbyId);

        assertEquals("token2", result.getCurrentPainterToken());
        assertTrue(result.getPainterHistoryTokens().contains("token2"));
        verify(lobbyRepository).save(result);
        verify(lobbyRepository).flush();
    }

    @Test
    public void setLobbyWord_validWord_setsAndSaves() {
        Long lobbyId = 1L;
        String word = "apple";
        Lobby lobby = new Lobby();
        lobby.setId(lobbyId);

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));
        when(lobbyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Lobby result = lobbyService.setLobbyWord(lobbyId, word);

        assertEquals("apple", result.getCurrentWord());
        verify(lobbyRepository).save(result);
        verify(lobbyRepository).flush();
    }

    @Test
    public void setLobbyWord_nullWord_throwsException() {
        Long lobbyId = 1L;
        Lobby lobby = new Lobby();
        lobby.setId(lobbyId);

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> lobbyService.setLobbyWord(lobbyId, null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    public void setLobbyWord_blankWord_throwsException() {
        Long lobbyId = 1L;
        Lobby lobby = new Lobby();
        lobby.setId(lobbyId);

        when(lobbyRepository.findById(lobbyId)).thenReturn(Optional.of(lobby));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> lobbyService.setLobbyWord(lobbyId, "   "));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void createLobby_ownerNull_throwsBadRequest() {
        Lobby lobby = new Lobby();
        lobby.setPlayerIds(new ArrayList<>());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> lobbyService.createLobby(lobby));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void createLobby_ownerNotFound_throwsNotFound() {
        Lobby lobby = new Lobby();
        lobby.setLobbyOwner(1L);

        when(userService.getUserById(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> lobbyService.createLobby(lobby));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }


    @Test
    void createLobby_ownerAddedToPlayerList() {
        Lobby lobby = new Lobby();
        lobby.setLobbyOwner(1L);
        lobby.setPlayerIds(new ArrayList<>());

        when(userService.getUserById(1L)).thenReturn(new User());
        when(lobbyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Lobby result = lobbyService.createLobby(lobby);
        assertTrue(result.getPlayerIds().contains(1L));
    }

    @Test
    void updateLobby_newOwnerNotInPlayers_doesNotUpdate() {
        Lobby lobby = new Lobby();
        lobby.setId(1L);
        lobby.setLobbyOwner(1L);
        lobby.setPlayerIds(List.of(1L));

        Lobby update = new Lobby();
        update.setLobbyOwner(2L); // not in playerIds

        when(lobbyRepository.findById(1L)).thenReturn(Optional.of(lobby));

        Lobby result = lobbyService.updateLobby(1L, update);
        assertEquals(1L, result.getLobbyOwner()); // should not update
    }

    @Test
    void updateLobby_maxPlayersLessThanCurrent_throwsBadRequest() {
        Lobby lobby = new Lobby();
        lobby.setId(1L);
        lobby.setPlayerIds(List.of(1L, 2L));

        Lobby update = new Lobby();
        update.setNumOfMaxPlayers(1L); // less than current size

        when(lobbyRepository.findById(1L)).thenReturn(Optional.of(lobby));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> lobbyService.updateLobby(1L, update));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }




}