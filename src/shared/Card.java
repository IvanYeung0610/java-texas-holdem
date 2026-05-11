package shared;

/*
 * Ivan Yeung
 * Java Texas Holdem
 * NYU Tandon
 * 05/11/2026
 */

import java.io.Serializable;

/**
 * This class contains information about a single card
 * The rank is represented using int. The numerical ranks
 * are represented by their respective number. The int representing
 * the non numerical ranks are shown below:
 * 	Jack = 11
 * 	Queen = 12
 * 	King = 13
 * 	Ace = 14
 */
public class Card implements Serializable {
	public static final long serialVersionUID = 1L;
	private final Suit suit;
	private final int rank;
	
	public Card(int rank, Suit suit) {
		this.rank = rank;
		this.suit = suit;
	}

	public Suit getSuit() {
		return suit;
	}

	public int getRank() {
		return rank;
	}

	@Override
	public String toString() {
		return suit + "," + rank;
	}
}