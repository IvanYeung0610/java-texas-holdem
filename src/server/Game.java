package server;

import shared.*;
import java.util.ArrayList;

public class Game {
	private Deck deck;
	private ArrayList<Player> players;
	private Pot pot;
	private ArrayList<Card> communityCards;
	private int currentPlayerIndex;
	private GamePhase phase;
	private int currentBet;
	private int lastAggressorIndex;
	private int smallBlindIndex;
	
	public Game(ArrayList<Player> players) {
		this.players = players;
		this.deck = new Deck();
		this.pot = new Pot();
		this.communityCards = new ArrayList<>();
		this.currentPlayerIndex = 0;
		this.phase = GamePhase.WAITING;
		this.currentBet = 0;
	}
	
	public void startGame() {
		// TODO: Start the round? Call the other functions
	}
	
	// Give each player two cards from the deck
	public void dealHoleCards() {
		for (Player player : players) {
			Card firstCard = deck.deal();
			player.receiveCard(firstCard);
			Card secondCard = deck.deal();
			player.receiveCard(secondCard);
		}
	}
	
	public void dealFlop() {
		for (int i = 0; i < 3; i++) {
			Card card = deck.deal();
			communityCards.add(card);
		}
	}
	
	public void dealTurn() {
		Card card = deck.deal();
		communityCards.add(card);
	}
	
	public void dealRiver() {
		Card card = deck.deal();
		communityCards.add(card);
	}
	
	public void handleAction(Player p, String[] action) {
		// Make switch statement for the different actions
		// Actions: FOLD, CALL, CHECK, RAISE
	}
	
	public GameState buildGameState(Player p) {
		// returns game state object
	}
	
	public boolean isGameOver() {
		// checks if only one player has a balance greater than 0
	}
	
	private void postBlinds() {
		// get small blind player and calls placeBet(smallBlindAmount)
		// gets big blind player ang calls placeBet(bigBlindAmount) on them
	}
	
	private void handleFold(Player p) {
		
	}
	
	private void handleCall(Player p) {
		
	}
	
	private void handleCheck(Player p) {
		
	}
	
	private void handleRaise(Player p, int amount) {
		
	}

	// Need to look over
	public void advancePhase() {
		switch(phase) {
			case WAITING:
				phase = GamePhase.PRE_FLOP;
				break;
			case PRE_FLOP:
				phase = GamePhase.FLOP;
				break;
			case FLOP:
				phase = GamePhase.TURN;
				break;
			case TURN:
				phase = GamePhase.RIVER;
				break;
			case RIVER:
				phase = GamePhase.SHOWDOWN;
				break;
			case SHOWDOWN:
				phase = GamePhase.PRE_FLOP;
				break;
		}
	}
	
	private void determineWinner() {
		ArrayList<Player> winners = new ArrayList<>();
		int winnerScore = 0;
		for (Player player : players) {
			if (!player.isFolded()) {
				ArrayList<Card> playerCards = new ArrayList<>();
				playerCards.addAll(player.getHand());
				playerCards.addAll(communityCards);
				int score = HandEvaluator.evaluate(playerCards);
				if (score > winnerScore) {
					winners.clear();
					winners.add(player);
					winnerScore = score;
				} else if (score == winnerScore) { // In the case of a tie
					winners.add(player);
				}
			}
		}
		
		awardPot(winners);
	}
	
	private void awardPot(ArrayList<Player> winners) {
		pot.award(winners);
		// Need to get names?
	}
	
}
