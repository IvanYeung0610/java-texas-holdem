package shared;

import java.util.ArrayList;

public class HandEvaluator {
	public int evaluate(ArrayList<Card> cards) {
		// 
		
		return 0;
	}
	
	private int getHandScore(int[] scores) {
		int shift = 20; // first classification number is shifted 20 bits
		int score = scores[0];
		score <<= shift;
		for (int level : scores) {
			shift -= 4;
			score |= (level << shift);
		}
		return score;
	}
}
