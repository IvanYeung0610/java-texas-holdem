package shared;

/*
 * Ivan Yeung
 * Java Texas Holdem
 * NYU Tandon
 * 05/11/2026
 */

import java.util.ArrayList;
import java.io.Serializable;

public class Player implements Serializable {
	public static final long serialVersionUID = 1L;
	private String name;
	private int balance;
	private int currentBet;
	private boolean folded;
	private boolean allIn;
	private ArrayList<Card> hand;
	
	public Player(String name, int balance) {
		this.name = name;
		this.balance = balance;
		this.currentBet = 0;
		this.folded = false;
		this.allIn = false;
		this.hand = new ArrayList<>();
	}
	
	public Player(Player other) {
		this(other, false);
	}

	// boolean determines whether to copy the hand
	public Player(Player other, boolean self) {
		this.name = other.name;
		this.balance = other.balance;
		this.currentBet = other.currentBet;
		this.folded = other.folded;
		this.allIn = other.allIn;
		// only copies over the hand if self is true
		if (self) {
			this.hand = new ArrayList<>(other.hand);
		} else {
			this.hand = new ArrayList<>();
		}
	}
	
	public void receiveCard(Card card) {
		hand.add(card);
	} 
	public void placeBet(int amount) {
		if (amount > balance) {
			System.err.println("Not enough balance to place bet");
			return;
		}
		balance -= amount;
		currentBet += amount;
		if (balance == 0) {
			allIn = true;
		}
	}
	
	public void fold() {
		folded = true;
	}
	
	public void reset() {
		currentBet = 0;
		folded = false;
		allIn = false;
		hand.clear();
	}

	public void resetCurrentBet() {
		currentBet = 0;
	}
	
	public void addBalance(int amount) {
		balance += amount;
	}

	public String getName() {
		return name;
	}

	public int getBalance() {
		return balance;
	}

	public int getCurrentBet() {
		return currentBet;
	}

	public boolean isFolded() {
		return folded;
	}

	public boolean isAllIn() {
		return allIn;
	}

	public ArrayList<Card> getHand() {
		return hand;
	}
}
