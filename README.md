# 🎨 DrawZone - Art Battle Royale - Server

## 🧭 Introduction

DrawZone is a real-time multiplayer drawing and guessing game (inspired by Pictionary and the drawing rounds of Activity). One player draws a given word, and other players try to guess that word based on the drawing. This repository contains the server-side implementation of DrawZone, which powers the game logic, real-time communication, and word generation. The goal of the project is to provide a fun social party game experience that encourages creativity and interaction among players. The server manages game sessions (lobbies, rounds, scoring) and communicates with the DrawZone client frontend to deliver a seamless real-time multiplayer experience.

---

## 🛠️ Technologies Used

- **Spring Boot** – Server-side framework for building the web application and WebSocket (Socket.IO) integration.
- **Java** – Primary programming language used to implement the backend logic (e.g., game management, networking).
- **H2 Database (in-memory)** – Lightweight, in-memory SQL database to store game state (lobbies, players, scores) during runtime. No external DB setup is required, as data is stored transiently in memory.
- **OpenAI API** – Used to generate or fetch random word prompts for the drawing rounds. The server calls OpenAI’s API to get words that players will have to draw and guess.


---

### 🧩 High-Level Components

The backend is structured into several key components, each responsible for a core aspect of the game. The major components and their roles are:

- **Lobby Management** ([`Lobby.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/entity/Lobby.java) and related classes)  
  Handles creation of game lobbies and management of players in each lobby. This component allows players to create or join a lobby (game room), keeps track of the lobby state (players ready, host, etc.), and prepares the lobby for starting a game. It interacts with the game logic component to initiate a new game when the lobby is full or the host starts the round.

- **Game Logic Service** ([`GameService.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/GameService.java))  
  Orchestrates the main game flow and rules. This service manages the progression of rounds and turns within a lobby. It selects a word for the drawing round (via the OpenAI API integration), assigns the drawing player, starts the timer for a round, and keeps track of guesses and scores. It updates player scores when someone guesses correctly and determines when to end a round or game. The GameService interacts with the lobby data and uses the communication component to broadcast game updates (e.g. new round starting, correct guess notifications).

- **Real-Time Communication** (Socket.IO/WebSocket controller, e.g. [`GameSocketController.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/websocket/GameSocketController.java))  
  Facilitates instant updates between server and clients. This component uses Socket.IO (WebSocket under the hood) to broadcast real-time events such as drawing strokes, chat messages, and guess results to all players in a lobby. For example, as a player draws, the drawing data is sent through the socket and relayed by the server to other players’ clients in real time. It interacts closely with the GameService to notify clients of events like correct guesses or round transitions.

- **Word Generation (OpenAI Integration)** ([`WordService.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/service/WordService.java) or similar)  
  Encapsulates interactions with the OpenAI API to retrieve random words or terms for players to draw. When a new round begins, this component calls OpenAI’s API to fetch a word prompt (optionally based on difficulty or category). The word is then given to the drawing player and stored as the current target word. Includes fallback logic for API failures if implemented.

- **Data Models & Persistence** (e.g., [`Player.java`](src/main/java/ch/uzh/ifi/hase/soprafs24/entity/Player.java), `Lobby`, `Round`, and repositories)  
  Represents game state with model classes and persists it using an in-memory H2 database. These models track players, lobbies, and scores. Spring Data JPA repositories like `LobbyRepository` and `PlayerRepository` allow adding/removing players, querying current state, and updating scores. Data is transient and resets on server restart, making setup simple and fast for development.

These components work together closely. For example:
- When a player joins (handled by Lobby management), the server notifies others via the communication controller.
- When a game starts, the `GameService` uses the `WordService` to get a word and starts the round using Socket.IO.




---


## 🚀 Launch & Deployment

## Getting Started

To get started with the project, a new developer should follow these steps:

1. **Clone the Repositories**
   Clone both the frontend and backend repositories to your local machine using:

   ```bash
   git clone https://github.com/zametrics/sopra-fs25-group-09-client
   git clone https://github.com/zametrics/sopra-fs25-group-09-server
   ```

