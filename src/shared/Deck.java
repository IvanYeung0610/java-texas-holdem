package shared;

/*
 * Ivan Yeung
 * Java Texas Holdem
 * NYU Tandon
 * 05/11/2026
 */

import java.util.ArrayList;
import java.util.Collections;

public class Deck {
	private ArrayList<Card> cards;
	
	// Builds all 52 cards used in the deck
	public Deck() {
		// initialize list
		cards = new ArrayList<>();

		for (Suit suit : Suit.values()) {
			for (int i = 2; i < 15; i++) {
				cards.add(new Card(i, suit));
			}
		}
	}
	
	// Randomly shuffles the cards in the deck
	public void shuffle() {
		Collections.shuffle(cards);
	}
	
	// Removes top card from the deck
	public Card deal() {
		if (cards.size() == 0) {
			System.err.println("Deck is empty");
			return null;
		}
		Card topCard = cards.remove(cards.size() - 1);
		return topCard;
	}
	
	// Rebuilds the deck and shuffles it
	public void reset() {
		cards.clear();
		for (Suit suit : Suit.values()) {
			for (int i = 2; i < 15; i++) {
				cards.add(new Card(i, suit));
			}
		}
		Collections.shuffle(cards);
	}
	
	public int size() {
		return cards.size();
	}
}
