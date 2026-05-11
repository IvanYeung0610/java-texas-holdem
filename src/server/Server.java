package server;

/*
 * Ivan Yeung
 * Java Texas Holdem
 * NYU Tandon
 * 05/11/2026
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import shared.GamePhase;
import shared.GameState;
import shared.Player;

public class Server {
	private static final int PHASE_TRANSITION_DELAY_MS = 1000;

	private ServerSocket serverSocket;
	private ArrayList<HandleClient> clients;
	private Game game;
	int maxPlayers;
	int port;
	int startingBalance;
	
	public Server(int port, int maxPlayers, int startingBalance) {
		this.port = port;
		this.maxPlayers = maxPlayers;
		this.startingBalance = startingBalance;
		clients = new ArrayList<>();
	}
	
	// Starts the server and waits for enough players to connect
	public void start() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		waitForPlayers();
		startGame();
	}
	
	// Handles a action sent from a client
	public synchronized void handleAction(Player p, String[] action) {
		if (game == null) {
			return;
		}

		game.handleAction(p, action);
		broadcastGameState();
		advanceGame();
	}
	
	// Removes a client from the server and folds them out if needed
	public synchronized void removeClient(HandleClient client) {
		if (!clients.contains(client)) {
			return;
		}

		clients.remove(client);
		client.disconnect();

		if (clients.isEmpty()) {
			try {
				if (serverSocket != null && !serverSocket.isClosed()) {
					serverSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		// if the game has started, fold the disconnected player out
		if (game != null && game.getPhase() != GamePhase.SHOWDOWN && !game.isGameOver()) {
			Player disconnectedPlayer = client.getPlayer();
			if (disconnectedPlayer != null) {
				game.forceFold(disconnectedPlayer);
				broadcastGameState();
				advanceGame();
			}
		}
	}
	
	// Waits for the expected number of players to connect
	private void waitForPlayers() {
		while (clients.size() != maxPlayers) {
			try {
				Socket clientSocket = serverSocket.accept();
				HandleClient clientHandler = 
						new HandleClient(clientSocket, this, startingBalance);
				clients.add(clientHandler);
				clientHandler.sendWaitingState();
				
				Thread thread = new Thread(clientHandler);
				thread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	// Starts a new hand with the currently connected players
	private void startGame() {
		if (clients.size() < 2) {
			return;
		}

		ArrayList<Player> players = new ArrayList<>();
		for (HandleClient client : clients) {
			players.add(client.getPlayer());
		}
		
		game = new Game(players);
		game.startGame();
		broadcastGameState();
	}
	
	// Sends the current game state to every connected client
	public void broadcastGameState() {
		if (game == null) {
			return;
		}

		for (HandleClient client : clients) {
			GameState state = game.buildGameState(client.getPlayer());
			client.sendGameState(state);
		}
	}

	// Advances the game after an action or after a showdown
	private void advanceGame() {
		if (game == null) {
			return;
		}

		while (game.shouldAutoAdvancePhase()) {
			sleepForTransition();
			game.autoAdvancePhase();
			broadcastGameState();
		}

		if (game.getPhase() != GamePhase.SHOWDOWN) {
			return;
		}

		sleepForTransition();

		if (clients.size() < 2 || game.isGameOver()) {
			shutdown();
			return;
		}

		startGame();
	}

	// Sleeps briefly so clients can see automatic transitions
	private void sleepForTransition() {
		try {
			Thread.sleep(PHASE_TRANSITION_DELAY_MS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	// Shuts down the server and disconnects all clients
	private void shutdown() {
		ArrayList<HandleClient> clientsToClose = new ArrayList<>(clients);
		clients.clear();

		for (HandleClient client : clientsToClose) {
			client.disconnect();
		}

		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// Default server settings: Port 3000, 3 players, 1000 starting balance
		Server server = new Server(3000, 3, 1000);
		server.start();
	}
	
}
