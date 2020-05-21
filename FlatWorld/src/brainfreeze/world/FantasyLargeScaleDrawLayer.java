package brainfreeze.world;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.lwjgl.util.vector.Vector2f;

public class FantasyLargeScaleDrawLayer extends BaseDrawLayer {

	public static enum MapType {
		DEFAULT, TOPDOWN
	}

	private Random r;
	private int sizeFactor;
	private static final int BASE_SIZE_FACTOR = 70;
	private static final int MOUNTAIN_STYLE = 5;
	private Color hillColor = new Color(.95f, .95f, .95f);
	private List<List<Location>> pickList;
	private double fluxThreshold;
	private MapType mapType;
	private BufferedImage selectionTexture;
	double x0;
	double y0;
	double xWidth;
	double yHeight;
	private Graphs graphs;

	public FantasyLargeScaleDrawLayer(Random r, int sizeFactor, Graphs graphs, double fluxThreshold,
			MapType mapType, BufferedImage selectionTexture) {
		this.r = r;
		this.sizeFactor = sizeFactor;
		this.graphs = graphs;
		this.fluxThreshold = fluxThreshold;
		this.mapType = mapType;
		this.selectionTexture = selectionTexture;

		pickList = new CellPicker(graphs, 0.008).pick(r, 20);
	}

	@Override
	public void draw(BufferedImage im) {
		Graphics2D g = (Graphics2D) im.getGraphics();
		Rectangle clipBounds = g.getDeviceConfiguration().getBounds();

		x0 = clipBounds.x - 20;
		y0 = clipBounds.y - 20;
		xWidth = clipBounds.width * 1.1;
		yHeight = clipBounds.height * 1.1;

		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

		g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		setBaseColors(graphs);

		averageNeighborColors(graphs);

		setVertexColors(graphs, Color.LIGHT_GRAY);

		drawCells(g, graphs, x0, y0, xWidth, yHeight);

		drawRoads(g, graphs, x0, y0, xWidth, yHeight);

		drawSecondaryRoads(g, graphs, x0, y0, xWidth, yHeight);

		drawRivers(g, graphs, x0, y0, xWidth, yHeight);

		drawWater(g, graphs, x0, y0, xWidth, yHeight);

		drawCoast(g, graphs, x0, y0, xWidth, yHeight);

		drawPickListCircles(g, graphs, x0, y0, xWidth, yHeight);

		//		drawLabels(g, graphs, x0, y0, xWidth, yHeight);

		//		drawMountains(g, graphs, x0, y0, xWidth, yHeight);
	}

	//	private void drawMountains(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight) {
	//		List<Location> flatList = pickList.stream().flatMap((l) -> {
	//			return l.stream();
	//		}).filter((loc) -> {
	//			return loc.mountain && !loc.water;
	//		}).collect(Collectors.toList());
	//
	//		flatList.sort((l1, l2) -> {
	//			return (int) (1000 * (l1.y - l2.y));
	//		});
	//
	//		flatList.forEach((loc) -> {
	//			drawMountain(g, graphs, x0, y0, xWidth, yHeight, 1, loc);
	//		});
	//		flatList.forEach((loc) -> {
	//			List<Location> neighborMountains = loc.neighboringVertices(graphs.dualGraph).stream().filter((v) -> {
	//				double distance = Math.sqrt((loc.x - v.x) * (loc.x - v.x) + (loc.y - v.y) * (loc.y - v.y));
	//				return flatList.contains(v) && v.mountain && !v.water && v.y > (loc.y + loc.radius) && distance < 0.04
	//						&& !v.inMountainRange;
	//			}).collect(Collectors.toList());
	//
	//			Location bestNeighbor = null;
	//			double maxXDistance = Double.NEGATIVE_INFINITY;
	//			for (Location neighbor : neighborMountains) {
	//				double distance = Math.abs(loc.x - neighbor.x);
	//				if (distance > maxXDistance) {
	//					maxXDistance = distance;
	//					bestNeighbor = neighbor;
	//				}
	//			}
	//
	//			if (bestNeighbor != null) {
	//				loc.inMountainRange = true;
	//				bestNeighbor.inMountainRange = true;
	//				g.drawLine((int) (x0 + loc.x * xWidth), (int) (y0 + (loc.y - loc.radius) * yHeight),
	//						(int) (x0 + bestNeighbor.x * xWidth),
	//						(int) (y0 + (bestNeighbor.y - bestNeighbor.radius) * yHeight));
	//			}
	//		});
	//
	//		//	
	//		//			flatList.forEach((loc) -> {
	//		//				drawMountain(g, graphs, x0, y0, xWidth, yHeight, 1, loc);
	//		//				Set<Location> neighbors = loc.neighboringVertices(graphs.dualGraph);
	//		//				neighbors.stream().filter((v) -> {
	//		//					return v.mountain && !v.water && loc.y < v.y;
	//		//				}).forEach((v) -> {
	//		//					g.drawLine((int) (x0 + loc.x * xWidth), (int) (y0 + loc.y * yHeight), (int) (x0 + v.x * xWidth),
	//		//							(int) (y0 + v.y * yHeight));
	//		//				});
	//		//			});
	//	}

