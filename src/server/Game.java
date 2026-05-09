package server;

import shared.Card;
import shared.Deck;
import shared.GamePhase;
import shared.GameState;
import shared.HandEvaluator;
import shared.Player;
import shared.Pot;

import java.util.ArrayList;

public class Game {
	private static final int SMALL_BLIND_AMOUNT = 10;
	private static final int BIG_BLIND_AMOUNT = 20;

	private Deck deck;
	private ArrayList<Player> players;
	private Pot pot;
	private ArrayList<Card> communityCards;
	private int currentPlayerIndex;
	private int lastAggressorIndex;
	private int currentBet;
	private GamePhase phase;
	private int smallBlindIndex;
	private int potTotal;
	private ArrayList<String> winnerNames;
	private int lastActorIndex;
	private boolean lastAggressorHasActed;

	public Game(ArrayList<Player> players) {
		this.players = players;
		this.deck = new Deck();
		this.pot = new Pot();
		this.communityCards = new ArrayList<>();
		this.currentPlayerIndex = 0;
		this.lastAggressorIndex = 0;
		this.currentBet = 0;
		this.phase = GamePhase.WAITING;
		this.smallBlindIndex = 0;
		this.potTotal = 0;
		this.winnerNames = new ArrayList<>();
		this.lastActorIndex = -1;
		this.lastAggressorHasActed = false;
	}

	public void startGame() {
		if (players == null || players.size() < 2) {
			phase = GamePhase.WAITING;
			return;
		}

		resetRound();
		if (players.size() < 2) {
			phase = GamePhase.WAITING;
			return;
		}

		postBlinds();
		dealHoleCards();
		phase = GamePhase.PRE_FLOP;
		currentPlayerIndex = (smallBlindIndex + 2) % players.size();
		currentPlayerIndex = findNextIndex(currentPlayerIndex, false);
		lastAggressorIndex = (smallBlindIndex + 1) % players.size();
		lastAggressorHasActed = false;
		lastActorIndex = -1;

		if (!hasPlayersWhoCanAct()) {
			advancePhase();
		}
	}

	public void handleAction(Player p, String[] action) {
		if (p == null || action == null || action.length == 0 || phase == GamePhase.WAITING
				|| phase == GamePhase.SHOWDOWN) {
			return;
		}

		if (!isCurrentPlayer(p)) {
			return;
		}

		switch (action[0]) {
		case "FOLD":
			handleFold(p);
			break;
		case "CALL":
			handleCall(p);
			break;
		case "CHECK":
			handleCheck(p);
			break;
		case "RAISE":
			handleRaise(p, Integer.parseInt(action[1]));
			break;
		default:
			return;
		}

		checkRoundState();
	}

	public GameState buildGameState(Player recipient) {
		ArrayList<Card> communityCopy = new ArrayList<>(communityCards);
		ArrayList<Player> playersCopy = new ArrayList<>();
		ArrayList<String> winnersCopy = new ArrayList<>(winnerNames);

		for (Player player : players) {
			// if player is the recipient of the game state,
			// it will get a copy of it's hand
			playersCopy.add(new Player(player, player == recipient));
		}

		return new GameState(communityCopy, playersCopy, potTotal, currentPlayerIndex, winnersCopy, phase);
	}

	public boolean isGameOver() {
		int playersWithChips = 0;

		for (Player player : players) {
			if (player.getBalance() > 0) {
				playersWithChips++;
			}
		}

		return playersWithChips <= 1;
	}

	private void postBlinds() {
		Player smallBlindPlayer = players.get(smallBlindIndex);
		int bigBlindIndex = (smallBlindIndex + 1) % players.size();
		Player bigBlindPlayer = players.get(bigBlindIndex);

		int smallBlindPosted = Math.min(SMALL_BLIND_AMOUNT, smallBlindPlayer.getBalance());
		int bigBlindPosted = Math.min(BIG_BLIND_AMOUNT, bigBlindPlayer.getBalance());

		if (smallBlindPosted > 0) {
			smallBlindPlayer.placeBet(smallBlindPosted);
			pot.addBet(smallBlindPosted);
			potTotal += smallBlindPosted;
		}

		if (bigBlindPosted > 0) {
			bigBlindPlayer.placeBet(bigBlindPosted);
			pot.addBet(bigBlindPosted);
			potTotal += bigBlindPosted;
		}

		currentBet = bigBlindPlayer.getCurrentBet();
		lastAggressorIndex = bigBlindIndex;
	}

