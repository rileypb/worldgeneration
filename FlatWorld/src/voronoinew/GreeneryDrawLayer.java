package voronoinew;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class GreeneryDrawLayer implements DrawLayer {

	private Color seaBlue = new Color(19, 18, 52);
	private Color anotherGreen = new Color(39, 50, 41);
	private Color greenest = new Color(60, 78, 57);
	private Color lightGreen = new Color(92, 112, 83);
	private Color medGreen = new Color(71, 100, 70);
	private Color medBrown = new Color(183, 142, 100);
	private Color brownest = new Color(252, 248, 190);
	private Random r;

	public GreeneryDrawLayer(Random r) {
		this.r = r;
	}

	@Override
	public void draw(Graphics2D g, Graphs graphs) {
		Rectangle clipBounds = g.getDeviceConfiguration().getBounds();

		double x0 = clipBounds.x - 20;
		double y0 = clipBounds.y - 20;
		double xWidth = clipBounds.width * 1.1;
		double yHeight = clipBounds.height * 1.1;

		//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		g.setColor(Color.BLACK);

		graphs.dualVertices.forEach((loc) -> {
			if (loc.water) {
				loc.baseColor = seaBlue;
			} else if (loc.mountain) {
				loc.baseColor = anotherGreen;
			} else {
				if (loc.moisture > 0) {
					loc.baseColor = greenest;
					if (loc.flux > 50) {
						loc.baseColor = medGreen;
					}
				} else {
					loc.baseColor = medBrown;
					if (loc.flux > 25) {
						loc.baseColor = medGreen;
					}
				}

			}
//			if (loc.elevation > 0.99 && loc.mountain) {
//				loc.baseColor = Color.white;
//			}
		});

		graphs.dualVertices.forEach((loc) -> {
			Set<Color> neighborColors = graphs.dualGraph.edgesOf(loc).stream().map((edge) -> {
				return edge.oppositeLocation(loc);
			}).map((v) -> {
				return v.baseColor;
			}).collect(Collectors.toSet());

			if (loc.water) {
				loc.color = seaBlue;
			} else {

				int numNeighbors = neighborColors.size();
				double red = 0;
				double green = 0;
				double blue = 0;
				for (Color c : neighborColors) {
					red += c.getRed();
					green += c.getGreen();
					blue += c.getBlue();
				}
				red = (red + 2 * numNeighbors * loc.baseColor.getRed()) / (3 * numNeighbors);
				green = (green + 2 * numNeighbors * loc.baseColor.getGreen()) / (3 * numNeighbors);
				blue = (blue + 2 * numNeighbors * loc.baseColor.getBlue()) / (3 * numNeighbors);
				loc.color = new Color((int) red, (int) green, (int) blue);
			}
		});

		graphs.dualVertices.forEach((loc) -> {
			if (loc.water) {
				return;
			}
			int red = (int) minmax(0, 255, loc.color.getRed() + r.nextInt(20) - 10);
			int green = (int) minmax(0, 255, loc.color.getGreen() + r.nextInt(20) - 10);
			int blue = (int) minmax(0, 255, loc.color.getBlue() + r.nextInt(20) - 10);
			loc.color = new Color(red, green, blue);
		});

		graphs.dualVertices.forEach((loc) -> {
			List<MapEdge> edgeList = graphs.dualGraph.edgesOf(loc).stream().map((voronoiEdge) -> {
				return graphs.dualToVoronoi.get(voronoiEdge);
			}).collect(Collectors.toList());

			edgeList.forEach((e) -> {
				if (e != null) {
					Path2D.Double p = new Path2D.Double();
					p.moveTo(x0 + xWidth * loc.x, y0 + yHeight * minmax(0, 1, loc.y));
					p.lineTo(x0 + xWidth * e.loc1.x, y0 + yHeight * minmax(0, 1, e.loc1.y));
					p.lineTo(x0 + xWidth * e.loc2.x, y0 + yHeight * minmax(0, 1, e.loc2.y));
					p.closePath();
					g.setColor(loc.color);
					g.fill(p);
				}
			});
		});

		g.setColor(seaBlue);
		if (graphs.riverPaths != null) {
			graphs.riverPaths.forEach((path) -> {
				Path2D.Double p = new Path2D.Double();
				boolean drawn = false;
				for (int i = 0; i < path.size() - 1; i++) {
					//					System.out.println(">>> " + path.getScore(i) + ", " + path.getElevation(i));

					g.setStroke(new BasicStroke((float) (path.getScore(i) / 25), BasicStroke.CAP_ROUND,
							BasicStroke.JOIN_ROUND));
					if (path.getScore(i) > 10) {
						g.drawLine((int) (x0 + path.getX(i) * xWidth), (int) (y0 + path.getY(i) * yHeight),
								(int) (x0 + path.getX(i + 1) * xWidth), (int) (y0 + path.getY(i + 1) * yHeight));
					}

					//					if (path.getScore(i) > 10) {
					//
					//						g.setStroke(new BasicStroke((float) (path.getScore(i) / 50), BasicStroke.CAP_ROUND,
					//								BasicStroke.JOIN_ROUND));
					//						if (!drawn) {
					//							p.moveTo(x0 + path.getX(i) * xWidth, y0 + path.getY(i) * yHeight);
					//							drawn = true;
					//						} else if (drawn) {
					//							p.lineTo(x0 + path.getX(i) * xWidth, y0 + path.getY(i) * yHeight);
					//						}
					//					}
				}
				if (drawn) {
					g.draw(p);
				}
			});
		}
	}

	private double minmax(double min, double max, double value) {
		return Math.min(max, Math.max(min, value));
	}

}
