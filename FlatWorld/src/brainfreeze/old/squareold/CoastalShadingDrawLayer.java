package brainfreeze.old.squareold;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class CoastalShadingDrawLayer implements DrawLayer {

	private double sealevel;

	public CoastalShadingDrawLayer(double sealevel) {
		this.sealevel = sealevel;
	}

	@Override
	public void draw(Graphics2D graphics, Terrain terrain, double x0, double y0, double xWidth, double yHeight) {
		double elevation = 0;
		Color parchment = new Color(144, 126, 96);
		Color parchment2 = new Color(120, 100, 80);
		for (int x = 0; x < terrain.numHorizontalSamples; x++) {
			for (int y = 0; y < terrain.numVerticalSamples; y++) {
				elevation = Math.min(1, Math.max(-1, terrain.getElevation(x, y) - sealevel));
				Color color;
				if (elevation > 0) {
					int distanceFromCoast = terrain.getDistanceFromCoast(x, y);
					System.out.println(distanceFromCoast);
					if (distanceFromCoast > 0 && distanceFromCoast <= 10) {
						// 144, 126, 96
						color = new Color((int)(144f * distanceFromCoast / 10f), (int)(126f * distanceFromCoast / 10f),
								(int)(96f * distanceFromCoast / 10f));
					} else if (distanceFromCoast == 0) {
						color = parchment;
					} else {
						color = parchment;
					}
					graphics.setColor(color);
					graphics.drawRect(x, y, 1, 1);
				} else {
					color = parchment2;
				}
			}
		}
	}

}
