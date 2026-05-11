package shared;

/*
 * Ivan Yeung
 * Java Texas Holdem
 * NYU Tandon
 * 05/11/2026
 */

import java.util.ArrayList;

public class Pot {
	private int total;
	
	public Pot() {
		total = 0;
	}
	
	public void addBet(int amount) {
		total += amount;
	}
	
	// transfers money in pot to winning player(s)
	public void award(ArrayList<Player> winners) {
		int awardAmount = total / winners.size();
		for (Player winner : winners) {
			winner.addBalance(awardAmount);
		}
		total = 0;
	}
	
}
