package square;
import java.awt.Color;
import java.awt.Graphics2D;

public class SnowDrawLayer implements DrawLayer {

	private double sealevel;

	public SnowDrawLayer(double sealevel) {
		this.sealevel = sealevel;
	}

	@Override
	public void draw(Graphics2D graphics, Terrain terrain, double x0, double y0, double xWidth, double yHeight) {

		double elevation = 0;
		for (int x = 0; x < terrain.numHorizontalSamples; x++) {
			for (int y = 0; y < terrain.numVerticalSamples; y++) {
				elevation = Math.min(1, Math.max(-1, terrain.getElevation(x, y) - sealevel));
				if (elevation > 0 && terrain.getBiome(x, y) == Biome.SNOW) {
					graphics.setColor(terrain.getBiome(x, y).color);
					graphics.drawRect(x, y, 1, 1);
				}
			}
		}
	}

}
