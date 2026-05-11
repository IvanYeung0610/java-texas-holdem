package server;

/*
 * Ivan Yeung
 * Java Texas Holdem
 * NYU Tandon
 * 05/11/2026
 */

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

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
	
	public HandleClient(Socket socket, Server server, int startingBalance) throws IOException {
		this.socket = socket;
		this.server = server;
		this.startingBalance = startingBalance;
		this.running = true;

		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String name = in.readLine();
			if (name == null) {
				throw new IOException("Client disconnected before sending a name");
			}

			this.player = new Player(name.trim(), startingBalance);
		} catch (IOException e) {
			disconnect();
			throw e;
		}
	}
	
	public void sendGameState(GameState state) {
		try {
			out.writeObject(state);
			out.flush();
			out.reset();
		} catch (IOException e) {
			server.removeClient(this);
		}
	}

	public void sendWaitingState() {
		sendGameState(new GameState(new ArrayList<>(), new ArrayList<>(), 0, 0, new ArrayList<>(),
				GamePhase.WAITING));
	}

	@Override
	public void run() {
		try {
			while (running) {
				String line = in.readLine();
				if (line == null) {
					server.removeClient(this);
					return;
				}

				String[] action = parseAction(line);
				if (action.length == 0) {
					continue;
				}
				server.handleAction(player, action);
			}
		} catch (IOException e) {
			server.removeClient(this);
		}
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void disconnect() {
		running = false;
		
		try {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String[] parseAction(String line) {
		String trimmedLine = line.trim();
		if (trimmedLine.isEmpty()) {
			return new String[0];
		}
		return trimmedLine.split("\\s+");
	}
}