	private void handleFold(Player p) {
		p.fold();
		lastActorIndex = players.indexOf(p);
		if (lastActorIndex == lastAggressorIndex) {
			lastAggressorHasActed = true;
		}

		ArrayList<Player> activePlayers = getActivePlayers();
		if (activePlayers.size() == 1) {
			awardPot(activePlayers);
			phase = GamePhase.SHOWDOWN;
			return;
		}

		advanceTurn();
	}

	private void handleCall(Player p) {
		int amountOwed = Math.max(0, currentBet - p.getCurrentBet());
		int amountToPlace = Math.min(amountOwed, p.getBalance());

		if (amountToPlace > 0) {
			p.placeBet(amountToPlace);
			pot.addBet(amountToPlace);
			potTotal += amountToPlace;
		}

		lastActorIndex = players.indexOf(p);
		if (lastActorIndex == lastAggressorIndex) {
			lastAggressorHasActed = true;
		}

		advanceTurn();
	}

	private void handleCheck(Player p) {
		if (p.getCurrentBet() != currentBet) {
			return;
		}

		lastActorIndex = players.indexOf(p);
		if (lastActorIndex == lastAggressorIndex) {
			lastAggressorHasActed = true;
		}

		advanceTurn();
	}

	private void handleRaise(Player p, int raiseAmount) {
		if (raiseAmount <= currentBet) {
			return;
		}

		int totalOwed = raiseAmount - p.getCurrentBet();
		if (totalOwed <= 0) {
			return;
		}

		int amountToPlace = Math.min(totalOwed, p.getBalance());
		if (amountToPlace <= 0) {
			return;
		}

		p.placeBet(amountToPlace);
		pot.addBet(amountToPlace);
		potTotal += amountToPlace;

		currentBet = p.getCurrentBet();
		lastAggressorIndex = players.indexOf(p);
		lastActorIndex = lastAggressorIndex;
		lastAggressorHasActed = true;

		advanceTurn();
	}

	private void advanceTurn() {
		if (phase == GamePhase.SHOWDOWN || players.isEmpty()) {
			return;
		}

		if (!hasPlayersWhoCanAct()) {
			return;
		}

		currentPlayerIndex = findNextIndex((currentPlayerIndex + 1) % players.size(), false);
	}

	private void checkRoundState() {
		if (phase == GamePhase.WAITING || phase == GamePhase.SHOWDOWN) {
			return;
		}

		if (isBettingRoundOver()) {
			advancePhase();
		}
	}

	private boolean isBettingRoundOver() {
		ArrayList<Player> activePlayers = getActivePlayers();
		if (activePlayers.size() <= 1) {
			return true;
		}

		if (!hasPlayersWhoCanAct()) {
			return true;
		}

		for (Player player : activePlayers) {
			if (!player.isAllIn() && player.getCurrentBet() != currentBet) {
				return false;
			}
		}

		if (!lastAggressorHasActed) {
			return false;
		}

		return currentPlayerIndex == lastAggressorIndex || lastActorIndex == lastAggressorIndex;
	}

	private void advancePhase() {
		resetCurrentBets();
		currentBet = 0;
		lastActorIndex = -1;

		int startingIndex = findPhaseStartIndex();
		currentPlayerIndex = startingIndex;
		lastAggressorIndex = startingIndex;
		lastAggressorHasActed = false;

		switch (phase) {
		case PRE_FLOP:
			dealFlop();
			phase = GamePhase.FLOP;
			break;
		case FLOP:
			dealTurn();
			phase = GamePhase.TURN;
			break;
		case TURN:
			dealRiver();
			phase = GamePhase.RIVER;
			break;
		case RIVER:
			determineWinner();
			phase = GamePhase.SHOWDOWN;
			return;
		case WAITING:
		case SHOWDOWN:
		default:
			return;
		}

		if (!hasPlayersWhoCanAct()) {
			advancePhase();
		}
	}

