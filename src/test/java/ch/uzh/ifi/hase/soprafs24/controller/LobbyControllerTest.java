package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.LobbyDTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.LobbyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * LobbyControllerTest
 * This is a WebMvcTest which allows testing the LobbyController i.e. GET/POST/PUT
 * requests without actually sending them over the network.
 * This tests if the LobbyController works.
 */

@WebMvcTest(LobbyController.class)
public class LobbyControllerTest {

    @Autowired  //automatically injects the required dependency
    private MockMvc mockMvc;

    @MockBean
    private LobbyService lobbyService;

    // Test for GET /lobbies
    @Test
    public void givenLobbies_whenGetLobbies_thenReturnJsonArray() throws Exception {
        //given
        Lobby lobby = new Lobby();
        lobby.setId(123456L);
        lobby.setLobbyOwner(1L);
        lobby.setNumOfMaxPlayers(8L);
        lobby.setPlayerIds(Arrays.asList(1L, 2L));
        lobby.setWordset("english");
        lobby.setNumOfRounds(3L);
        lobby.setDrawTime(80);

        List<Lobby> allLobbies = Arrays.asList(lobby);
        given(lobbyService.getLobbies()).willReturn(allLobbies);

        //when
        MockHttpServletRequestBuilder getRequest = get("/lobbies").contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(lobby.getId().intValue())))
                .andExpect(jsonPath("$[0].lobbyOwner", is(lobby.getLobbyOwner().intValue())))
                .andExpect(jsonPath("$[0].numOfMaxPlayers", is(lobby.getNumOfMaxPlayers().intValue())))
                .andExpect(jsonPath("$[0].playerIds", contains(1, 2)))
                //kept getting Expected: is [<1L>, <2L>] but: was<[1,2]> error so changed to contains (also needed import for this(org.hamcrest.Matchers.contains))
                .andExpect(jsonPath("$[0].wordset", is(lobby.getWordset())))
                .andExpect(jsonPath("$[0].numOfRounds", is(lobby.getNumOfRounds().intValue())))
                .andExpect(jsonPath("$[0].drawTime", is(lobby.getDrawTime())));
    }

    // Test for POST /lobbies --> 201 Created status
    @Test
    public void createLobby_validInput_lobbyCreated() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(123456L);
        lobby.setLobbyOwner(1L);
        lobby.setNumOfMaxPlayers(8L);
        lobby.setPlayerIds(Arrays.asList(1L));
        lobby.setWordset("english");
        lobby.setNumOfRounds(3L);
        lobby.setDrawTime(80);

        LobbyPostDTO lobbyPostDTO = new LobbyPostDTO();
        lobbyPostDTO.setLobbyOwner(1L);
        lobbyPostDTO.setNumOfMaxPlayers(8L);
        lobbyPostDTO.setPlayerIds(Arrays.asList(1L));
        lobbyPostDTO.setWordset("english");
        lobbyPostDTO.setNumOfRounds(3L);
        lobbyPostDTO.setDrawTime(80);

        given(lobbyService.createLobby(Mockito.any())).willReturn(lobby);

        // when
        MockHttpServletRequestBuilder postRequest = post("/lobbies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(lobbyPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(lobby.getId().intValue())))
                .andExpect(jsonPath("$.lobbyOwner", is(lobby.getLobbyOwner().intValue())))
                .andExpect(jsonPath("$.numOfMaxPlayers", is(lobby.getNumOfMaxPlayers().intValue())))
                .andExpect(jsonPath("$.playerIds", contains(1)))
                .andExpect(jsonPath("$.wordset", is(lobby.getWordset())))
                .andExpect(jsonPath("$.numOfRounds", is(lobby.getNumOfRounds().intValue())))
                .andExpect(jsonPath("$.drawTime", is(lobby.getDrawTime())));
    }

    // Test for GET /lobbies/{lobbyId} --> 200 OK
    @Test
    public void getLobby_success() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(123456L);
        lobby.setLobbyOwner(1L);
        lobby.setNumOfMaxPlayers(8L);
        lobby.setPlayerIds(Arrays.asList(1L, 2L));
        lobby.setWordset("english");
        lobby.setNumOfRounds(3L);
        lobby.setDrawTime(80);

        given(lobbyService.getLobbyById(123456L)).willReturn(lobby);

        // when
        MockHttpServletRequestBuilder getRequest = get("/lobbies/123456")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(lobby.getId().intValue())))
                .andExpect(jsonPath("$.lobbyOwner", is(lobby.getLobbyOwner().intValue())))
                .andExpect(jsonPath("$.numOfMaxPlayers", is(lobby.getNumOfMaxPlayers().intValue())))
                .andExpect(jsonPath("$.playerIds", contains(1, 2)))
                .andExpect(jsonPath("$.wordset", is(lobby.getWordset())))
                .andExpect(jsonPath("$.numOfRounds", is(lobby.getNumOfRounds().intValue())))
                .andExpect(jsonPath("$.drawTime", is(lobby.getDrawTime())));
    }

    // Test for GET /lobbies/{lobbyId} --> 404 Not Found
    @Test
    public void getLobby_lobbyNotFound() throws Exception {
        // given
        Long lobbyId = 999L;
        given(lobbyService.getLobbyById(lobbyId))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby with ID " + lobbyId + " was not found"));

        // when
        MockHttpServletRequestBuilder getRequest = get("/lobbies/" + lobbyId)
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Lobby with ID " + lobbyId + " was not found")));
    }

    // Test for PUT /lobbies/{lobbyId} --> 204 No Content
    @Test
    public void updateLobby_validInput_success() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(123456L);
        lobby.setLobbyOwner(1L);
        lobby.setNumOfMaxPlayers(10L);
        lobby.setPlayerIds(Arrays.asList(1L, 2L, 3L));
        lobby.setWordset("english");
        lobby.setNumOfRounds(5L);
        lobby.setDrawTime(90);

        LobbyPutDTO lobbyPutDTO = new LobbyPutDTO();
        lobbyPutDTO.setId(123456L);
        lobbyPutDTO.setLobbyOwner(1L);
        lobbyPutDTO.setNumOfMaxPlayers(10L);
        lobbyPutDTO.setPlayerIds(Arrays.asList(1L, 2L, 3L));
        lobbyPutDTO.setWordset("english");
        lobbyPutDTO.setNumOfRounds(5L);
        lobbyPutDTO.setDrawTime(90);

        given(lobbyService.updateLobby(Mockito.eq(123456L), Mockito.any())).willReturn(lobby);

        // when
        MockHttpServletRequestBuilder putRequest = put("/lobbies/123456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(lobbyPutDTO));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());
    }

    // Test for PUT /lobbies/{lobbyId}/join --> 200 OK
    @Test
    public void joinLobby_validInput_success() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(123456L);
        lobby.setLobbyOwner(1L);
        lobby.setNumOfMaxPlayers(8L);
        lobby.setPlayerIds(Arrays.asList(1L, 2L, 3L));
        lobby.setWordset("english");
        lobby.setNumOfRounds(3L);
        lobby.setDrawTime(80);

        given(lobbyService.addPlayerToLobby(123456L, 3L)).willReturn(lobby);

        // when
        MockHttpServletRequestBuilder putRequest = put("/lobbies/123456/join")
                .param("playerId", "3")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(lobby.getId().intValue())))
                .andExpect(jsonPath("$.playerIds", contains(1, 2, 3)))
                .andExpect(jsonPath("$.playerIds", hasSize(3)));
    }

    // Test for PUT /lobbies/{lobbyId}/join --> 400 Bad Request (lobby full)
    @Test
    public void joinLobby_lobbyFull_error() throws Exception {
        // given
        Long lobbyId = 123456L;
        Long playerId = 3L;
        given(lobbyService.addPlayerToLobby(lobbyId, playerId))
                .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lobby is full"));

        // when
        MockHttpServletRequestBuilder putRequest = put("/lobbies/" + lobbyId + "/join")
                .param("playerId", playerId.toString())
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Lobby is full")));
    }

    // Test for PUT /lobbies/{lobbyId}/leave --> 200 OK
    @Test
    public void leaveLobby_validInput_success() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(123456L);
        lobby.setLobbyOwner(1L);
        lobby.setNumOfMaxPlayers(8L);
        lobby.setPlayerIds(Arrays.asList(1L));
        lobby.setWordset("english");
        lobby.setNumOfRounds(3L);
        lobby.setDrawTime(80);

        given(lobbyService.removePlayerFromLobby(123456L, 2L)).willReturn(lobby);

        // when
        MockHttpServletRequestBuilder putRequest = put("/lobbies/123456/leave")
                .param("playerId", "2")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(lobby.getId().intValue())))
                .andExpect(jsonPath("$.playerIds", contains(1)))
                .andExpect(jsonPath("$.playerIds", hasSize(1)));
    }




/**
 * Helper Method to convert DTOs into a JSON string such that the input can be processed
 * Input will look like: {"lobbyOwner": 1, "numOfMaxPlayers": 8, ...}
 *
 * @param object
 * @return string
 */
private String asJsonString(final Object object) {
    try {
        return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                String.format("The request body could not be created.%s", e.toString()));
    }
}
}