	private void drawLabels(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight) {
		Font font = new Font("sans", Font.PLAIN, 12);
		g.setFont(font);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphs.dualVertices.stream().filter((loc) -> {
			return loc.city;
		}).forEach((loc) -> {
			GlyphVector vector = font.createGlyphVector(new FontRenderContext(new AffineTransform(), false, true),
					"New York");
			Shape outline = vector.getOutline((float) (x0 + xWidth * loc.x + 10), (float) (y0 + yHeight * loc.y + 5));
			Rectangle2D bounds2d = outline.getBounds2D();
			Rectangle2D bigBounds = new Rectangle2D.Double(bounds2d.getMinX() - 2, bounds2d.getMinY() - 2,
					bounds2d.getWidth() + 4, bounds2d.getHeight() + 4);
			//			g.setStroke(new BasicStroke(2));
			g.setColor(new Color(1, 1, 1, 0.75f));
			//			g.fill(bigBounds);
			//			g.drawString(loc.name + "", (float)(x0 + xWidth * loc.x + 10), (float)(y0 + yHeight * loc.y + 5));
			g.setColor(Color.DARK_GRAY);
			g.fill(outline);
			//			g.drawString(loc.name + "", (float)(x0 + xWidth * loc.x + 10), (float)(y0 + yHeight * loc.y + 5));
		});

		graphs.dualVertices.stream().filter((loc) -> {
			return loc.town;
		}).forEach((loc) -> {
			GlyphVector vector = font.createGlyphVector(new FontRenderContext(new AffineTransform(), false, true),
					"Harrisonburg");
			Shape outline = vector.getOutline((float) (x0 + xWidth * loc.x + 10), (float) (y0 + yHeight * loc.y + 5));
			Rectangle2D bounds2d = outline.getBounds2D();
			Rectangle2D bigBounds = new Rectangle2D.Double(bounds2d.getMinX() - 2, bounds2d.getMinY() - 2,
					bounds2d.getWidth() + 4, bounds2d.getHeight() + 4);
			//			g.setStroke(new BasicStroke(2));
			g.setColor(new Color(1, 1, 1, 0.75f));
			//			g.fill(bigBounds);
			//			g.drawString(loc.name + "", (float)(x0 + xWidth * loc.x + 10), (float)(y0 + yHeight * loc.y + 5));
			g.setColor(Color.DARK_GRAY);
			g.fill(outline);
			//			g.drawString(loc.name + "", (float)(x0 + xWidth * loc.x + 10), (float)(y0 + yHeight * loc.y + 5));
		});
	}

