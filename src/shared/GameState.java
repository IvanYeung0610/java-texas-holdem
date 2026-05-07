package shared;

import java.io.Serializable;
import java.util.ArrayList;

public class GameState implements Serializable {
	public static final long serialVersionUID = 1L;
	private ArrayList<Card> communityCards;
	private ArrayList<Player> players;
	private int potTotal;
	private int currentPlayer; // the player that is going
	private String winner;
	
	public GameState(ArrayList<Card> communityCards, ArrayList<Player> players, int potTotal,
			int currentPlayer, String winner) {
		this.communityCards = communityCards;
		this.players = players;
		this.potTotal = potTotal;
		this.currentPlayer = currentPlayer;
		this.winner = winner;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public ArrayList<Card> getCommunityCards() {
		return communityCards;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}

	public int getPotTotal() {
		return potTotal;
	}

	public int getCurrentPlayer() {
		return currentPlayer;
	}

	public String getWinner() {
		return winner;
	}
}
