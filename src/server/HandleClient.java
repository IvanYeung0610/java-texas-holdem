package server;

import java.io.*;
import java.net.Socket;

import shared.Player;
import shared.GamePhase;
import shared.GameState;

public class HandleClient implements Runnable{
	private Socket socket;
	private Player player;
	private BufferedReader in; // Reads in text from client to get what it did
	private ObjectOutputStream out; // Sends out game state objects
	private Server server;
	private boolean running;
	private int startingBalance;
	
	public HandleClient(Socket socket, Server server, int startingBalance) {
		this.socket = socket;
		this.server = server;
		this.startingBalance = startingBalance;
	}
	
	public void sendGameState(GameState state) {
		try {
			out.writeObject(state);
			out.flush();
			out.reset();
		} catch (IOException e) {
			// TODO: call server's removeClient
		}
	}

	@Override
	public void run() {
		running = true;
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			// immediately read name to initialize the player
			String name = in.readLine().trim();
			this.player = new Player(name, startingBalance);
			
			// sends WAITING GameState to notify client that they are connected
			sendGameState(new GameState(null, null, 0, 0, null, GamePhase.WAITING));
			
			while (running) {
				String line = in.readLine();
				String[] action = line.trim().split(" ");
				server.handleAction(player, action);
			}

		} catch(IOException e) {
			// TODO: calls server's removeClient
			running = false;
		}
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void disconnect() {
		running = false;
		
		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
