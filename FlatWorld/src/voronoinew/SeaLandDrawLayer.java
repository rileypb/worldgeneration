package voronoinew;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class SeaLandDrawLayer implements DrawLayer {

	@Override
	public void draw(Graphics2D g, Graphs graphs, BufferedImage im) {
		Rectangle clipBounds = g.getDeviceConfiguration().getBounds();

		double x0 = clipBounds.x - 20;
		double y0 = clipBounds.y - 20;
		double xWidth = clipBounds.width * 1.1;
		double yHeight = clipBounds.height * 1.1;

		//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		g.setColor(Color.BLACK);
		graphs.dualVertices.forEach((loc) -> {
			List<MapEdge> edgeList = graphs.dualGraph.edgesOf(loc).stream().map((dualEdge) -> {
				return graphs.dualToVoronoi.get(dualEdge);
			}).collect(Collectors.toList());

			edgeList.forEach((e) -> {
				if (e != null) {
					Path2D.Double p = new Path2D.Double();
					p.moveTo(x0 + xWidth * loc.x, y0 + yHeight * minmax(0, 1, loc.y));
					p.lineTo(x0 + xWidth * e.loc1.x, y0 + yHeight * minmax(0, 1, e.loc1.y));
					p.lineTo(x0 + xWidth * e.loc2.x, y0 + yHeight * minmax(0, 1, e.loc2.y));
					p.closePath();
					//					System.out.println(e.elevation + ": " + e.loc1.water);
					if (loc.water) {
						g.setColor(Color.lightGray);
					} else {
						g.setColor(Color.white);
						if (loc.elevation > 0.5) {
							g.setColor(Color.red);
						} else if (loc.elevation > 0.25) {
							g.setColor(Color.PINK);
						}
//						float c = (float) Math.max(0, Math.min(1, 0.5+loc.elevation));
//						g.setColor(new Color(c,c,c));
					}
					g.fill(p);
				}
			});
		});

		//		g.setStroke(new BasicStroke(5));
		//		graphs.voronoiEdges.forEach((e) -> {
		//
		//			if (e.river && e.flux > 10) {
		//				g.setColor(Color.BLUE);
		//				g.drawLine((int) (x0 + xWidth * e.loc1.x), (int) (y0 + yHeight * e.loc1.y),
		//						(int) (x0 + xWidth * e.loc2.x), (int) (y0 + yHeight * e.loc2.y));
		//
		//				if (e.loc1.riverJuncture) {
		//					Set<MapEdge> incomingEdges = graphs.riverGraph.incomingEdgesOf(e.loc1);
		//					double min = incomingEdges.stream().mapToDouble((edge) -> {
		//						return edge.flux;
		//					}).min().orElse(0);
		//					if (min > 10 && e.loc1.elevation > MapperMain.SEALEVEL) {
		//						g.setColor(Color.RED);
		//						int x = (int) (x0 + e.loc1.x * xWidth) - 3;
		//						int y = (int) (y0 + e.loc1.y * yHeight) - 3;
		//						g.drawOval(x, y, 6, 6);
		//					}
		//				} else {
		//					MapEdge incomingEdge = null;
		//					MapEdge outgoingEdge = null;
		//					Set<MapEdge> incomingEdges = graphs.riverGraph.incomingEdgesOf(e.loc1);
		//					if (!incomingEdges.isEmpty()) {
		//						incomingEdge = incomingEdges.iterator().next();
		//					}
		//					Set<MapEdge> outgoingEdges = graphs.riverGraph.outgoingEdgesOf(e.loc1);
		//					if (!outgoingEdges.isEmpty()) {
		//						outgoingEdge = outgoingEdges.iterator().next();
		//					}
		//					if (outgoingEdge != null && incomingEdge != null) {
		//						
		//					}
		//				}
		//			}
		//		});

		g.setColor(Color.lightGray);
		g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		if (graphs.riverPaths != null) {
			graphs.riverPaths.forEach((path) -> {
				Path2D.Double p = new Path2D.Double();
				boolean drawn = false;
				for (int i = 0; i < path.size(); i++) {
					System.out.println(">>> " + path.getScore(i) + ", " + path.getElevation(i));
					if (path.getScore(i) > 25) {
						if (!drawn) {
							p.moveTo(x0 + path.getX(i) * xWidth, y0 + path.getY(i) * yHeight);
							drawn = true;
						} else if (drawn) {
							p.lineTo(x0 + path.getX(i) * xWidth, y0 + path.getY(i) * yHeight);
						}
					}
				}
				if (drawn) {
					g.draw(p);
				}
			});
		}

		//		g.setColor(Color.lightGray);
		//		graphs.voronoiVertices.forEach((loc) -> {
		//			List<MapEdge> edgeList = graphs.voronoiGraph.edgesOf(loc).stream().map((dualEdge) -> {
		//				return graphs.voronoiToDual.get(dualEdge);
		//			}).collect(Collectors.toList());
		//
		//			edgeList.forEach((e) -> {
		//				Path2D.Double p = new Path2D.Double();
		//				p.moveTo(x0 + xWidth/2 * loc.x, y0 + yHeight * minmax(0, 1, loc.y));
		//				p.lineTo(x0 + xWidth/2 * e.loc1.x, y0 + yHeight * minmax(0, 1, e.loc1.y));
		//				p.lineTo(x0 + xWidth/2 * e.loc2.x, y0 + yHeight * minmax(0, 1, e.loc2.y));
		//				p.closePath();
		//
		//				g.draw(p);
		//			});
		//		});

		//		graphs.voronoiVertices.forEach((loc) -> {
		//			List<MapEdge> edgeList = graphs.voronoiGraph.edgesOf(loc).stream().map((dualEdge) -> {
		//				return graphs.voronoiToDual.get(dualEdge);
		//			}).collect(Collectors.toList());
		//
		//			edgeList.forEach((e) -> {
		//				Path2D.Double p = new Path2D.Double();
		//				p.moveTo(x0 + xWidth * minmax(0, 1, loc.x) + xWidth, y0 + yHeight * minmax(0, 1, loc.y));
		//				p.lineTo(x0 + xWidth * minmax(0, 1, e.loc1.x) + xWidth, y0 + yHeight * minmax(0, 1, e.loc1.y));
		//				p.lineTo(x0 + xWidth * minmax(0, 1, e.loc2.x) + xWidth, y0 + yHeight * minmax(0, 1, e.loc2.y));
		//				p.closePath();
		//
		//				if (loc.elevation >= 0) {
		//					g.setColor(new Color((float) loc.red, (float) loc.green, (float) loc.blue));
		//				} else {
		//					g.setColor(Color.blue);
		//				}
		//				g.fill(p);
		//			});
		//		});

		//		g.setColor(Color.black);
		//		graphs.dualVertices.forEach((loc) -> {
		//			if (loc.elevation > .5) {
		//
		//				List<MapEdge> edgeList = graphs.dualGraph.edgesOf(loc).stream().map((dualEdge) -> {
		//					return graphs.dualToVoronoi.get(dualEdge);
		//				}).collect(Collectors.toList());
		//
		//				edgeList.forEach((e) -> {
		//					if (e != null) {
		//						g.drawLine((int) (loc.x * xWidth), (int) (loc.y * yHeight), (int) (e.loc1.x * xWidth),
		//								(int) (e.loc1.y * yHeight));
		//						g.drawLine((int) (loc.x * xWidth), (int) (loc.y * yHeight), (int) (e.loc2.x * xWidth),
		//								(int) (e.loc2.y * yHeight));
		//					}
		//				});
		//			}
		//		});

		//		g.setColor(Color.black);
		//		graphs.voronoiEdges.forEach((edge) -> {
		//			//			if (Math.abs(edge.loc1.x - edge.loc2.x) > 0.001 && Math.abs(edge.loc1.y - edge.loc2.y) > 0.005
		//			//					) {//&& Math.abs(edge.loc1.x - edge.loc2.x) < 0.05 && Math.abs(edge.loc1.y - edge.loc2.y) < 0.05) {
		//			g.drawLine((int) (x0 + xWidth /2* edge.loc1.x), (int) (y0 + yHeight * minmax(0,1,edge.loc1.y)),
		//					(int) (x0 + xWidth /2* edge.loc2.x), (int) (y0 + yHeight * minmax(0,1,edge.loc2.y)));
		//			//			}
		//		});

		//		g.setStroke(new BasicStroke(3, 0, 0));
		//		g.setColor(Color.red);
		//		graphs.voronoiEdges.forEach((edge) -> {
		//			if (edge.river) {
		//				g.setColor(edge.riverColor);
		//				g.drawLine((int) (x0 + xWidth * edge.loc1.x), (int) (y0 + yHeight * edge.loc1.y),
		//						(int) (x0 + xWidth * edge.loc2.x), (int) (y0 + yHeight * edge.loc2.y));
		//			}
		//		});

	}

	private double minmax(double min, double max, double value) {
		return Math.min(max, Math.max(min, value));
	}

}
