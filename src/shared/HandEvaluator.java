package shared;

import java.util.ArrayList;

public class HandEvaluator {
	// Scores associated with each hand pattern
    private static final int HIGH_CARD = 1;
    private static final int ONE_PAIR = 2;
    private static final int TWO_PAIRS = 3;
    private static final int THREE_OF_A_KIND = 4;
    private static final int STRAIGHT = 5;
    private static final int FLUSH = 6;
    private static final int FULL_HOUSE = 7;
    private static final int FOUR_OF_A_KIND = 8;
    private static final int STRAIGHT_FLUSH = 9;
    
    // Class to store boolean indicating if there is a straight
    // and an int indicating the rank of the high card of the straight
    private static class StraightInfo {
    	boolean existStraight  = false;
    	int highCard = 0;
    }
    
    // Class to store information about what rank of a grouping exists
    private static class Grouping {
    	int fourOfAKind = 0;
    	int threeOfAKind = 0;
    	int[] pairs = new int[2];
    	int[] kickers = new int[5];
    }

	public static int evaluate(ArrayList<Card> cards) {
		int maxScore = 0;
		
		Card[] hand = new Card[5];
		
		// find max score of all 21 combinations of 5 from the 7 cards
		for (int i = 0; i < 3; i++) {
			for (int j = i + 1; j < 4; j++) {
				for (int k = j + 1; k < 5; k++) {
					for (int l = k + 1; l < 6; l++) {
						for (int m = l + 1; m < 7; m++) {
							hand[0] = cards.get(i);
							hand[1] = cards.get(j);
							hand[2] = cards.get(k);
							hand[3] = cards.get(l);
							hand[4] = cards.get(m);
							
							int score = evaluateHand(hand);
							
							if (score > maxScore) maxScore = score;
						}
					}
				}
			}
		}

		return maxScore;
	}
	
	// Generates a score by shifting category type and tie breaking cards
	// into separate 4 bits of the int to get a numeric score
	private static int generateScore(int category, int c1, int c2, int c3,
			int c4, int c5) {
		return (category << 20) | (c1 << 16) | (c2 << 12) | (c3 << 8) | (c4 << 4) | c5;
	}
	
	// Gets the score for a hand of five cards
	public static int evaluateHand(Card[] cards) {
		int[] rankCounts = countRanks(cards); // get frequency map of ranks
		boolean isFlush = isFlush(cards);
		int[] distinctRanks = getSortedRanks(rankCounts); // get a array of distinct ranks
		StraightInfo info = isStraight(distinctRanks);
		Grouping grouping = getGroups(rankCounts);
		
		// Straight Flush
		if (isFlush && info.existStraight) {
			// generates score with STRAIGHT_FLUSH score and a tie breaker of high card
			return generateScore(STRAIGHT_FLUSH, info.highCard,
					0, 0, 0, 0);
		}
		
		// Four of a Kind
		if (grouping.fourOfAKind > 0) {
			// generates score with FOUR_OF_A_KIND score and tie breakers of the
			// rank of the four cards and the remaining kicker card
			return generateScore(FOUR_OF_A_KIND, grouping.fourOfAKind,
					grouping.kickers[0], 0, 0, 0);
		}
		
		// Full House
		if (grouping.threeOfAKind > 0 && grouping.pairs[0] > 0) {
			// generates score with FULL_HOUSE score and tie breakers of the rank
			// of the triplet and the rank of the pair
			return generateScore(FULL_HOUSE, grouping.threeOfAKind,
					grouping.pairs[0], 0, 0, 0);
		}
		
		// Flush
		if (isFlush) {
			// generate score with FLUSH score and tie breakers of the ranks of
			// all five cards from highest to lowest
			return generateScore(FLUSH, distinctRanks[0], distinctRanks[1],
					distinctRanks[2], distinctRanks[3], distinctRanks[4]);
		}
		
		// Straight
		if (info.existStraight) {
			// generate score with STRAIGHT score and tie breaker of the high card
			return generateScore(STRAIGHT, info.highCard,
					0, 0, 0, 0);
		}
		
		// Three of a Kind
		if (grouping.threeOfAKind > 0) {
			// generate score with THREE_OF_A_KIND score and tie breakers
			// of the rank of the two kicker cards of the hand
			return generateScore(THREE_OF_A_KIND, grouping.threeOfAKind,
					grouping.kickers[0], grouping.kickers[1], 0, 0);
		}
		
		// Two Pairs
		if (grouping.pairs[0] > 0 && grouping.pairs[1] > 0) {
			// generate score with TWO_PAIRS score and tie breakers of
			// the rank of the pairs and the remaining kicker card
			return generateScore(TWO_PAIRS, grouping.pairs[0], grouping.pairs[1],
					grouping.kickers[0], 0, 0);
		}
		
		// One Pair
		if (grouping.pairs[0] > 0) {
			// generate score with ONE_PAIR score and tie breakers of the rank
			// of the pair and the rank of the remaining kicker cards
			return generateScore(ONE_PAIR, grouping.pairs[0],
					grouping.kickers[0], grouping.kickers[1], grouping.kickers[2], 0);
		}
		
		// High Card (No pattern)
		// generate score with HIGH_CARD score and tie breakers of the rank of
		// all the cards in the hand
			return generateScore(HIGH_CARD, distinctRanks[0], distinctRanks[1],
					distinctRanks[2], distinctRanks[3], distinctRanks[4]);
	}
	