2. **Frontend Setup**
   Navigate to the frontend directory and build the project:

   ```bash
   npm run build
   ```

   After building, the application will be available by default at [http://localhost:3000](http://localhost:3000).



3. **Configure OpenAI API Key**: The server requires an OpenAI API key to fetch word prompts. Provide your OpenAI API key before running the server:
   **Option 1:** Set an environment variable OPENAI_API_KEY with your key (this is a common method Spring Boot can use to pick up the key at runtime).

**Option 2:** Alternatively, open the configuration file (e.g. application.properties or a dedicated OpenAI service class) and insert your API key in the designated place (for example, a field or property like openai.api.key=).
Note: Without a valid API key, the word generation feature will not work and game rounds may not start properly.

3. **Backend Setup**
   Navigate to the backend directory and execute the following commands:

    * To build the project:

      ```bash
      ./gradlew build
      ```

    * To run the application:

      ```bash
      ./gradlew bootRun
      ```

    * To execute tests:

      ```bash
      ./gradlew test
      ```

   The backend service will be available at [http://localhost:8080](http://localhost:8080).

## Deployment & Releases

### Docker Deployment to Google Cloud Run

After making changes in your client or socket server repository, follow these steps:

#### Rebuild the Docker Image

```bash
sudo docker build -t gcr.io/my-socket-server-456017/socket-server .
```

#### Push to Google Container Registry

Tag the image explicitly (recommended for versioning):

```bash
TAG=v1.0.0  # or use: TAG=$(git rev-parse --short HEAD)
docker build -t gcr.io/my-socket-server-456017/socket-server:$TAG .
```

Push with the tag:

```bash
docker push gcr.io/my-socket-server-456017/socket-server:$TAG
```

Deploy with the same tag:

```bash
gcloud run deploy socket-server \
  --image gcr.io/my-socket-server-456017/socket-server:$TAG \
  --platform managed \
  --region europe-west1 \
  --port 8080 \
  --allow-unauthenticated
```



---

## 🖼️ Illustration

Although this repository doesn’t have a user interface, it **powers the real-time gameplay** and maintains the **game state** for DrawZone. The server works in tandem with the frontend (see the DrawZone client’s README for UI details) to deliver an interactive drawing & guessing experience.

### ⚡ Real-Time Game Flow

The backend uses **Socket.IO-based WebSockets** to handle real-time events:

- When a player is drawing, their strokes are emitted from the client and relayed by the server to all other players in the lobby.
- When a player submits a guess, the server checks it against the secret word and broadcasts feedback (e.g., "correct!" or "you’re close") instantly.

This real-time messaging keeps the game interactive and synchronous for all players.

---

### 🧠 Stateful Lobbies and Rounds

The server maintains the full **game state in memory**:

- Player list
- Current round and turn
- Word to be guessed
- Scoreboard

State is preserved during the session using the H2 in-memory database. This allows all players in a lobby to share the same game context. The server handles round transitions, score updates, and word assignment.

---

### 🔗 Integration with Frontend

The frontend communicates with the backend via:

- **HTTP Requests** for setup actions (e.g., creating a lobby)
- **Socket.IO events** for real-time gameplay (e.g., drawing, guessing, chat)

The backend is the **authoritative game engine**, enforcing all rules (e.g., guessing time limits) and broadcasting status updates (e.g., round over, correct guess).

> For more on the UI and game visuals, refer to the [DrawZone client repository](https://github.com/zametrics/sopra-fs25-group-09-client).

---

This **server-centric architecture** ensures that players remain in sync across all devices and networks. The **Socket.IO integration** provides fast and reliable communication, enabling a smooth multiplayer experience.

## 🗺️ Roadmap

Possible enhancements and extensions for the DrawZone backend include:

- **🗃️ Persistent Storage**  
  Currently, all game data is kept in memory and resets on server restart. Introducing a persistent database (e.g., PostgreSQL or another SQL/NoSQL DB) would allow storing:
    - Persistent user profiles
    - Game history
    - High scores  
      This would enable features like reconnecting to ongoing games or tracking long-term stats across sessions.

- **🛡️ Improved Error Handling & Validation**  
  Make the server more robust by:
    - Validating client inputs (e.g., invalid draw data or spammy guesses)
    - Adding synchronization checks (e.g., for network lag or disconnections)
    - Returning standardized error responses (e.g., JSON error objects or codes)  
      This would improve both stability and developer experience.

- **📊 Admin & Analytics APIs**  
  Add endpoints or tools for monitoring and analytics:
    - List active lobbies and player counts
    - Forcibly terminate or moderate games
    - Gather metrics (e.g., most guessed words, average round time)  
      This could help with debugging and improving game balancing.

- **🔒 Additional Features (Future)**  
  Ideas for larger-scale upgrades:
    - Add spectator mode
    - Implement custom word types
    - Add vote kick system

These improvements are not required to run DrawZone but can help scale and polish the backend over time.


## 🙌 Authors and Acknowledgment
> Team members and any third-party tools or inspirations you want to mention.

Daniel Toth [@Danino42](https://github.com/Danino42)  
Ilias Woert [@iliasw15](https://github.com/iliasw15)  
Nikola Petrovic [@ortakyakuza](https://github.com/ortakyakuza)  
Richard Uk [@zametrics](https://github.com/zametrics)

Huge thank you to our TA Lucas Timothy Leo Bär

---

## 🛡️ License
The project is under MIT License