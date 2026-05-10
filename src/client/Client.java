package client;

import shared.Card;
import shared.GamePhase;
import shared.GameState;
import shared.Player;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Client {
	private Socket socket;
	private ObjectInputStream in;
	private PrintWriter out;
	private String playerName;
	private GameState lastGameState;

	private JFrame frame;
	private JLabel statusLabel;
	private JLabel potLabel;
	private JPanel playersPanel;
	private JPanel communityPanel;
	private JPanel handPanel;
	private JPanel actionPanel;
	private JTextField raiseField;
	private JButton foldButton;
	private JButton callButton;
	private JButton checkButton;
	private JButton raiseButton;

	public Client(String host, int port, String playerName) throws IOException {
		this.playerName = playerName;
		this.socket = new Socket(host, port);
		this.in = new ObjectInputStream(socket.getInputStream());
		this.out = new PrintWriter(socket.getOutputStream(), true);
		this.out.println(playerName);
		this.out.flush();

		buildUI();
		listenForUpdates();
	}

	private void buildUI() {
		frame = new JFrame("Texas Hold'em - " + playerName);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(900, 600);
		frame.setLocationRelativeTo(null);
		frame.setLayout(new BorderLayout(10, 10));

		JPanel headerPanel = new JPanel(new GridLayout(2, 1));
		statusLabel = new JLabel("Waiting for game to start...", SwingConstants.CENTER);
		potLabel = new JLabel("Pot: 0", SwingConstants.CENTER);
		headerPanel.add(statusLabel);
		headerPanel.add(potLabel);

		playersPanel = new JPanel();
		playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
		playersPanel.setBorder(BorderFactory.createTitledBorder("Players"));

		communityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 16));
		communityPanel.setBorder(BorderFactory.createTitledBorder("Community Cards"));

		handPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 16));
		handPanel.setBorder(BorderFactory.createTitledBorder("Your Hand"));

		actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

		foldButton = new JButton("Fold");
		callButton = new JButton("Call");
		checkButton = new JButton("Check");
		raiseButton = new JButton("Raise");
		raiseField = new JTextField(8);

		foldButton.addActionListener(e -> sendAction("FOLD"));
		callButton.addActionListener(e -> sendAction("CALL"));
		checkButton.addActionListener(e -> sendAction("CHECK"));
		raiseButton.addActionListener(e -> {
			String raiseText = raiseField.getText().trim();
			if (raiseText.isEmpty()) {
				return;
			}
			try {
				Integer.parseInt(raiseText);
				sendAction("RAISE " + raiseText);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(frame, "Raise amount must be a number.",
						"Invalid Raise", JOptionPane.ERROR_MESSAGE);
			}
		});

		actionPanel.add(foldButton);
		actionPanel.add(callButton);
		actionPanel.add(checkButton);
		actionPanel.add(raiseField);
		actionPanel.add(raiseButton);

		setActionButtonsEnabled(false);

		JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
		centerPanel.add(communityPanel);
		centerPanel.add(handPanel);

		frame.add(headerPanel, BorderLayout.NORTH);
		frame.add(new JScrollPane(playersPanel), BorderLayout.WEST);
		frame.add(centerPanel, BorderLayout.CENTER);
		frame.add(actionPanel, BorderLayout.SOUTH);
		frame.setVisible(true);
	}

	private void listenForUpdates() {
		Thread listenerThread = new Thread(() -> {
			try {
				while (true) {
					GameState state = (GameState) in.readObject();
					lastGameState = state;
					SwingUtilities.invokeLater(() -> updateUI(state));
				}
			} catch (IOException | ClassNotFoundException e) {
				SwingUtilities.invokeLater(() -> {
					setActionButtonsEnabled(false);
					statusLabel.setText("Disconnected from server.");
				});
			}
		});

		listenerThread.setDaemon(true);
		listenerThread.start();
	}

	private void updateUI(GameState state) {
		ArrayList<Player> players = state.getPlayers() == null ? new ArrayList<>() : state.getPlayers();
		ArrayList<Card> communityCards = state.getCommunityCards() == null
				? new ArrayList<>() : state.getCommunityCards();

		String currentPlayerName = "N/A";
		if (!players.isEmpty() && state.getCurrentPlayer() >= 0 && state.getCurrentPlayer() < players.size()) {
			currentPlayerName = players.get(state.getCurrentPlayer()).getName();
		}

		statusLabel.setText("Phase: " + state.getPhase() + " | Current Player: " + currentPlayerName);
		potLabel.setText("Pot: " + state.getPotTotal());

		playersPanel.removeAll();
		for (int i = 0; i < players.size(); i++) {
			Player player = players.get(i);
			String turnMarker = i == state.getCurrentPlayer() && state.getPhase() != GamePhase.SHOWDOWN ? " <- Turn" : "";
			String playerText = player.getName()
					+ " | Balance: " + player.getBalance()
					+ " | Bet: " + player.getCurrentBet()
					+ (player.isFolded() ? " | Folded" : "")
					+ (player.isAllIn() ? " | All-In" : "")
					+ turnMarker;
			playersPanel.add(new JLabel(playerText));
		}

		communityPanel.removeAll();
		for (Card card : communityCards) {
			communityPanel.add(createCardComponent(card));
		}

		handPanel.removeAll();
		Player self = findSelf(players);
		if (self != null) {
			for (Card card : self.getHand()) {
				handPanel.add(createCardComponent(card));
			}
		}

		boolean isMyTurn = state.getPhase() != GamePhase.WAITING
				&& state.getPhase() != GamePhase.SHOWDOWN
				&& self != null
				&& !players.isEmpty()
				&& state.getCurrentPlayer() >= 0
				&& state.getCurrentPlayer() < players.size()
				&& players.get(state.getCurrentPlayer()).getName().equals(playerName);

		setActionButtonsEnabled(isMyTurn);

		if (state.getPhase() == GamePhase.SHOWDOWN) {
			ArrayList<String> winners = state.getWinner();
			String winnerText = (winners == null || winners.isEmpty())
					? "No winner"
					: String.join(", ", winners);
			statusLabel.setText("Showdown | Winner: " + winnerText);
			setActionButtonsEnabled(false);
		}

		frame.revalidate();
		frame.repaint();
	}

	private void sendAction(String action) {
		out.println(action);
		out.flush();
		setActionButtonsEnabled(false);
	}

	private Player findSelf(ArrayList<Player> players) {
		for (Player player : players) {
			if (player.getName().equals(playerName)) {
				return player;
			}
		}
		return null;
	}

	private JComponent createCardComponent(Card card) {
		return new CardView(card);
	}

	private String getRankText(Card card) {
		int rank = card.getRank();
		switch (rank) {
		case 11:
			return "J";
		case 12:
			return "Q";
		case 13:
			return "K";
		case 14:
			return "A";
		default:
			return Integer.toString(rank);
		}
	}

	private String getSuitSymbol(Card card) {
		switch (card.getSuit()) {
		case HEART:
			return "\u2665";
		case DIAMOND:
			return "\u2666";
		case CLUB:
			return "\u2663";
		case SPADE:
			return "\u2660";
		default:
			return "?";
		}
	}

	private Color getSuitColor(Card card) {
		switch (card.getSuit()) {
		case HEART:
		case DIAMOND:
			return new Color(184, 35, 35);
		case CLUB:
		case SPADE:
		default:
			return new Color(25, 25, 25);
		}
	}

	private void setActionButtonsEnabled(boolean enabled) {
		foldButton.setEnabled(enabled);
		callButton.setEnabled(enabled);
		checkButton.setEnabled(enabled);
		raiseButton.setEnabled(enabled);
		raiseField.setEnabled(enabled);
	}

	private final class CardView extends JPanel {
		private static final int CARD_WIDTH = 90;
		private static final int CARD_HEIGHT = 130;
		private final Card card;

		private CardView(Card card) {
			this.card = card;
			setOpaque(false);
			setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
			setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
			setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
			setAlignmentY(Component.CENTER_ALIGNMENT);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			Color suitColor = getSuitColor(card);
			String rankText = getRankText(card);
			String suitText = getSuitSymbol(card);

			g2.setColor(Color.WHITE);
			g2.fillRoundRect(0, 0, CARD_WIDTH - 1, CARD_HEIGHT - 1, 18, 18);
			g2.setColor(new Color(55, 55, 55));
			g2.drawRoundRect(0, 0, CARD_WIDTH - 1, CARD_HEIGHT - 1, 18, 18);

			g2.setColor(suitColor);
			g2.setFont(new Font("SansSerif", Font.BOLD, 20));
			g2.drawString(rankText, 10, 24);
			g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
			g2.drawString(suitText, 12, 44);

			g2.setFont(new Font("SansSerif", Font.PLAIN, 40));
			Font centerFont = g2.getFont();
			int centerSuitWidth = g2.getFontMetrics(centerFont).stringWidth(suitText);
			g2.drawString(suitText, (CARD_WIDTH - centerSuitWidth) / 2, 78);

			g2.setFont(new Font("SansSerif", Font.BOLD, 20));
			int bottomRankWidth = g2.getFontMetrics().stringWidth(rankText);
			g2.drawString(rankText, CARD_WIDTH - bottomRankWidth - 10, CARD_HEIGHT - 20);
			g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
			int bottomSuitWidth = g2.getFontMetrics().stringWidth(suitText);
			g2.drawString(suitText, CARD_WIDTH - bottomSuitWidth - 12, CARD_HEIGHT - 40);

			g2.dispose();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame joinFrame = new JFrame("Join Texas Hold'em");
			joinFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			joinFrame.setSize(350, 220);
			joinFrame.setLocationRelativeTo(null);
			joinFrame.setLayout(new BorderLayout(10, 10));

			JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
			JTextField nameField = new JTextField();
			JTextField hostField = new JTextField("localhost");
			JTextField portField = new JTextField("3000");

			fieldsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
			fieldsPanel.add(new JLabel("Name:"));
			fieldsPanel.add(nameField);
			fieldsPanel.add(new JLabel("Host:"));
			fieldsPanel.add(hostField);
			fieldsPanel.add(new JLabel("Port:"));
			fieldsPanel.add(portField);

			JButton joinButton = new JButton("Join Game");
			joinButton.addActionListener(e -> {
				String name = nameField.getText().trim();
				String host = hostField.getText().trim();
				String portText = portField.getText().trim();

				if (name.isEmpty() || host.isEmpty() || portText.isEmpty()) {
					JOptionPane.showMessageDialog(joinFrame, "Name, host, and port are required.",
							"Missing Fields", JOptionPane.ERROR_MESSAGE);
					return;
				}

				try {
					int port = Integer.parseInt(portText);
					new Client(host, port, name);
					joinFrame.dispose();
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(joinFrame, "Port must be a number.",
							"Invalid Port", JOptionPane.ERROR_MESSAGE);
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(joinFrame, "Could not connect to server: " + ex.getMessage(),
							"Connection Failed", JOptionPane.ERROR_MESSAGE);
				}
			});

			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			buttonPanel.add(joinButton);

			joinFrame.add(fieldsPanel, BorderLayout.CENTER);
			joinFrame.add(buttonPanel, BorderLayout.SOUTH);
			joinFrame.setVisible(true);
		});
	}
}
