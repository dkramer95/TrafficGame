import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * A road represents a 2 directional travel flow for cars. This would be
 * North to South and East to West.A road contains cars.
 * @author David Kramer
 *
 */
public class Road extends Rectangle {
	
	public static final int DIRECTION_VERTICAL 	 = 0;	// North and South
	public static final int DIRECTION_HORIZONTAL = 1;	// East and West
	
	public static final int NORTH_BOUND = 10;
	public static final int SOUTH_BOUND = 11;
	public static final int EAST_BOUND  = 12;
	public static final int WEST_BOUND  = 13;
	
	private ArrayList<Car> cars;
	
	private int direction;
	
	public Road(int direction) {
		cars = new ArrayList<>();
		this.direction = direction;
	}
}
