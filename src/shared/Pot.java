package shared;

public class Pot {
	private int total;
	
	public Pot() {
		total = 0;
	}
	
	public void addBet(int amount) {
		total += amount;
	}
	
	// transfers money in pot to player
	public void award(Player winner) {
		winner.addBalance(total);
		total = 0;
	}
	
}
