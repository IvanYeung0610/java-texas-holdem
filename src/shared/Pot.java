package shared;

import java.util.ArrayList;

public class Pot {
	private int total;
	
	public Pot() {
		total = 0;
	}
	
	public void addBet(int amount) {
		total += amount;
	}
	
	// transfers money in pot to player
	public void award(ArrayList<Player> winners) {
		int awardAmount = total / winners.size();
		for (Player winner : winners) {
			winner.addBalance(awardAmount);
		}
		total = 0;
	}
	
}
