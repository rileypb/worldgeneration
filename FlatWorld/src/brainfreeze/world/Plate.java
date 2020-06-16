package brainfreeze.world;

import java.util.ArrayList;
import java.util.List;

public class Plate {
	
	public static enum PlateType {
		OCEAN,
		LAND;
	}

	public List<Location> cells = new ArrayList<>();
	public PlateType type;
	public double growthFactor;
	public Vector movement;
	public double height;

}
