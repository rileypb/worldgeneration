package voronoinew;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

public class SeaLandDrawLayer3 implements DrawLayer {

	private Random r;
	private int sizeFactor;
	private static final int BASE_SIZE_FACTOR = 70;

	public SeaLandDrawLayer3(Random r, int sizeFactor) {
		this.r = r;
		this.sizeFactor = sizeFactor;
	}

	@Override
	public void draw(Graphics2D g, Graphs graphs, BufferedImage im) {
		boolean prettyMountains = true;

		InputStream imgStream = this.getClass().getResourceAsStream("mountain.png");
		Image mountainImage = null;
		try {
			mountainImage = ImageIO.read(imgStream);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		final Image mImage = mountainImage;

		Rectangle clipBounds = g.getDeviceConfiguration().getBounds();

		double x0 = clipBounds.x - 20;
		double y0 = clipBounds.y - 20;
		double xWidth = clipBounds.width * 1.1;
		double yHeight = clipBounds.height * 1.1;

		//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		Color hillColor = new Color(.9f, .9f, .9f);

		g.setColor(Color.BLACK);

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

		graphs.dualVertices.forEach((loc) -> {
			Set<Color> neighborColors = graphs.dualGraph.edgesOf(loc).stream().map((edge) -> {
				return edge.oppositeLocation(loc);
			}).map((v) -> {
				return v.baseColor;
			}).collect(Collectors.toSet());

			int numNeigbors = neighborColors.size();
			double red = 0;
			double green = 0;
			double blue = 0;
			for (Color c : neighborColors) {
				red += c.getRed();
				green += c.getGreen();
				blue += c.getBlue();
			}
			red = (red + numNeigbors * loc.baseColor.getRed()) / (2 * numNeigbors);
			green = (green + numNeigbors * loc.baseColor.getGreen()) / (2 * numNeigbors);
			blue = (blue + numNeigbors * loc.baseColor.getBlue()) / (2 * numNeigbors);
			loc.color = new Color((int) red, (int) green, (int) blue);
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

					//					System.out.println(loc.elevation);

					//					System.out.println(e.elevation + ": " + e.loc1.water);
					//					if (loc.water) {
					//						g.setColor(Color.lightGray);
					//					} else {
					g.setColor(loc.color);

					//						g.setColor(Color.cyan);
					//						if (loc.elevation > 0.5) {
					//							g.setColor(Color.red);
					//						} else if (loc.elevation > 0.25) {
					//							g.setColor(Color.PINK);
					//						}
					//												float c = (float) Math.max(0, Math.min(1, loc.elevation));
					//												g.setColor(new Color(c,c,c));
					//					}
					g.fill(p);
				}
			});
		});

		//		g.setStroke(new BasicStroke(5));
		//		graphs.dualEdges.forEach((e) -> {
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

		g.setColor(new Color(0.5f, 0.5f, 0.5f));
		g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		if (graphs.riverPaths != null) {
			graphs.riverPaths.forEach((path) -> {
				Path2D.Double p = new Path2D.Double();
				boolean drawn = false;
				for (int i = 0; i < path.size(); i++) {
					//					System.out.println(">>> " + path.getScore(i) + ", " + path.getElevation(i));
					if (path.getScore(i) > 10 * sizeFactor / BASE_SIZE_FACTOR) {
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
						//					System.out.println(loc.elevation);

						//					System.out.println(e.elevation + ": " + e.loc1.water);
						if (loc.water) {
							g.setColor(Color.lightGray);
						} else {
							g.setColor(Color.white);

							if (loc.mountain) {
								//							g.setColor(Color.darkGray);
							} else if (loc.hill) {
								//							g.setColor(new Color(0.5f,0.5f,0.5f));
							}

							//						g.setColor(Color.cyan);
							//						if (loc.elevation > 0.5) {
							//							g.setColor(Color.red);
							//						} else if (loc.elevation > 0.25) {
							//							g.setColor(Color.PINK);
							//						}
							//												float c = (float) Math.max(0, Math.min(1, loc.elevation));
							//												g.setColor(new Color(c,c,c));
						}
						g.fill(p);
					}
				});
			}
		});

		graphs.dualVertices.forEach((loc) -> {
			double minX = Double.POSITIVE_INFINITY;
			double maxX = Double.NEGATIVE_INFINITY;
			double minY = Double.POSITIVE_INFINITY;
			double maxY = Double.NEGATIVE_INFINITY;
			for (MapEdge edge : graphs.dualGraph.edgesOf(loc)) {
				Location l2 = edge.oppositeLocation(loc);
				minX = Math.min(minX, l2.x);
				maxX = Math.max(minX, l2.x);
				minY = Math.min(minY, l2.y);
				maxY = Math.max(maxY, l2.y);
			}
			double dx = (maxX - minX);
			double dy = (maxY - minY);
			double maxD = (dx + dy) / 5;

			double xa = minX + maxD / 4 + .01;
			double ya = maxY - maxD;
			if (loc.hill && !loc.water) {
				Path2D.Float p = new Path2D.Float();

				p.moveTo(x0 + xa * xWidth, y0 + ya * yHeight);

				p.curveTo(x0 + (xa + maxD / 4) * xWidth, y0 + (ya - maxD / 2) * yHeight,
						x0 + (xa + 3 * maxD / 4) * xWidth, y0 + (ya - maxD / 2) * yHeight, x0 + (xa + maxD) * xWidth,
						y0 + ya * yHeight);

				//				p.closePath();
				g.setColor(Color.white);
				g.fill(p);
				g.setColor(Color.lightGray);
				g.draw(p);
			}
		});

		graphs.voronoiEdges.forEach((e) -> {
			g.setStroke(new BasicStroke(1.5f));
			MapEdge dualEdge = graphs.voronoiToDual.get(e);
			if (dualEdge != null) {
				if (dualEdge.loc1.water != dualEdge.loc2.water) {
					//					System.out.println("draw");
					g.setColor(Color.black);
					g.drawLine((int) (x0 + e.loc1.x * xWidth), (int) (y0 + e.loc1.y * yHeight),
							(int) (x0 + e.loc2.x * xWidth), (int) (y0 + e.loc2.y * yHeight));
				}
			}
		});

		Color medGray = new Color(0.5f, 0.5f, 0.5f);
		g.setStroke(new BasicStroke(1));
		graphs.dualVertices.forEach((loc) -> {
			double minX = Double.POSITIVE_INFINITY;
			double maxX = Double.NEGATIVE_INFINITY;
			double minY = Double.POSITIVE_INFINITY;
			double maxY = Double.NEGATIVE_INFINITY;
			Set<Location> vertices = graphs.dualGraph.edgesOf(loc).stream().map((e) -> {
				return graphs.dualToVoronoi.get(e);
			}).filter((e) -> {
				return e != null;
			}).flatMap((e) -> {
				return Arrays.stream(new Location[] { e.loc1, e.loc2 });
			}).collect(Collectors.toSet());
			for (Location v : vertices) {
				minX = Math.min(minX, v.x);
				maxX = Math.max(minX, v.x);
				minY = Math.min(minY, v.y);
				maxY = Math.max(maxY, v.y);
			}
			double dx = (maxX - minX);
			double dy = (maxY - minY);
			double maxD = Math.max(dx, dy);

			double xa = minX + maxD / 4;
			if (maxD < .02) {
				double ya = maxY - maxD;
				if (loc.mountain && !loc.water) {
					Path2D.Float p = new Path2D.Float();
					if (!prettyMountains) {
						p.moveTo(x0 + xa * xWidth, y0 + ya * yHeight);
						p.lineTo(x0 + (xa + maxD / 2) * xWidth, y0 + (ya - 2 * maxD / 3) * yHeight);
						p.lineTo(x0 + (xa + maxD) * xWidth, y0 + ya * yHeight);
						p.closePath();
						g.setColor(Color.darkGray);
						g.fill(p);
					} else {
						xa -= .003;
						ya += .0035;
						p.moveTo(x0 + (xa) * xWidth, y0 + ya * yHeight);
						double jiggleX = (r.nextDouble() - .1) * maxD * .06;
						double jiggleY = r.nextDouble() * maxD * .5;
						double jiggleX2 = -(r.nextDouble() - .1) * maxD * .06;
						double jiggleY2 = r.nextDouble() * maxD * .5;

						p.curveTo(x0 + (xa + maxD / 2 + jiggleX) * xWidth, y0 + (ya - 2 * maxD / 3 + jiggleY) * yHeight,
								x0 + (xa + maxD / 2 + jiggleX) * xWidth, y0 + (ya - 2 * maxD / 3) * yHeight,
								x0 + (xa + maxD / 2) * xWidth, y0 + (ya - 2 * maxD / 3) * yHeight);
						p.curveTo(x0 + (xa + maxD / 2 + jiggleX2) * xWidth,
								y0 + (ya - 2 * maxD / 3 + jiggleY2) * yHeight, x0 + (xa + maxD / 2 + jiggleX2) * xWidth,
								y0 + (ya - 2 * maxD / 3) * yHeight, x0 + (xa + maxD) * xWidth, y0 + ya * yHeight);

						//					if (jiggleX < 0) {
						//						p.lineTo(x0 + (xa + maxD / 2 + jiggleX) * xWidth, y0 + (ya - 2 * maxD / 3 + jiggleY) * yHeight);
						//						p.lineTo(x0 + (xa + maxD / 2) * xWidth, y0 + (ya - 2 * maxD / 3) * yHeight);
						//					} else {
						//						p.lineTo(x0 + (xa + maxD / 2) * xWidth, y0 + (ya - 2 * maxD / 3) * yHeight);
						//						p.lineTo(x0 + (xa + maxD / 2 + jiggleX) * xWidth, y0 + (ya - 2 * maxD / 3 + jiggleY) * yHeight);
						//					}
						//					p.lineTo(x0 + (xa + maxD) * xWidth, y0 + ya * yHeight);
						g.setColor(Color.LIGHT_GRAY);
						g.fill(p);
						g.setColor(Color.DARK_GRAY);
						g.draw(p);
					}
					//				g.drawImage(mImage, (int) xa,(int) ya, 20, 20, null);
				}
			}
		});

		//		g.setColor(Color.lightGray);
		//		graphs.dualVertices.forEach((loc) -> {
		//			List<MapEdge> edgeList = graphs.dualGraph.edgesOf(loc).stream().map((voronoiEdge) -> {
		//				return graphs.dualTovoronoi.get(voronoiEdge);
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

		//		graphs.dualVertices.forEach((loc) -> {
		//			List<MapEdge> edgeList = graphs.dualGraph.edgesOf(loc).stream().map((voronoiEdge) -> {
		//				return graphs.dualTovoronoi.get(voronoiEdge);
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
		//		graphs.voronoiVertices.forEach((loc) -> {
		//			if (loc.elevation > .5) {
		//
		//				List<MapEdge> edgeList = graphs.voronoiGraph.edgesOf(loc).stream().map((voronoiEdge) -> {
		//					return graphs.voronoiTodual.get(voronoiEdge);
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
		//		graphs.dualEdges.forEach((edge) -> {
		//			//			if (Math.abs(edge.loc1.x - edge.loc2.x) > 0.001 && Math.abs(edge.loc1.y - edge.loc2.y) > 0.005
		//			//					) {//&& Math.abs(edge.loc1.x - edge.loc2.x) < 0.05 && Math.abs(edge.loc1.y - edge.loc2.y) < 0.05) {
		//			g.drawLine((int) (x0 + xWidth /2* edge.loc1.x), (int) (y0 + yHeight * minmax(0,1,edge.loc1.y)),
		//					(int) (x0 + xWidth /2* edge.loc2.x), (int) (y0 + yHeight * minmax(0,1,edge.loc2.y)));
		//			//			}
		//		});

		//		g.setStroke(new BasicStroke(3, 0, 0));
		//		g.setColor(Color.red);
		//		graphs.dualEdges.forEach((edge) -> {
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
