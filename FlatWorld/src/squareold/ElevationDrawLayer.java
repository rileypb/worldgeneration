package squareold;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

public class ElevationDrawLayer implements DrawLayer {

	private double sealevel;

	public ElevationDrawLayer(double sealevel) {
		this.sealevel = sealevel;
	}

	@Override
	public void draw(Graphics2D graphics, Terrain terrain, double x0, double y0, double xWidth, double yHeight) {

		double elevation = 0;
		for (int x = 0; x < terrain.width; x++) {
			for (int y = 0; y < terrain.height; y++) {
				elevation = Math.min(1, Math.max(-1, terrain.getElevation(x, y) - sealevel));
				if (elevation < 0) {
					graphics.setColor(Color.BLUE);
				} else {
					double elvpow = Math.pow(elevation, 1);
					graphics.setColor(new Color((float) (elvpow), (float) (1 - elvpow), 0));
				}
				graphics.drawRect(x, y, 1, 1);
			}
		}
	}

}
