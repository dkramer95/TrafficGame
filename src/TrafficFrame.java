import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.Timer;

public class TrafficFrame extends JFrame implements ActionListener {
	public static TrafficFrame instance = null;
	
	public static final Dimension SIZE = new Dimension(600, 800);
	private GamePanel gamePanel;
	private Timer resetTimer;		// timer to control the reset of a new draw panel
	
	public TrafficFrame() {
		gamePanel = new GamePanel();
		setContentPane(gamePanel);
		setTitle("Traffic Game by David Kramer");
		setSize(SIZE);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
		instance = this;
	}
	
	public static TrafficFrame getInstance() {
		if (instance == null) {
			instance = new TrafficFrame();
		}
		return instance;
	}
	
	/**
	 * Resets the game after a 2.5 second delay, after a collision within the
	 * game has occurred.
	 */
	public void reset() {
		System.out.println("Resetting......");
		resetTimer = new Timer(2500, this);
		resetTimer.setRepeats(false);
		resetTimer.start();
	}
	
	/**
	 * Method called from timer that sets the content pane of the game
	 * to a new instance of draw panel.
	 */
	public void actionPerformed(ActionEvent e) {
		getContentPane().remove(gamePanel);
		this.gamePanel = new GamePanel();
		setContentPane(gamePanel);
		gamePanel.setFocusable(true);
		gamePanel.requestFocus();
		validate();
		repaint();
		resetTimer.stop();
	}
	
	public static void main(String[] args) {
		TrafficFrame app = new TrafficFrame();
	}
}
