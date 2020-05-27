package brainfreeze.world;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class DualGraphDrawLayer implements DrawLayer {

	private Graphs graphs;

	public DualGraphDrawLayer(Graphs graphs) {
		this.graphs = graphs;
	}
	
	@Override
	public void draw(BufferedImage im) {
		Graphics2D g = (Graphics2D) im.getGraphics();
		Rectangle clipBounds = g.getDeviceConfiguration().getBounds();

		double x0 = clipBounds.x;
		double y0 = clipBounds.y;
		double xWidth = clipBounds.width * 1;
		double yHeight = clipBounds.height * 1;

		//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(new BasicStroke(1));

		g.setColor(Color.blue);
		graphs.dualEdges.forEach((e) -> {
//			if (graphs.dualToVoronoi.get(e) == null) {
//				g.setColor(Color.red);
//			} else {
//				g.setColor(Color.lightGray);
//			}
			g.drawLine((int) (x0 + xWidth * e.loc1.getX()), (int) (y0 + yHeight * e.loc1.getY()), (int) (x0 + xWidth * e.loc2.getX()),
					(int) (y0 + yHeight * e.loc2.getY()));
		});


	}

	private double minmax(double min, double max, double value) {
		return Math.min(max, Math.max(min, value));
	}

}
