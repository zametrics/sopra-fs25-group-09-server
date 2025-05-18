package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPutDTO;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class LobbyDTOMapperTest {

    @Test
    public void testConvertEntityToLobbyGetDTO_success() {
        // Given
        Lobby lobby = new Lobby();
        lobby.setId(123456L);
        lobby.setLobbyOwner(1L);
        lobby.setNumOfMaxPlayers(8L);
        lobby.setPlayerIds(Arrays.asList(1L, 2L));
        lobby.setLanguage("english");
        lobby.setNumOfRounds(3L);
        lobby.setDrawTime(80);
        lobby.setType("anything");
        lobby.setCurrentPainterToken("token1");
        lobby.setCurrentWord("apple");
        lobby.setStatus(1); // Assuming status is an integer

        // When
        LobbyGetDTO getDTO = LobbyDTOMapper.INSTANCE.convertEntityToLobbyGetDTO(lobby);

        // Then
        assertEquals(lobby.getId(), getDTO.getId());
        assertEquals(lobby.getLobbyOwner(), getDTO.getLobbyOwner());
        assertEquals(lobby.getNumOfMaxPlayers(), getDTO.getNumOfMaxPlayers());
        assertEquals(lobby.getPlayerIds(), getDTO.getPlayerIds());
        assertEquals(lobby.getLanguage(), getDTO.getLanguage());
        assertEquals(lobby.getNumOfRounds(), getDTO.getNumOfRounds());
        assertEquals(lobby.getDrawTime(), getDTO.getDrawTime());
        assertEquals(lobby.getType(), getDTO.getType());
        assertEquals(lobby.getCurrentPainterToken(), getDTO.getCurrentPainterToken());
        assertEquals(lobby.getCurrentWord(), getDTO.getCurrentWord());
    }

    @Test
    public void testConvertLobbyPutDTOtoEntity_success() {
        // Given
        LobbyPutDTO putDTO = new LobbyPutDTO();
        putDTO.setId(123456L);
        putDTO.setLobbyOwner(2L);
        putDTO.setNumOfMaxPlayers(10L);
        putDTO.setPlayerIds(Arrays.asList(1L, 2L, 3L));
        putDTO.setLanguage("german");
        putDTO.setNumOfRounds(5L);
        putDTO.setDrawTime(90);
        putDTO.setType("animals");

        // When
        Lobby lobby = LobbyDTOMapper.INSTANCE.convertLobbyPutDTOtoEntity(putDTO);

        // Then
        assertEquals(putDTO.getId(), lobby.getId());
        assertEquals(putDTO.getLobbyOwner(), lobby.getLobbyOwner());
        assertEquals(putDTO.getNumOfMaxPlayers(), lobby.getNumOfMaxPlayers());
        assertEquals(putDTO.getPlayerIds(), lobby.getPlayerIds());
        assertEquals(putDTO.getLanguage(), lobby.getLanguage());
        assertEquals(putDTO.getNumOfRounds(), lobby.getNumOfRounds());
        assertEquals(putDTO.getDrawTime(), lobby.getDrawTime());
        assertEquals(putDTO.getType(), lobby.getType());
        assertNull(lobby.getCurrentPainterToken());
    }
}