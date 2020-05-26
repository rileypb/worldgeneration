package brainfreeze.world;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;

public class GraphDrawLayer implements DrawLayer {
	private Graphs graphs;
	private Double bounds;

	public GraphDrawLayer(Graphs graphs, Double bounds) {
		this.graphs = graphs;
		this.bounds = bounds;
	}

	@Override
	public void draw(BufferedImage im) {
		Graphics2D g = (Graphics2D) im.getGraphics();
		Rectangle clipBounds = g.getDeviceConfiguration().getBounds();

		double x0 = clipBounds.x;
		double y0 = clipBounds.y;
		double xWidth = clipBounds.width * 1;
		double yHeight = clipBounds.height * 1;
		
		g.fillRect((int)x0, (int)y0, (int)xWidth, (int)yHeight);
		
		g.setColor(Color.blue);
		g.setStroke(new BasicStroke(1));
		g.drawRect((int)(x0 + xWidth * bounds.x), (int)(y0 + yHeight * bounds.y), (int)(xWidth * bounds.width), (int) (yHeight * bounds.height));

		//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(new BasicStroke(1));

		g.setColor(Color.red);
		graphs.voronoiEdges.forEach((e) -> {
			g.drawLine((int) (x0 + xWidth * e.loc1.x), (int) (y0 + yHeight * e.loc1.y), (int) (x0 + xWidth * e.loc2.x),
					(int) (y0 + yHeight * e.loc2.y));

		});

		graphs.dualVertices.forEach((loc) -> {
			g.drawOval((int) (x0 + xWidth * loc.x), (int) (y0 + yHeight * loc.y), 4, 4);
		});

	}

	private double minmax(double min, double max, double value) {
		return Math.min(max, Math.max(min, value));
	}

}