	// returns an array with index corresponding to rank and the value at
	// the index indicating the frequency of the rank in the hand
	private static int[] countRanks(Card[] cards) {
		int[] counts = new int[15];
		
		for (Card card : cards) {
			counts[card.getRank()]++;
		}
		
		return counts;
	}
	
	private static boolean isFlush(Card[] cards) {
		Suit suit = cards[0].getSuit();
		
		for (int i = 1; i < cards.length; i++) {
			if (cards[i].getSuit() != suit) {
				return false;
			}
		}
		
		return true;
	}
	
	// Returns a int array of size 5 with the ranks that exists
	// in the hand from highest rank to lowest
	private static int[] getSortedRanks(int[] rankCounts) {
		int[] ranks = {0, 0, 0, 0, 0};
		int index = 0;
		
		for (int rank = 14; rank >= 2; rank--) {
			if (rankCounts[rank] > 0) {
				ranks[index] = rank;
				index++;
			}
		}
		
		return ranks;
	}
	
	// Checks if there is a straight and returns a StraightInfo class
	// that contains information about whether there is a straight and
	// what the high card of the straight is if there is a straight
	private static StraightInfo isStraight(int[] ranks) {
		StraightInfo info = new StraightInfo();
		
		// if there aren't 5 distinct ranks, straight is not possible
		if (ranks[4] == 0) {
			return info;
		}
		
		// Edge case: Straight with A as "1". Ex: A, 5, 4, 3, 2
		if (ranks[0] == 14 && ranks[1] == 5 && ranks[2] == 4 &&
				ranks[3] == 3 && ranks[4] == 2) {
			info.existStraight = true;
			info.highCard = 5;
			return info;
		}

		for (int i = 0; i < 4; i++) {
			if (ranks[i] - 1 != ranks[i + 1]) {
				return info;
			}
		}
		
		info.existStraight = true;
		info.highCard = ranks[0];
		return info;
	}
	
	private static Grouping getGroups(int[] rankCounts) {
		Grouping grouping = new Grouping();
		
		int kickerIndex = 0;
		int pairIndex = 0;
		
		for (int rank = 14; rank >= 2; rank--) {
			switch(rankCounts[rank]) {
			case 4:
				grouping.fourOfAKind = rank;
				break;
			case 3:
				grouping.threeOfAKind = rank;
				break;
			case 2:
				grouping.pairs[pairIndex] = rank;
				pairIndex++;
				break;
			case 1:
				grouping.kickers[kickerIndex] = rank;
				kickerIndex++;
				break;
			}
		}
		
		return grouping;
	}
}
