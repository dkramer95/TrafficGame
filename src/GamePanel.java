import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable, MouseListener {
	private static GamePanel instance = null;	//singleton instance
	private static Font font = new Font("Courier New", Font.BOLD, 20);
	
	private BufferedImage bgImg;
	private Graphics2D g2d;
	private ArrayList<Car> cars;
	private ArrayList<CarSpawner> carSpawners;
	private Rectangle collisionRect;
	
	// game thread
	private Thread thread;
	private boolean running;
	private int FPS = 60;
	private long targetTime = 1000 / FPS;
	
	private boolean gameOver;
	private boolean gamePaused;
	private String scoreStr;
	private int score;
	
	public GamePanel() {
		bgImg = null;
		
		try {
			bgImg = ImageIO.read(new File("res/Intersection.jpg"));
		} catch (IOException e) {}
		
		score = 0;
		scoreStr = "";
		cars = new ArrayList<>();
		carSpawners = new ArrayList<>();
		
		carSpawners.add(new CarSpawner(cars, Road.NORTH_BOUND, this));
		carSpawners.add(new CarSpawner(cars, Road.SOUTH_BOUND, this));
		carSpawners.add(new CarSpawner(cars, Road.EAST_BOUND, this));
		carSpawners.add(new CarSpawner(cars, Road.WEST_BOUND, this));
		
		gameOver = false;
		gamePaused = false;
		addMouseListener(this);
		
		init();
		instance = this;
	}
	
	/**
	 * 
	 * @return the singleton instance of this class.
	 */
	public static GamePanel getInstance() {
		if (instance == null) {
			instance = new GamePanel();
		}
		return instance;
	}
	
	/**
	 * Initializes the thread and starts the game.
	 */
	private void init() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
			System.out.println("Thread started!");
		}
		running = true;
	}
	
	/**
	 * Game loop.
	 */
	public void run() {
		System.out.println("run()");
		init();

		long start;
		long elapsed;
		long wait;

		while (running) {
			start = System.nanoTime();
			if (!gamePaused) {
				if (!gameOver) {
					try {
						update();
					} catch (ConcurrentModificationException e) {}
				} else {
					setSpawning(false);	// don't generate anymore!
				}
				
				repaint();

				elapsed = System.nanoTime() - start;

				wait = targetTime - elapsed / 1000000;

				if (wait < 0) {
					wait = 5;
				}

				try {
					Thread.sleep(wait);
				} catch (InterruptedException e) {}	
			}
			
		}
	}
	
	private void setSpawning(boolean b) {
		for (CarSpawner cs : carSpawners) {
			if (b) {
				cs.getSpawnTimer().start();
			} else {
				cs.getSpawnTimer().stop();	
			}
		}
	}
	
	public void paintComponent(Graphics g) {
		g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.clearRect(0, 0, getWidth(), getHeight());
		g2d.drawImage(bgImg, 0, 0, null);
		g2d.setFont(font);
		g2d.drawString(scoreStr, 450, 20);
		
		if (gamePaused) {
			g2d.setFont(new Font("Courier New", Font.BOLD, 50));
			g2d.setColor(Color.WHITE);
			g2d.drawString("PAUSED", 400, 80);
		}
		render();
		
		if (gameOver) {
			renderGameOver();
		}
		g2d.dispose();
	}
	
	/**
	 * Draws a box around the collision area.
	 */
	public void renderGameOver() {
		// fade out scene
		g2d.setColor(new Color(0, 0, 0, 150));
		g2d.fillRect(0, 0, getWidth(), getHeight());
		// draw red outline
		g2d.setColor(new Color(255, 0, 0, 200));
		g2d.setStroke(new BasicStroke(5.0f));
		g2d.drawRect(collisionRect.x - 75, collisionRect.y - 50, collisionRect.width + 100, collisionRect.height + 100);
		// draw highlight
		g2d.setColor(new Color(255, 255, 255, 30));
		g2d.fillRect(collisionRect.x - 75, collisionRect.y - 50, collisionRect.width + 100, collisionRect.height + 100);
		// Render final score to the screen.
		g2d.setFont(new Font("Courier New", Font.BOLD, 50));
		int offsetWidth = g2d.getFontMetrics().stringWidth(scoreStr);
		g2d.setColor(Color.WHITE);
		g2d.drawString(scoreStr, (getWidth() - offsetWidth) / 2 , 650);
		
	}
	
	public synchronized void render() {
		for (Car car : cars) {
			car.render(g2d);
		}
	}
	
	public void setIsRunning(boolean b) {
		running = b;
	}
	
	public void incrementScore() {
		score++;
		scoreStr = "Score: " + score;
	}
	
	/**
	 * Updates all cars.
	 */
	public synchronized void update() {
		for (Car car : cars) {
			try {
				checkCollision(car);
				car.update();	
			} catch (ConcurrentModificationException e) {}
		}
	}
	
	/**
	 * Checks for collision for all cars that are within the bounds of the world.
	 * @param car
	 */
	public synchronized void checkCollision(Car car) {
		Rectangle bounds = getBounds();	// only check collision if cars are within bounds
		for (int i = 0; i < cars.size(); i++) {
			Car car2Check = cars.get(i);
			
			if (car2Check != car) {
				if (car.intersects(car2Check) && bounds.contains(car) && bounds.contains(car2Check)) {
					System.out.println("COLLISION OCCURRED!");
					gameOver = true;
					collisionRect = (Rectangle)car.createUnion(car2Check);	// rectangle around collision area
					
					repaint();
					TrafficFrame.getInstance().reset();	//reset the game
					return;
				}
			} 
		}
	}
	
	public int getScore() {
		return score;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public boolean isGamedPaused() {
		return gamePaused;
	}

	public void mousePressed(MouseEvent e) {
		for (Car car : cars) {
			car.mousePress(e);
		}
	}
	
	// Unused mouse methods
	public void mouseClicked(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

}