	private void dealHoleCards() {
		for (int i = 0; i < 2; i++) {
			for (Player player : players) {
				player.receiveCard(deck.deal());
			}
		}
	}

	private void dealFlop() {
		for (int i = 0; i < 3; i++) {
			communityCards.add(deck.deal());
		}
	}

	private void dealTurn() {
		communityCards.add(deck.deal());
	}

	private void dealRiver() {
		communityCards.add(deck.deal());
	}

	private void determineWinner() {
		ArrayList<Player> activePlayers = getActivePlayers();
		ArrayList<Player> winners = new ArrayList<>();
		int bestScore = Integer.MIN_VALUE;

		for (Player player : activePlayers) {
			ArrayList<Card> allCards = new ArrayList<>(player.getHand());
			allCards.addAll(communityCards);

			int score = HandEvaluator.evaluate(allCards);
			if (score > bestScore) {
				winners.clear();
				winners.add(player);
				bestScore = score;
			} else if (score == bestScore) {
				winners.add(player);
			}
		}

		awardPot(winners);
	}

	private void awardPot(ArrayList<Player> winners) {
		pot.award(winners);
		potTotal = 0;
		
		for (Player winner : winners) {
			winnerNames.add(winner.getName());
		}
	}

	private ArrayList<Player> getActivePlayers() {
		ArrayList<Player> activePlayers = new ArrayList<>();

		for (Player player : players) {
			if (!player.isFolded()) {
				activePlayers.add(player);
			}
		}

		return activePlayers;
	}

	private void resetRound() {
		removeEliminatedPlayers();
		if (players.isEmpty()) {
			return;
		}

		deck.reset();
		communityCards.clear();
		pot = new Pot();
		potTotal = 0;
		currentBet = 0;
		winnerNames.clear();
		lastActorIndex = -1;
		lastAggressorHasActed = false;

		for (Player player : players) {
			player.reset();
		}

		smallBlindIndex = (smallBlindIndex + 1) % players.size();
	}

	private boolean isCurrentPlayer(Player player) {
		if (players.isEmpty() || currentPlayerIndex < 0 || currentPlayerIndex >= players.size()) {
			return false;
		}
		return players.get(currentPlayerIndex) == player;
	}

	private boolean hasPlayersWhoCanAct() {
		for (Player player : players) {
			if (!player.isFolded() && !player.isAllIn()) {
				return true;
			}
		}
		return false;
	}

	private int findNextIndex(int startIndex, boolean includeAllIn) {
		if (players.isEmpty()) {
			return 0;
		}

		for (int offset = 0; offset < players.size(); offset++) {
			int index = (startIndex + offset) % players.size();
			Player player = players.get(index);

			if (player.isFolded()) {
				continue;
			}

			if (!includeAllIn && player.isAllIn()) {
				continue;
			}

			return index;
		}

		return startIndex % players.size();
	}

	private int findPhaseStartIndex() {
		if (players.isEmpty()) {
			return 0;
		}

		int preferredIndex = findNextIndex((smallBlindIndex + 1) % players.size(), false);
		Player preferredPlayer = players.get(preferredIndex);
		if (!preferredPlayer.isFolded() && !preferredPlayer.isAllIn()) {
			return preferredIndex;
		}

		return findNextIndex((smallBlindIndex + 1) % players.size(), true);
	}

	private void resetCurrentBets() {
		for (Player player : players) {
			player.resetCurrentBet();
		}
	}

	private void removeEliminatedPlayers() {
		players.removeIf(player -> player.getBalance() <= 0);
		if (players.isEmpty()) {
			smallBlindIndex = 0;
		} else {
			smallBlindIndex %= players.size();
		}
	}
	
	public GamePhase getPhase() {
		return phase;
	}

}
