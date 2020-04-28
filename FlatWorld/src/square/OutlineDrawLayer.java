package square;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

public class OutlineDrawLayer implements DrawLayer {

	private double sealevel;

	public OutlineDrawLayer(double sealevel) {
		this.sealevel = sealevel;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void draw(Graphics2D graphics, Terrain terrain) {
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setBackground(Color.white);
		graphics.setColor(Color.white);
		Rectangle clipBounds = graphics.getDeviceConfiguration().getBounds();
		graphics.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);

		double x0 = clipBounds.x - 0;
		double y0 = clipBounds.y - 0;
		double xWidth = clipBounds.width + 0;
		double yHeight = clipBounds.height + 0;

		double elevation = 0;
		for (int x = 0; x < terrain.numHorizontalSamples; x++) {
			for (int y = 0; y < terrain.numVerticalSamples; y++) {
				elevation = Math.min(1, Math.max(-1, terrain.getElevation(x, y) - sealevel));
				if (elevation < 0) {
//					graphics.setColor(new Color(128, 128, 255));
					graphics.setColor(Color.blue);
					//					graphics.setColor(new Color(0,0,(float)(1-Math.min(1, -elevation))));
				} else {
					boolean coast = false;
					double elev2;
					for (int i = -1; i < 2; i++) {
						for (int j = -1; j < 2; j++) {
							elev2 = Math.min(1, Math.max(-1, terrain.getElevation(x + i, y + j) - sealevel));
							if (elev2 < 0) {
								coast = true;
								break;
							}
						}
						if (coast) {
							break;
						}
					}
					int contourNumber = terrain.getContourNumber(x, y);
					boolean drawContour = terrain.getDrawContour(x, y);
					if (drawContour) {
						graphics.setColor(Color.black);
					} else if (coast) {
						graphics.setColor(Color.black);
					} else {
//												graphics.setColor(Color.lightGray);
//												graphics.setColor(new Color(64,128,64));
//						graphics.setColor(new Color((128 - contourNumber * 16), (192 - contourNumber * 16),
//								(128 - contourNumber * 16)));
						graphics.setColor(terrain.getBiome(x, y).color);
					}
				}
				graphics.drawRect(x, y, 1, 1);
			}
		}
	}

}
