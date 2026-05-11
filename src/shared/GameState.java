package shared;

/*
 * Ivan Yeung
 * Java Texas Holdem
 * NYU Tandon
 * 05/11/2026
 */

import java.io.Serializable;
import java.util.ArrayList;

public class GameState implements Serializable {
	public static final long serialVersionUID = 1L;
	private ArrayList<Card> communityCards;
	private ArrayList<Player> players;
	private int potTotal;
	private int currentPlayer; // the player that is going
	private ArrayList<String> winners;
	private GamePhase phase;
	
	public GameState(ArrayList<Card> communityCards, ArrayList<Player> players, int potTotal,
			int currentPlayer, ArrayList<String> winners, GamePhase phase) {
		this.communityCards = communityCards;
		this.players = players;
		this.potTotal = potTotal;
		this.currentPlayer = currentPlayer;
		this.winners = winners;
		this.phase = phase;
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

	public ArrayList<String> getWinner() {
		return winners;
	}
	
	public GamePhase getPhase() {
		return phase;
	}
}
