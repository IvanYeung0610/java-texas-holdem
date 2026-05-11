# Java Texas Hold'em

Texas hold’em is a popular variant of poker. The basic premise of the game consists of each player getting two cards at the beginning of a round and the dealer spreading out five cards that can be used by all players. The object of this game is to have the best possible combination of five cards using any combination of the seven cards available (the two cards received at the beginning along with the five cards spread by the dealer). The dealer will spread these five cards out over the course of three phases. Before and in between these phases each player will have a turn to perform an action (fold, call, check, or raise) to leave the game, stay in the game, or raise the amount being bet for that round. Once all five cards have been shown, the person with the best hand gets all the money that was bet. If everyone folded except a single person, that remaining person gets all the money.

This project is a multiplayer Texas Hold'em game written in Java. It uses a client-server architecture where one machine runs the game server and multiple clients connect to play through a Swing-based graphical interface.

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
6. Start the game after all players have connected.

If running on terminal:

1. Go into the project directory `cd java-texas-holdem`
2. Compile the java files `javac src/*.java`
3. Go into src folder `cd src/`
3. Run the server `java server.Server`
4. For each client open a terminal and run `java client.Client`
5. In each client window, enter:
   - player name
   - server host
   - port `3000` unless you changed it in `Server.java`
6. Start the game after all players have connected.

**Default Settings**

The default values are currently hardcoded in Server.java:

- Port: `3000`
- Number of players: `3`
- Starting balance: `1000`

These can be changed in the `main` method of `server.Server`.

**Gameplay Features**

- Multiplayer Texas Hold'em
- Dealer rotation with blinds
- Pre-flop, flop, turn, river, and showdown phases
- Folding, checking, calling, and raising
- Pot tracking
- Hand comparison and winner selection
- Client display for community cards, player hand, turn tracking, and hand history

**Poker Hand Scoring Algorithm**

The poker hand scoring logic is implemented in HandEvaluator.java. The evaluator uses the following algorithm to find the best hand that a player has from their two hole cards and the five community cards by producing a numeric score for the hand:

1. Generate all possible 5-card combinations from the 7 available cards.
   There are 21 total combinations, and the evaluator checks every one of them.

2. Score each 5-card hand individually.
   For each 5-card combination, the algorithm:
   - counts the frequency of each rank
   - checks whether all suits match to detect a flush
   - checks whether the ranks form a straight, including the special low-Ace straight `A-2-3-4-5`
   - detects grouped patterns such as pairs, three of a kind, and four of a kind

3. Assign a hand category.
   The evaluator checks categories from strongest to weakest:
   - straight flush
   - four of a kind
   - full house
   - flush
   - straight
   - three of a kind
   - two pair
   - one pair
   - high card

4. Build a numeric score with tie-breakers included.
   The code creates one integer score where:
   - the highest part stores the hand category
   - the remaining parts store tie-breaker ranks such as pair rank, trip rank, or kicker cards
   - this number is generated bit shifting the score of each section according to it's importance (the category shifted to the left the most) and ORing the different numbers to produce a singular score

5. Keep the highest score out of all 21 combinations.
   The best numeric score found becomes that player's final hand score. The game then compares player scores to determine the winner at showdown.

**Advanced Topics Used**

This project uses the following three advanced course topics:

1. **Networking with sockets**
   The game uses Java sockets so players on different machines can connect to the same server. The server listens for incoming client connections. The sockets were used to send game information to the clients with the Serializable GameState class. The actions performed by the clients were communicated as text through the input and output streams of the socket to the server. Each action was tied to a specific string (FOLD, CALL, CHECK, RAISE) which were sent with PrintWriter and read with BufferedReader.

2. **Multithreading**
   Each connected player is handled in its own thread through a HandleClient class. This allows the server to manage multiple clients at the same time while still listening for player actions independently.

3. **Graphical user interface with Swing**
   The client is implemented as a desktop GUI using Swing. It includes buttons for actions, text fields for raises, custom card rendering, scrollable status panels, and dynamic turn/action updates.


**How The Advanced Topics Were Used**

- **Sockets** were used to allow remote multiplayer play rather than a local-only game.
- **Threads** were used for the server to communicate with multiple clients to enforce the expected flow and logic of texas holdem.
- **Swing** was used to create a playable user interface. Players are able to see a graphical render of the cards and use buttons to perform actions in the game.

**Notes**

- The current server setup expects exactly players before starting.
- The port that the server runs on, the number of players in the game, and the starting balance for each player can be changed
by changing the line in main() of Server.java that initializes the Server object:
- The small and big blinds are hard coded to 10 and 20 respectively.
```
	Server server = new Server(3000, 3, 1000);
```
