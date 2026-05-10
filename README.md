# Java Texas Hold'em

This project is a networked multiplayer Texas Hold'em game written in Java. It uses a client-server architecture where one machine runs the game server and multiple clients connect to play through a Swing-based graphical interface.

**Requirements**

- Java 17 or newer installed on all machines
- Eclipse or another Java IDE, or the Java command line tools
- Machines must be able to reach the server over the network

**Project Structure**

- `src/server`: server-side networking and game control
- `src/client`: Swing client UI and player interaction
- `src/shared`: shared model classes such as `Player`, `GameState`, `Card`, `Deck`, and hand evaluation logic

**How To Run**

If using Eclipse:

1. Import the project with `File -> Import -> Existing Projects into Workspace`.
2. Select the project folder containing `.project`, `.classpath`, and `src/`.
3. Run `server.Server` first.
4. Run `client.Client` once for each player.
5. In each client window, enter:
   - player name
   - server host
   - port `3000` unless you changed it in `Server.java`
6. Start the game after both players have connected.

**Default Settings**

The default values are currently hardcoded in [Server.java](/home/iyeung/Documents/Projects/java-texas-holdem/src/server/Server.java):

- Port: `3000`
- Number of players: `2`
- Starting balance: `1000`

These can be changed in the `main` method of `server.Server`.

**Gameplay Features**

- Two-player Texas Hold'em
- Dealer rotation with blinds
- Pre-flop, flop, turn, river, and showdown phases
- Folding, checking, calling, and raising
- Pot tracking
- Hand comparison and winner selection
- Client display for community cards, player hand, turn tracking, and hand history

**Advanced Topics Used**

This project uses more than three advanced course topics:

1. **Networking with sockets**
   The game uses Java sockets so players on different machines can connect to the same server. The server listens for incoming client connections in `server.Server`, and each client connects through `client.Client`.

2. **Multithreading**
   Each connected player is handled in its own thread through `HandleClient implements Runnable`. This allows the server to manage multiple clients at the same time while still listening for player actions independently.

3. **Graphical user interface with Swing**
   The client is implemented as a desktop GUI using Swing. It includes buttons for actions, text fields for raises, custom card rendering, scrollable status panels, and dynamic turn/action updates.


**How The Advanced Topics Were Used**

- **Sockets** were used to allow remote multiplayer play rather than a local-only game.
- **Threads** were used so one client waiting for input would not freeze the rest of the server.
- **Serialization** was used to keep client updates simple and consistent by sending a full `GameState`.
- **Swing** was used to create a playable user interface instead of a console-only program.

**Notes**

- The current server setup expects exactly two players before starting.
- If you move the project to another machine, make sure hidden files like `.project` and `.classpath` are copied too if you plan to use Eclipse.
- If clients connect from another machine, use the server machine's IP address instead of `localhost`.
