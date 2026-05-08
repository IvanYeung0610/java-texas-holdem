package shared;

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
	
	Player(String name, int balance) {
		this.name = name;
		this.balance = balance;
		this.currentBet = 0;
		this.folded = false;
		this.allIn = false;
		this.hand = new ArrayList<>();
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
