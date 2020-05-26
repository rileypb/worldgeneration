package brainfreeze.framework;

import java.awt.geom.Rectangle2D;

public class RegionParameters {

	public double xMin = 0;
	public double xMax = 1;
	public double yMin = 0;
	public double yMax = 1;
	
	public int numberOfPoints;
	
	public Rectangle2D.Double getBounds() {
		return new Rectangle2D.Double(xMin, yMin, xMax, yMax);
	}

}
