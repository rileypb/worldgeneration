package voronoinew;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GraphDrawLayer implements DrawLayer {

	@Override
	public void draw(Graphics2D g, Graphs graphs) {
		Rectangle clipBounds = g.getDeviceConfiguration().getBounds();

		double x0 = clipBounds.x + 20;
		double y0 = clipBounds.y + 20;
		double xWidth = clipBounds.width * 0.9;
		double yHeight = clipBounds.height * 0.9;
		System.out.println(xWidth);
		System.out.println(yHeight);

		//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(new BasicStroke(1));

		g.setColor(Color.BLUE);
		graphs.voronoiEdges.forEach((e) -> {
			g.drawLine((int) (x0 + xWidth * e.loc1.x), (int) (y0 + yHeight * e.loc1.y), (int) (x0 + xWidth * e.loc2.x),
					(int) (y0 + yHeight * e.loc2.y));
			
		});


	}

	private double minmax(double min, double max, double value) {
		return Math.min(max, Math.max(min, value));
	}

}