	private void drawRoads(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight) {
		g.setColor(Color.black);

		g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[] { 4, 8 }, 0));
		graphs.dualEdges.stream().filter((edge) -> {
			return edge.road;
		}).forEach((edge) -> {
			g.drawLine((int) (x0 + edge.loc1.x * xWidth), (int) (y0 + edge.loc1.y * yHeight),
					(int) (x0 + edge.loc2.x * xWidth), (int) (y0 + edge.loc2.y * yHeight));
		});
	}

	private void drawSecondaryRoads(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight) {
		g.setColor(Color.black);

		g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[] { 1, 6 }, 0));
		graphs.dualEdges.stream().filter((edge) -> {
			return edge.secondaryRoad && !edge.road;
		}).forEach((edge) -> {
			g.drawLine((int) (x0 + edge.loc1.x * xWidth), (int) (y0 + edge.loc1.y * yHeight),
					(int) (x0 + edge.loc2.x * xWidth), (int) (y0 + edge.loc2.y * yHeight));
		});
	}

	private void drawPickListCircles(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight) {

		List<Location> flatList = pickList.stream().flatMap((l) -> {
			return l.stream();
		}).collect(Collectors.toList());

		flatList.sort((l1, l2) -> {
			return (int) (1000 * (l1.y - l2.y));
		});

		//				flatList.stream().filter((loc) -> {
		//					return !loc.water;
		//				}).forEach((loc) -> {
		//					
		//					if (loc.hill && !loc.mountain) {
		//						g.setColor(Color.blue);
		//					} else {
		//						g.setColor(Color.black);
		//					}
		//					g.drawOval((int) (x0 + loc.x * xWidth - 6), (int) (y0 + loc.y * yHeight - 6), (int)(loc.radius * xWidth), (int)(loc.radius * yHeight));
		//					if (loc.mountain) {
		//						g.fillOval((int) (x0 + loc.x * xWidth - 6), (int) (y0 + loc.y * yHeight - 6), (int)(loc.radius * xWidth), (int)(loc.radius * yHeight));
		//					}
		//				});

		flatList.stream().filter((loc) -> {
			return !loc.water;
		}).forEach((loc) -> {
			if (loc.hill && !loc.mountain && !loc.water) {
				drawHill(g, x0, y0, xWidth, yHeight, 0.015, loc.x - loc.radius / 2, loc.y, loc);
			} else if (loc.mountain && !loc.water) {
				drawMountain(g, graphs, x0, y0, xWidth, yHeight, MOUNTAIN_STYLE, loc);
				//				drawMountain(g, 1, x0, y0, xWidth, yHeight, 0.015, loc.x - loc.radius / 2, loc.y);
			} else if (loc.forest && !loc.water) {
				drawTree(g, x0, y0, xWidth, yHeight, loc.y + loc.radius / 2, 0.015, loc.x - loc.radius / 2);
			}
		});

		g.setColor(Color.black);
		graphs.dualVertices.stream().filter((loc) -> {
			return loc.city;
		}).forEach((loc) -> {
			g.fillOval((int) (x0 + loc.x * xWidth - 3), (int) (y0 + loc.y * yHeight - 3), 6, 6);
			g.drawOval((int) (x0 + loc.x * xWidth - 6), (int) (y0 + loc.y * yHeight - 6), 12, 12);
		});

		g.setColor(Color.black);
		graphs.dualVertices.stream().filter((loc) -> {
			return loc.town;
		}).forEach((loc) -> {
			g.drawOval((int) (x0 + loc.x * xWidth - 3), (int) (y0 + loc.y * yHeight - 3), 6, 6);
			//			g.drawOval((int) (x0 + loc.x * xWidth - 6), (int) (y0 + loc.y * yHeight - 6), 12, 12);
		});

	}

	private void drawTree(Graphics2D g, double x0, double y0, double xWidth, double yHeight, double maxY, double maxD,
			double xa) {
		g.setStroke(new BasicStroke(1));
		g.setColor(Color.DARK_GRAY);
		double ya = maxY - maxD + .015;
		Path2D.Float p = new Path2D.Float();

		g.drawLine((int) (x0 + (xa + maxD / 4) * xWidth), (int) (y0 + (ya - maxD) * yHeight),
				(int) (x0 + (xa + maxD / 4) * xWidth), (int) (y0 + ya * yHeight));
		p.moveTo(x0 + (xa + maxD / 4) * xWidth, y0 + (ya - maxD) * yHeight);
		p.lineTo(x0 + (xa) * xWidth, y0 + (ya - maxD / 4) * yHeight);
		p.lineTo(x0 + (xa + maxD / 2) * xWidth, y0 + (ya - maxD / 4) * yHeight);
		p.closePath();

		g.setColor(Color.lightGray);
		g.fill(p);
		g.setColor(Color.DARK_GRAY);
		g.draw(p);
	}

	//	private void drawMountain(Graphics2D g, int mountainStyle, double x0, double y0, double xWidth,
	//			double yHeight, double maxD, double xa, double ya) {
	//		Path2D.Float p = new Path2D.Float();
	//		if (mountainStyle == 0) {
	//			p.moveTo(x0 + xa * xWidth, y0 + ya * yHeight);
	//			p.lineTo(x0 + (xa + maxD / 2) * xWidth, y0 + (ya - 2 * maxD / 3) * yHeight);
	//			p.lineTo(x0 + (xa + maxD) * xWidth, y0 + ya * yHeight);
	//			p.closePath();
	//			g.setColor(Color.darkGray);
	//			g.fill(p);
	//		} else {
	//			xa -= .003;
	//			ya += .0035;
	//			p.moveTo(x0 + (xa) * xWidth, y0 + ya * yHeight);
	//			double jiggleX = (r.nextDouble() - .2) * maxD * .06;
	//			double jiggleY = r.nextDouble() * maxD * .5;
	//			double jiggleX2 = -(r.nextDouble() - .2) * maxD * .06;
	//			double jiggleY2 = r.nextDouble() * maxD * .5;
	//
	//			p.curveTo(x0 + (xa + maxD / 2 + jiggleX) * xWidth, y0 + (ya - 2 * maxD / 3 + jiggleY) * yHeight,
	//					x0 + (xa + maxD / 2 + jiggleX) * xWidth, y0 + (ya - 2 * maxD / 3) * yHeight,
	//					x0 + (xa + maxD / 2) * xWidth, y0 + (ya - 2 * maxD / 3) * yHeight);
	//			p.curveTo(x0 + (xa + maxD / 2 - jiggleX2) * xWidth, y0 + (ya - 2 * maxD / 3 + jiggleY2) * yHeight,
	//					x0 + (xa + maxD / 2 - jiggleX2) * xWidth, y0 + (ya - 2 * maxD / 3) * yHeight,
	//					x0 + (xa + maxD) * xWidth, y0 + ya * yHeight);
	//
	//			g.setColor(Color.LIGHT_GRAY);
	//			g.fill(p);
	//			g.setColor(Color.DARK_GRAY);
	//			g.draw(p);
	//		}
	//	}

	private void drawMountain(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight,
			int mountainStyle, Location loc) {
		if (mapType == MapType.TOPDOWN) {
			drawMountainTopDown(g, graphs, x0, y0, xWidth, yHeight, loc);
		} else {
			switch (mountainStyle) {
			case 0:
				drawMountainStyle0(g, x0, y0, xWidth, yHeight, loc);
				break;

			case 1:

				drawMountainStyle1(g, x0, y0, xWidth, yHeight, loc);

				break;

			case 2:
				drawMountainStyle2(g, graphs, x0, y0, xWidth, yHeight, loc);

				break;

			case 3:
				drawMountainStyle3(g, graphs, x0, y0, xWidth, yHeight, loc);

				break;

			case 4:
				drawMountainStyle4(g, graphs, x0, y0, xWidth, yHeight, loc);

				break;

			case 5:
				drawMountainStyle5(g, graphs, x0, y0, xWidth, yHeight, loc);

				break;
			}
		}

	}

	private void drawMountainTopDown(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight,
			Location loc) {
		if (!graphs.dualGraph.containsVertex(loc)) {
			return;
		}
		graphs.dualGraph.edgesOf(loc).stream().map((e) -> {
			return graphs.dualToVoronoi.get(e);
		}).flatMap((e) -> {
			return Arrays.stream(new Location[] { e.loc1, e.loc2 });
		}).forEach((v) -> {
			g.setColor(Color.DARK_GRAY);
			g.drawLine((int) (x0 + xWidth * loc.x), (int) (y0 + yHeight * loc.y), (int) (x0 + xWidth * v.x),
					(int) (y0 + yHeight * v.y));
		});
	}

	private void drawMountainStyle4(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight,
			Location loc) {
		double jiggleX = (r.nextDouble() - 0.5) * loc.radius * 0.5;
		double jiggleY = (r.nextDouble() - 0.5) * loc.radius;
		double jiggleX2 = (r.nextDouble() - 0.5) * loc.radius * 0.5;
		double jiggleY2 = (r.nextDouble() - 0.5) * loc.radius;

		Path2D p = new Path2D.Double();
		p.moveTo(x0 + (loc.x - loc.radius) * xWidth, y0 + loc.y * yHeight);
		//			p.lineTo(x0 + loc.x * xWidth, y0 + (loc.y - loc.radius) * yHeight);
		//			p.lineTo(x0 + (loc.x + loc.radius) * xWidth, y0 + loc.y * yHeight);

		p.curveTo(x0 + (loc.x - 2 * loc.radius / 3 + jiggleX) * xWidth,
				y0 + (loc.y - loc.radius / 3 + jiggleY) * yHeight, x0 + (loc.x - loc.radius / 3 + jiggleX) * xWidth,
				y0 + (loc.y - 2 * loc.radius / 3) * yHeight, x0 + (loc.x) * xWidth,
				y0 + (loc.y - loc.radius) * yHeight);
		p.curveTo(x0 + (loc.x + loc.radius / 3 - jiggleX2) * xWidth,
				y0 + (loc.y - 2 * loc.radius / 3 + jiggleY2) * yHeight,
				x0 + (loc.x + 2 * loc.radius / 3 - jiggleX2) * xWidth, y0 + (loc.y - loc.radius / 3) * yHeight,
				x0 + (loc.x + loc.radius) * xWidth, y0 + loc.y * yHeight);

		g.setPaint(new GradientPaint((float) (x0 + (loc.x - loc.radius) * xWidth), (float) loc.y,
				new Color(0.9f, 0.9f, 0.9f), (float) (x0 + (loc.x + loc.radius) * xWidth), (float) loc.y,
				new Color(0.5f, 0.5f, 0.5f), false));

		g.fill(p);
		g.setColor(Color.DARK_GRAY);
		g.setStroke(new BasicStroke(1.5f));
		g.draw(p);
	}

	private void drawMountainStyle0(Graphics2D g, double x0, double y0, double xWidth, double yHeight, Location loc) {
		Path2D p = new Path2D.Double();
		p.moveTo(x0 + (loc.x - loc.radius) * xWidth, y0 + loc.y * yHeight);

		p.lineTo(x0 + (loc.x) * xWidth, y0 + (loc.y - loc.radius) * yHeight);
		p.lineTo(x0 + (loc.x + loc.radius) * xWidth, y0 + loc.y * yHeight);

		//		g.setColor(Color.LIGHT_GRAY);
		//		g.fill(p);
		g.setColor(Color.DARK_GRAY);
		g.draw(p);
	}

	private void drawMountainStyle3(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight,
			Location loc) {
		List<MapEdge> voronoiEdges = graphs.dualGraph.edgesOf(loc).stream().map((dualEdge) -> {
			return graphs.dualToVoronoi.get(dualEdge);
		}).collect(Collectors.toList());
		voronoiEdges.sort((e1, e2) -> {
			double y1 = (e1.loc1.y + e1.loc2.y) / 2;
			double y2 = (e2.loc1.y + e2.loc2.y) / 2;
			return (int) (1000 * (y1 - y2));
		});

		double minY = voronoiEdges.stream().mapToDouble((e) -> {
			return Math.min(e.loc1.y, e.loc2.y);
		}).min().getAsDouble();
		double maxX = voronoiEdges.stream().mapToDouble((e) -> {
			return Math.max(e.loc1.x, e.loc2.x);
		}).max().getAsDouble();
		double minX = voronoiEdges.stream().mapToDouble((e) -> {
			return Math.min(e.loc1.x, e.loc2.x);
		}).min().getAsDouble();

		double avgX = (minX + maxX) / 2;

		g.setColor(Color.LIGHT_GRAY);
		double peakY = minY - (maxX - minX) / 8;

		Path2D.Double p2 = new Path2D.Double();
		p2.moveTo(x0 + xWidth * minX, y0 + yHeight * loc.y);
		p2.lineTo(x0 + xWidth * loc.x, y0 + yHeight * peakY);
		p2.lineTo(x0 + xWidth * avgX, y0 + yHeight * loc.y);
		g.setColor(Color.DARK_GRAY);
		g.fill(p2);

		Path2D.Double p3 = new Path2D.Double();
		p3.moveTo(x0 + xWidth * avgX, y0 + yHeight * loc.y);
		p3.lineTo(x0 + xWidth * loc.x, y0 + yHeight * peakY);
		p3.lineTo(x0 + xWidth * maxX, y0 + yHeight * loc.y);
		g.setColor(new Color(0.5f, 0.5f, 0.5f));
		g.fill(p3);

	}

	private void drawMountainStyle1(Graphics2D g, double x0, double y0, double xWidth, double yHeight, Location loc) {
		double jiggleX = (r.nextDouble() - 0.5) * loc.radius * 0.5;
		double jiggleY = (r.nextDouble() - 0.5) * loc.radius;
		double jiggleX2 = (r.nextDouble() - 0.5) * loc.radius * 0.5;
		double jiggleY2 = (r.nextDouble() - 0.5) * loc.radius;

		Path2D p = new Path2D.Double();
		p.moveTo(x0 + (loc.x - loc.radius) * xWidth, y0 + loc.y * yHeight);
		//			p.lineTo(x0 + loc.x * xWidth, y0 + (loc.y - loc.radius) * yHeight);
		//			p.lineTo(x0 + (loc.x + loc.radius) * xWidth, y0 + loc.y * yHeight);

		p.curveTo(x0 + (loc.x - 2 * loc.radius / 3 + jiggleX) * xWidth,
				y0 + (loc.y - loc.radius / 3 + jiggleY) * yHeight, x0 + (loc.x - loc.radius / 3 + jiggleX) * xWidth,
				y0 + (loc.y - 2 * loc.radius / 3) * yHeight, x0 + (loc.x) * xWidth,
				y0 + (loc.y - loc.radius) * yHeight);
		p.curveTo(x0 + (loc.x + loc.radius / 3 - jiggleX2) * xWidth,
				y0 + (loc.y - 2 * loc.radius / 3 + jiggleY2) * yHeight,
				x0 + (loc.x + 2 * loc.radius / 3 - jiggleX2) * xWidth, y0 + (loc.y - loc.radius / 3) * yHeight,
				x0 + (loc.x + loc.radius) * xWidth, y0 + loc.y * yHeight);

		g.setStroke(new BasicStroke(1.5f));
		g.setColor(Color.LIGHT_GRAY);
		g.fill(p);
		g.setColor(Color.DARK_GRAY);
		g.draw(p);
	}

	private void drawMountainStyle5(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight,
			Location loc) {
		drawMountainStyle4(g, graphs, x0, y0, xWidth, yHeight, loc);

		double jiggleX = (r.nextDouble() - 0.5) * loc.radius * 0.5;
		double jiggleY = (r.nextDouble() - 0.5) * loc.radius;
		double jiggleX2 = (r.nextDouble() - 0.5) * loc.radius * 0.5;
		double jiggleY2 = (r.nextDouble() - 0.5) * loc.radius;

		Path2D p = new Path2D.Double();
		p.moveTo(x0 + (loc.x - loc.radius / 2) * xWidth, y0 + loc.y * yHeight);
		//			p.lineTo(x0 + loc.x * xWidth, y0 + (loc.y - loc.radius) * yHeight);
		//			p.lineTo(x0 + (loc.x + loc.radius) * xWidth, y0 + loc.y * yHeight);

		p.curveTo(x0 + (loc.x - loc.radius / 3 + jiggleX) * xWidth, y0 + (loc.y - loc.radius / 3 + jiggleY) * yHeight,
				x0 + (loc.x - loc.radius / 6 + jiggleX) * xWidth, y0 + (loc.y - 2 * loc.radius / 3) * yHeight,
				x0 + (loc.x) * xWidth, y0 + (loc.y - loc.radius) * yHeight);

		g.setStroke(new BasicStroke(0.5f));
		g.setColor(Color.DARK_GRAY);
		g.draw(p);
	}

	private void drawMountainStyle2(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight,
			Location loc) {
		List<MapEdge> voronoiEdges = graphs.dualGraph.edgesOf(loc).stream().map((dualEdge) -> {
			return graphs.dualToVoronoi.get(dualEdge);
		}).collect(Collectors.toList());
		voronoiEdges.sort((e1, e2) -> {
			double y1 = (e1.loc1.y + e1.loc2.y) / 2;
			double y2 = (e2.loc1.y + e2.loc2.y) / 2;
			return (int) (1000 * (y1 - y2));
		});

		double minY = voronoiEdges.stream().mapToDouble((e) -> {
			return Math.min(e.loc1.y, e.loc2.y);
		}).min().getAsDouble();
		double maxX = voronoiEdges.stream().mapToDouble((e) -> {
			return Math.max(e.loc1.x, e.loc2.x);
		}).max().getAsDouble();
		double minX = voronoiEdges.stream().mapToDouble((e) -> {
			return Math.min(e.loc1.x, e.loc2.x);
		}).min().getAsDouble();

		Vector2f light = new Vector2f(1, 0);

		g.setColor(Color.LIGHT_GRAY);
		double peakY = minY - (maxX - minX) / 8;
		for (MapEdge e : voronoiEdges) {
			Path2D.Double p2 = new Path2D.Double();

			Vector2f v1 = new Vector2f((float) (e.loc1.x - e.loc2.x), (float) (e.loc1.y - e.loc2.y)).normalise(null);
			float dot = Vector2f.dot(light, v1);
			float c = 0.2f + (1 + dot) * 0.3f;
			g.setColor(new Color(c, c, c));

			p2.moveTo(x0 + xWidth * e.loc1.x, y0 + yHeight * e.loc1.y);
			p2.lineTo(x0 + xWidth * e.loc2.x, y0 + yHeight * e.loc2.y);
			p2.lineTo(x0 + xWidth * loc.x, y0 + yHeight * peakY);

			//				g.setColor(Color.LIGHT_GRAY);
			g.fill(p2);
			//				g.setColor(Color.DARK_GRAY);
			//				g.draw(p2);
		}
	}

	private void drawCoast(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight) {
		graphs.voronoiEdges.forEach((e) -> {
			g.setStroke(new BasicStroke(1.5f));
			MapEdge dualEdge = graphs.voronoiToDual.get(e);
			if (dualEdge != null) {
				if (dualEdge.loc1.water != dualEdge.loc2.water) {
					g.setColor(Color.black);
					g.drawLine((int) (x0 + e.loc1.x * xWidth), (int) (y0 + e.loc1.y * yHeight),
							(int) (x0 + e.loc2.x * xWidth), (int) (y0 + e.loc2.y * yHeight));
				}
			}
		});
	}

	private void drawHill(Graphics2D g, double x0, double y0, double xWidth, double yHeight, double maxD, double xa,
			double ya, Location loc) {
		Path2D.Float p = new Path2D.Float();

		p.moveTo(x0 + xa * xWidth, y0 + ya * yHeight);

		p.curveTo(x0 + (xa + maxD / 4) * xWidth, y0 + (ya - maxD / 2) * yHeight, x0 + (xa + 3 * maxD / 4) * xWidth,
				y0 + (ya - maxD / 2) * yHeight, x0 + (xa + maxD) * xWidth, y0 + ya * yHeight);

		//				p.closePath();
		g.setColor(hillColor);
		g.setPaint(new GradientPaint((float) (x0 + xa * xWidth), (float) loc.y, hillColor,
				(float) (x0 + (xa + maxD) * xWidth), (float) loc.y, Color.lightGray, false));

		g.fill(p);
		g.setColor(Color.lightGray);
		g.draw(p);

		//			p = new Path2D.Float();
		//	
		//			double jiggleX = r.nextDouble() < 0.5 ? -5 : 5;
		//			double jiggleY = r.nextDouble() < 0.5 ? 5 : 8;
		//			
		//			p.moveTo(x0 + xa * xWidth + jiggleX, y0 + ya * yHeight + jiggleY);
		//	
		//			p.curveTo(x0 + (xa + maxD / 4) * xWidth + jiggleX, y0 + (ya - maxD / 2) * yHeight + jiggleY, x0 + (xa + 3 * maxD / 4) * xWidth + jiggleX,
		//					y0 + (ya - maxD / 2) * yHeight + jiggleY, x0 + (xa + maxD) * xWidth + jiggleX, y0 + ya * yHeight + jiggleY);
		//	
		//			//				p.closePath();
		//			g.setColor(hillColor);
		//			g.fill(p);
		//			g.setColor(Color.lightGray);
		//			g.draw(p);
	}

	//	private void drawHill(Graphics2D g, double x0, double y0, double xWidth, double yHeight, double maxD, double xa,
	//			double ya, Location loc) {
	//		Path2D.Float p = new Path2D.Float();
	//
	//		p.moveTo(x0 + (loc.x - loc.radius) * xWidth, y0 + ya * yHeight);
	//
	//		p.curveTo(x0 + (loc.x - loc.radius / 3) * xWidth, y0 + (ya - maxD / 2) * yHeight,
	//				x0 + (loc.x + loc.radius / 3) * xWidth, y0 + (ya - maxD / 2) * yHeight,
	//				x0 + (loc.x + loc.radius) * xWidth, y0 + ya * yHeight);
	//
	//		//				p.closePath();
	//		g.setColor(Color.white);
	//		g.fill(p);
	//		g.setColor(Color.lightGray);
	//		g.draw(p);
	//
	//		p = new Path2D.Float();
	//
	//		double jiggleX = r.nextDouble() < 0.5 ? -5 : 5;
	//		double jiggleY = r.nextDouble() < 0.5 ? 5 : 8;
	//
	//		p.moveTo(x0 + (loc.x - loc.radius) * xWidth + jiggleX, y0 + ya * yHeight + jiggleY);
	//
	//		p.curveTo(x0 + (loc.x - loc.radius / 3) * xWidth + jiggleX, y0 + (ya - maxD / 2) * yHeight + jiggleY,
	//				x0 + (loc.x + loc.radius/3) * xWidth + jiggleX, y0 + (ya - maxD / 2) * yHeight + jiggleY,
	//				x0 + (loc.x + loc.radius) * xWidth + jiggleX, y0 + ya * yHeight + jiggleY);
	//
	//		//				p.closePath();
	//		g.setColor(Color.white);
	//		g.fill(p);
	//		g.setColor(Color.lightGray);
	//		g.draw(p);
	//	}

	private void drawWater(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight) {
		graphs.dualVertices.forEach((loc) -> {
			List<MapEdge> edgeList = graphs.dualGraph.edgesOf(loc).stream().map((voronoiEdge) -> {
				return graphs.dualToVoronoi.get(voronoiEdge);
			}).collect(Collectors.toList());

			if (loc.water) {
				edgeList.forEach((e) -> {
					if (e != null) {
						Path2D.Double p = new Path2D.Double();
						p.moveTo(x0 + xWidth * loc.x, y0 + yHeight * minmax(0, 1, loc.y));
						p.lineTo(x0 + xWidth * e.loc1.x, y0 + yHeight * minmax(0, 1, e.loc1.y));
						p.lineTo(x0 + xWidth * e.loc2.x, y0 + yHeight * minmax(0, 1, e.loc2.y));
						p.closePath();
						g.setColor(Color.lightGray);
						g.fill(p);
					}
				});
			}
		});
	}

	private void drawRivers(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight) {
		g.setColor(new Color(0.4f, 0.4f, 0.4f));
		g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		//		if (graphs.riverPaths != null) {
		//			graphs.riverPaths.forEach((path) -> {
		//				Path2D.Double p = new Path2D.Double();
		//				boolean drawn = false;
		//				for (int i = 0; i < path.size(); i++) {
		//					if (path.getScore(i) > fluxThreshold) {
		//						if (!drawn) {
		//							p.moveTo(x0 + path.getX(i) * xWidth, y0 + path.getY(i) * yHeight);
		//							drawn = true;
		//						} else if (drawn) {
		//							p.lineTo(x0 + path.getX(i) * xWidth, y0 + path.getY(i) * yHeight);
		//						}
		//					}
		//				}
		//				if (drawn) {
		//					g.draw(p);
		//				}
		//			});
		//		}

		graphs.dualEdges.forEach((e) -> {
			if (e.river && e.flux > fluxThreshold) {
				g.drawLine((int) (x0 + xWidth * e.loc1.x), (int) (y0 + yHeight * e.loc1.y),
						(int) (x0 + xWidth * e.loc2.x), (int) (y0 + yHeight * e.loc2.y));
			}
		});
	}

	private void drawCells(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight) {
		int[] i = new int[1];
		Graphics2D g2 = (Graphics2D) selectionTexture.getGraphics();
		graphs.dualVertices.forEach((loc) -> {
			List<MapEdge> edgeList = graphs.dualGraph.edgesOf(loc).stream().map((voronoiEdge) -> {
				return graphs.dualToVoronoi.get(voronoiEdge);
			}).collect(Collectors.toList());
			loc.index = i[0];

			edgeList.forEach((e) -> {
				if (e != null) {
					Path2D.Double p = new Path2D.Double();
					p.moveTo(x0 + xWidth * loc.x, y0 + yHeight * minmax(0, 1, loc.y));
					p.lineTo(x0 + xWidth * e.loc1.x, y0 + yHeight * minmax(0, 1, e.loc1.y));
					p.lineTo(x0 + xWidth * e.loc2.x, y0 + yHeight * minmax(0, 1, e.loc2.y));
					p.closePath();

					Point2D.Double pt1 = new Point2D.Double(x0 + xWidth * loc.x, y0 + yHeight * minmax(0, 1, loc.y));
					Point2D.Double pt2 = new Point2D.Double(x0 + xWidth * e.loc1.x,
							y0 + yHeight * minmax(0, 1, e.loc1.y));
					Point2D.Double pt3 = new Point2D.Double(x0 + xWidth * e.loc2.x,
							y0 + yHeight * minmax(0, 1, e.loc2.y));

					if (loc.water) {
						if (loc.isLake) {
							g.setColor(Color.red);
						} else {
							g.setColor(loc.color);
						}
						//					} else if (loc.forest && !loc.mountain && !loc.hill) {
						//						g.setColor(hillColor.green);
					} else {
						//						g.setPaint(
						//								new TriangleGradientPaint(pt1, loc.color, pt2, Color.white, pt3, Color.white, false));
						g.setColor(loc.color);
						g.fill(p); // this gets rid of stray tearing artifacts.

						g.setPaint(
								new TriangleGradientPaint(pt1, loc.color, pt2, e.loc1.color, pt3, e.loc2.color, false));
						//					g.setColor(loc.color);
					}

					g.fill(p);

					g2.setColor(new Color(i[0]));
					g2.fill(p);
					g2.draw(p);
				}
			});
			i[0]++;
		});
	}

	private void averageNeighborColors(Graphs graphs) {
		graphs.dualVertices.forEach((loc) -> {
			Set<Color> neighborColors = graphs.dualGraph.edgesOf(loc).stream().map((edge) -> {
				return edge.oppositeLocation(loc);
			}).filter((v) -> {
				return !v.water;
			}).map((v) -> {
				return v.baseColor;
			}).collect(Collectors.toSet());

			int numNeighbors = neighborColors.size();

			if (numNeighbors == 0) {
				loc.color = loc.baseColor;
			} else {

				double red = 0;
				double green = 0;
				double blue = 0;
				for (Color c : neighborColors) {
					red += c.getRed();
					green += c.getGreen();
					blue += c.getBlue();
				}
				red = (red + numNeighbors * loc.baseColor.getRed()) / (2 * numNeighbors);
				green = (green + numNeighbors * loc.baseColor.getGreen()) / (2 * numNeighbors);
				blue = (blue + numNeighbors * loc.baseColor.getBlue()) / (2 * numNeighbors);
				loc.color = new Color((int) red, (int) green, (int) blue);
			}
		});
	}

	protected void setBaseColors(Graphs graphs) {
		graphs.dualVertices.forEach((loc) -> {
			if (loc.water) {
				loc.baseColor = Color.lightGray;
			} else if (loc.hill) {
				loc.baseColor = hillColor;
			} else if (loc.mountain) {
				loc.baseColor = Color.LIGHT_GRAY;
			} else {
				loc.baseColor = Color.white;
			}
		});
	}

	static double minmax(double min, double max, double value) {
		return Math.min(max, Math.max(min, value));
	}

}
