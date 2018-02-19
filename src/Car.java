import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.SwingUtilities;

public class Car extends Rectangle {
	// These can alternative, depending on the travel direction.
	public static final int WIDTH = 60;
	public static final int HEIGHT = 20;
	public static final int SPAWN_OFFSET = 60;
	
	public static final int MAX_STOP_COUNT = 3;		// car can only stop max 3 times
	public static final int MAX_STOP_TIME = 3000;	// 3 second max wait time
	
	// Speed Constants
	public static final double MIN_SPEED = 1.0;
	public static final double MAX_SPEED = 8.0;
	
	// Appearance
	private Color color;
	
	private GamePanel gamePanel;
	
	//Flags
	private boolean isStopped;		// flag if car is not currently moving
	private boolean isAccelerating;	// flag if acceleration is being applied to movement
	private boolean didAddToScore;	// has this car already been added to the score?
	
	private double speed;			// how fast is car moving
	private double acceleration;	// speed boost factor applied to the car
	private int moveDirection;		// is car moving N-S, E-W or vice versa
	private int stopCount;			// how many times has car been stopped?
	
	// stop control
	private long firstStopTime;	
	private long currentStopTime;
	private long stopTimeDiff;		
	
	public Car (double speed, int moveDirection, Color color) {
		this.speed = speed;
		this.moveDirection = moveDirection;
		this.color = color;
		
		Random rng = new Random();
		acceleration = 0.5 + rng.nextDouble();
		gamePanel = GamePanel.getInstance();
		init();
	}
	
	/**
	 * Initializes the starting location of the car and also determines how to
	 * setup the rectangle, depending on the direction it is traveling.
	 */
	private void init() {
		switch (moveDirection) {
		case Road.NORTH_BOUND:
			x = 330;
			y = gamePanel.getHeight() + SPAWN_OFFSET;
			break;
		case Road.SOUTH_BOUND:
			x = 250;
			y = 0 - SPAWN_OFFSET;
			break;
		case Road.EAST_BOUND:
			y = 430;
			x = 0 - SPAWN_OFFSET;
			break;
		case Road.WEST_BOUND:
			x = gamePanel.getWidth() + SPAWN_OFFSET;
			y = 350;
			break;
		}
		width = WIDTH;
		height = HEIGHT;

		if (moveDirection == Road.NORTH_BOUND || moveDirection == Road.SOUTH_BOUND) {	// flip orientation
			width = HEIGHT;
			height = WIDTH;
		}
	}
	
	/**
	 * Stops the car. If the amount of stops the car has made is greater than 
	 * the maximum allowed, no action is performed. 
	 */
	public void stop() {
		stopCount++;
		
		if(isAccelerating) {
			speed -= acceleration;	// remove acceleration influence
			if (speed <= 1.0) {
				speed = MIN_SPEED;
			}
			isAccelerating = false;
		}
		
		if (stopCount > MAX_STOP_COUNT) {
			go();
		} else {
			isStopped = true;
			System.out.println("Car stopped!");
		}
		
	}
	
	public void go() {
		isStopped = false;
	}
	
	/**
	 * Applies acceleration to the car. If the car is not moving, it just calls
	 * the go() method and no acceleration is applied.
	 */
	public void accelerate() {
		if (isStopped) {
			go();
			return;
		}
		
		acceleration += speed * acceleration;
		speed += acceleration;
		
		if (speed > MAX_SPEED) {
			speed = MAX_SPEED;
		}
		isAccelerating = true;
	}
	
	/**
	 * Toggle between stop and go.
	 */
	public void toggle() {
		if (isStopped) {
			go();
		} else {
			stop();
		}
	}
	
	/**
	 * Renders car to the screen.
	 * @param g2d
	 */
	public void render(Graphics2D g2d) {
		g2d.setStroke(new BasicStroke(4.0f));
		if (isStopped) {
			g2d.setColor(Color.RED);	// draw red outline to let user know car is stopped
		} else {
			g2d.setColor(Color.BLACK);	
		}
		g2d.drawRect(x, y, width, height);
		g2d.setColor(color);
		g2d.fillRect(x, y, width, height);
	}
	
	public void mousePress(MouseEvent e) {
		if (contains(e.getPoint())) {
			if (SwingUtilities.isRightMouseButton(e)) {
				toggle();
			} else {
				accelerate();
			}
		}
	}
	
	/**
	 * Updates the amount of the time that the car has been stopped for
	 * recently. If the current stop time is more than the maximum stop
	 * time, the car will start to move again.
	 */
	private void updateStopTime() {
		if (firstStopTime == 0) {
			firstStopTime = System.currentTimeMillis();
		}
		
		currentStopTime = System.currentTimeMillis();
		stopTimeDiff = currentStopTime - firstStopTime;
		
		if (stopTimeDiff > MAX_STOP_TIME) {
			go();
			resetStopTime();
		}
	}
	
	/**
	 * Checks to see if the car is outside the bounds. If it is, score is incremented.
	 */
	private void updateScore() {
		if (x < -SPAWN_OFFSET || x > gamePanel.getWidth() + SPAWN_OFFSET
				|| y < - SPAWN_OFFSET || y > gamePanel.getHeight() + SPAWN_OFFSET) {	// out of bounds x or y
			
			if (!didAddToScore) {
				gamePanel.incrementScore();
				didAddToScore = true;
			}
		}
	}
	
	/**
	 * Reset all stop time variables to 0.
	 */
	private void resetStopTime() {
		firstStopTime = 0;
		currentStopTime = 0;
		stopTimeDiff = 0;
	}
	
	/**
	 * Update position of the car.
	 */
	public void update() {
		if (!isStopped) {
			switch (moveDirection) {
			case Road.NORTH_BOUND:
				y -= speed;
				break;
			case Road.SOUTH_BOUND:
				y += speed;
				break;
			case Road.EAST_BOUND:
				x += speed;
				break;
			case Road.WEST_BOUND:
				x -= speed;
				break;
			}
			updateScore();
		} else {
			updateStopTime();	// we are still stopped. add more time to stop time counter
		}
	}
	
	public boolean didAddToScore() {
		return didAddToScore;
	}
}
