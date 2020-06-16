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

	private final double xScale;
	private double yScale;
	private boolean horizontalRepeat;

	public GraphDrawLayer(Graphs graphs, Double bounds) {
		this(graphs, bounds, 1, 1, false);
	}

	public GraphDrawLayer(Graphs graphs, Double bounds, double xScale, double yScale, boolean horizontalRepeat) {
		this.graphs = graphs;
		this.bounds = bounds;
		this.xScale = xScale;
		this.yScale = yScale;
		this.horizontalRepeat = horizontalRepeat;
	}

	@Override
	public void draw(BufferedImage im) {
		Graphics2D g = (Graphics2D) im.getGraphics();
		Rectangle clipBounds = g.getDeviceConfiguration().getBounds();

		double x0 = clipBounds.x;
		double y0 = clipBounds.y;
		double xWidth = clipBounds.width * 1 / xScale;
		double yHeight = clipBounds.height * 1 / yScale;

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

		graphs.dualVertices.forEach((loc) -> {
			if (loc.plateIndex > -1) {
//				Color c = new Color(loc.plateIndex*10,loc.plateIndex*10,loc.plateIndex*10);
//				g.setColor(c);
//				switch (loc.plate.type) {
//				case OCEAN:
//					g.setColor(Color.blue);
//					break;
//				case LAND:
//					g.setColor(Color.green);
//					break;
//				}
//				if (loc.tectonicStress > 0.75) {
//					g.setColor(Color.green);
//				} else if (loc.tectonicStress < -0.75) {
//					g.setColor(Color.blue);
//				}
				
				double totalHeight = loc.elevation; //loc.baseElevation + Math.max(0, loc.tectonicStress);
				if (totalHeight > 0) {
//					g.setColor(Color.GREEN);
					float c = (float) minmax(0, 1, totalHeight);
					g.setColor(new Color(c,c,c));
				} else {
					g.setColor(Color.BLUE);
				}
				
				if (loc.tectonicStress > 0.25 && totalHeight > 0) {
					g.setColor(Color.red);
				}
				
				
				
				loc.sides.forEach((side) -> {
					Path2D.Double p = new Path2D.Double();
					p.moveTo(x0 + xWidth * loc.getX(), y0 + yHeight * loc.getY());
					p.lineTo(x0 + xWidth * side.loc1.getX(), y0 + yHeight * side.loc1.getY());
					p.lineTo(x0 + xWidth * side.loc2.getX(), y0 + yHeight * side.loc2.getY());
					g.fill(p);
				});
				if (horizontalRepeat) {
					loc.sides.forEach((side) -> {
						Path2D.Double p = new Path2D.Double();
						p.moveTo(x0 + xWidth * loc.getX() + xWidth * bounds.width, y0 + yHeight * loc.getY());
						p.lineTo(x0 + xWidth * side.loc1.getX() + xWidth * bounds.width, y0 + yHeight * side.loc1.getY());
						p.lineTo(x0 + xWidth * side.loc2.getX() + xWidth * bounds.width, y0 + yHeight * side.loc2.getY());
						g.fill(p);
					});
					loc.sides.forEach((side) -> {
						Path2D.Double p = new Path2D.Double();
						p.moveTo(x0 + xWidth * loc.getX() - xWidth * bounds.width, y0 + yHeight * loc.getY());
						p.lineTo(x0 + xWidth * side.loc1.getX() - xWidth * bounds.width, y0 + yHeight * side.loc1.getY());
						p.lineTo(x0 + xWidth * side.loc2.getX() - xWidth * bounds.width, y0 + yHeight * side.loc2.getY());
						g.fill(p);
					});
				}
			}
		});

//		graphs.dualVertices.forEach((loc) -> {
//			if (loc.boundaryLocation) {
//				g.setColor(Color.blue);
//			} else {
//				g.setColor(Color.red);
//			}
//			g.drawOval((int) (x0 + xWidth * loc.getX()) - 2, (int) (y0 + yHeight * loc.getY()) - 2, 4, 4);
//		});

//		graphs.voronoiVertices.forEach((loc) -> {
//			if (loc.boundaryLocation) {
//				g.setColor(Color.blue);
//			} else {
//				g.setColor(Color.red);
//			}
//			g.drawOval((int) (x0 + xWidth * loc.getX()) - 2, (int) (y0 + yHeight * loc.getY()) - 2, 4, 4);
//		});
	}

	private double minmax(double min, double max, double value) {
		return Math.min(max, Math.max(min, value));
	}

}
