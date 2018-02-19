import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.Timer;

/**
 * This class spawns cars with a random lane direction at random intervals.
 * @author David Kramer
 *
 */
public class CarSpawner implements ActionListener {
	private GamePanel gamePanel;
	private ArrayList<Car> cars;	// target array to add cars to
	private Timer spawnTimer;
	private Random rng;
	private int randDelay;
	private int direction;
			
	public CarSpawner(ArrayList<Car> cars, int direction, GamePanel gamePanel) {
		this.cars = cars;
		this.direction = direction;
		this.gamePanel = gamePanel;
		
		rng = new Random();
		
		int randStart = rng.nextInt(1000) + 2800;
		spawnTimer = new Timer(randStart, this);
		spawnTimer.setInitialDelay(randStart);
		spawnTimer.start();
	}
	
	/**
	 * Called by the spawn timer. This will randomly adjust the spawn frequency
	 * and create random cars.
	 */
	public void actionPerformed(ActionEvent e) {
		
		if (!gamePanel.isGamedPaused()) {	// only spawn when game isn't paused
			double randSpeed = rng.nextDouble() + 1.2;
			
			switch (direction) {
			case Road.NORTH_BOUND:
				cars.add(new Car(randSpeed, direction, getRandomColor()));
				break;
			case Road.SOUTH_BOUND:
				cars.add(new Car(randSpeed, direction, getRandomColor()));
				break;
			case Road.EAST_BOUND:
				cars.add(new Car(randSpeed, direction, getRandomColor()));
				break;
			case Road.WEST_BOUND:
				cars.add(new Car(randSpeed, direction, getRandomColor()));
				break;
			}	
			
			randDelay = rng.nextInt(2200) + 3100;
			spawnTimer.setDelay(randDelay);
		}
	}
	
	/**
	 * 
	 * @return random RGB color
	 */
	public Color getRandomColor() {
		Random rng = new Random();
		
		int red = rng.nextInt(200) + 40;
		int green = rng.nextInt(200) + 40;
		int blue = rng.nextInt(200) + 40;
		
		return new Color(red, green, blue);
	}
	
	public Timer getSpawnTimer() {
		return spawnTimer;
	}
	
}
