package brainfreeze.world;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

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

		double x0 = clipBounds.x + 0;
		double y0 = clipBounds.y + 0;
		double xWidth = clipBounds.width * 1;
		double yHeight = clipBounds.height * 1;

		//		g.fillRect((int)x0, (int)y0, (int)xWidth, (int)yHeight);

		g.setColor(Color.blue);
		g.setStroke(new BasicStroke(1));
		g.drawRect((int) (x0 + xWidth * bounds.x), (int) (y0 + yHeight * bounds.y), (int) (xWidth * bounds.width),
				(int) (yHeight * bounds.height));

		//		g.drawLine((int)(x0 + xWidth * 0.5), (int) y0, (int)(x0 + xWidth), (int)(y0 + yHeight * 0.5));
		//		g.drawLine((int)(x0 + xWidth * 0.5), (int) (y0 + yHeight * 1), (int)(x0 + xWidth), (int)(y0 + yHeight * 0.5));
		//		g.drawLine((int)(x0 + xWidth * 0.5), (int) y0, (int)(x0), (int)(y0 + yHeight * 0.5));
		//		g.drawLine((int)(x0 + xWidth * 0.5), (int) (y0 + yHeight * 1), (int)(x0), (int)(y0 + yHeight * 0.5));

		//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setStroke(new BasicStroke(1));

		//		graphs.dualVertices.forEach((loc) -> {
		//
		//			g.setColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
		//			List<MapEdge> edgeList = graphs.dualGraph.edgesOf(loc).stream().map((voronoiEdge) -> {
		//				return graphs.dualToVoronoi.get(voronoiEdge);
		//			}).collect(Collectors.toList());
		//			edgeList.forEach((e) -> {
		//				if (e != null) {
		//					Path2D.Double p = new Path2D.Double();
		//					p.moveTo(x0 + xWidth * loc.getX(), y0 + yHeight * loc.getY());
		//					p.lineTo(x0 + xWidth * e.loc1.getX(), y0 + yHeight * e.loc1.getY());
		//					p.lineTo(x0 + xWidth * e.loc2.getX(), y0 + yHeight * e.loc2.getY());
		//					p.closePath();
		////
		//					g.fill(p);
		//					System.out.println("??? " + e);
		////					g.setColor(Color.green);
		//					g.drawLine((int)(x0 + xWidth * e.loc1.getX()), (int)(y0 + yHeight * e.loc1.getY()), 
		//							(int)(x0 + xWidth * e.loc2.getX()), (int) (y0 + yHeight * e.loc2.getY()));
		//				}
		//			});
		//		});

		g.setColor(Color.red);
		graphs.voronoiEdges.forEach((e) -> {
			g.drawLine((int) (x0 + xWidth * e.loc1.getX()), (int) (y0 + yHeight * e.loc1.getY()),
					(int) (x0 + xWidth * e.loc2.getX()), (int) (y0 + yHeight * e.loc2.getY()));

		});

//		graphs.dualVertices.forEach((loc) -> {
//			if (loc.boundaryLocation) {
//				g.setColor(Color.blue);
//			} else {
//				g.setColor(Color.red);
//			}
//			g.drawOval((int) (x0 + xWidth * loc.getX()) - 2, (int) (y0 + yHeight * loc.getY()) - 2, 4, 4);
//		});

		graphs.voronoiVertices.forEach((loc) -> {
			if (loc.boundaryLocation) {
				g.setColor(Color.blue);
			} else {
				g.setColor(Color.red);
			}
			g.drawOval((int) (x0 + xWidth * loc.getX()) - 2, (int) (y0 + yHeight * loc.getY()) - 2, 4, 4);
		});
	}

	private double minmax(double min, double max, double value) {
		return Math.min(max, Math.max(min, value));
	}

}
