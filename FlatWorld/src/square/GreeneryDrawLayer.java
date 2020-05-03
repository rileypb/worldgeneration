package square;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class GreeneryDrawLayer implements DrawLayer {

	private double sealevel;

	public GreeneryDrawLayer(double sealevel) {
		this.sealevel = sealevel;
	}

	@Override
	public void draw(Graphics2D graphics, Terrain terrain, double x0, double y0, double xWidth, double yHeight) {
		Random rnd = new Random();
		
		Color greenest = new Color(35, 44, 17);
		Color brownest = new Color(252, 248, 190);
		
		double elevation = 0;
		for (int x = 0; x < terrain.numHorizontalSamples; x++) {
			for (int y = 0; y < terrain.numVerticalSamples; y++) {
				elevation = Math.min(1, Math.max(-1, terrain.getElevation(x, y) - sealevel));
				if (elevation > 0) {
					double moisture = terrain.getMoisture(x, y);
					float adjustedMoisture = Math.min(1, (float) moisture + 0.2f);
					int r = (int) (adjustedMoisture*greenest.getRed() + (1-adjustedMoisture)*brownest.getRed());
					int g = (int) (adjustedMoisture*greenest.getGreen() + (1-adjustedMoisture)*brownest.getGreen());
					int b = (int) (adjustedMoisture*greenest.getBlue() + (1-adjustedMoisture)*brownest.getBlue());
					r += rnd.nextInt(30)-15;
					g += rnd.nextInt(30)-15;
					b += rnd.nextInt(30)-15;
					Color color = new Color(r,g,b);
					graphics.setColor(color);
					graphics.drawRect(x, y, 1, 1);
				}
			}
		}
	}

}
