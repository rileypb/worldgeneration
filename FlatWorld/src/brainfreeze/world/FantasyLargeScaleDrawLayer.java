package brainfreeze.world;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.locationtech.jts.awt.PointTransformation;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.lwjgl.util.vector.Vector2f;

import brainfreeze.framework.GraphBuilder;
import brainfreeze.framework.WorldGeometry;

public class FantasyLargeScaleDrawLayer extends BaseDrawLayer {

	public static enum MapType {
		DEFAULT, TOPDOWN
	}

	private Random r;
	private int sizeFactor;
	private static final int BASE_SIZE_FACTOR = 70;
	private static final int MOUNTAIN_STYLE = 5;
	private static boolean SHOW_MOUNTAIN_CELLS = false;
	private static int WATER_STYLE = 2;
	private Color hillColor = new Color(.95f, .95f, .95f);
	private List<Location> pickList;
	private double fluxThreshold;
	private MapType mapType;
	private BufferedImage selectionTexture;
	double x0;
	double y0;
	double xWidth;
	double yHeight;
	private Graphs graphs;
	private List<Location> clippingPolygon;
	private BufferedImage distanceFromLand;
	private BufferedImage backBuffer1;
	private BufferedImage backBuffer2;
	private WorldGeometry geometry;

	public FantasyLargeScaleDrawLayer(Random r, int sizeFactor, Graphs graphs, double fluxThreshold, MapType mapType,
			BufferedImage selectionTexture, List<Location> clippingPolygon, WorldGeometry geometry) {
		this.r = r;
		this.sizeFactor = sizeFactor;
		this.graphs = graphs;
		this.fluxThreshold = fluxThreshold;
		this.mapType = mapType;
		this.selectionTexture = selectionTexture;
		this.clippingPolygon = clippingPolygon;
		this.geometry = geometry;

		if (clippingPolygon == null) {
			this.clippingPolygon = new ArrayList<Location>();
			this.clippingPolygon.add(new Location(0, 0));
			this.clippingPolygon.add(new Location(0, 1));
			this.clippingPolygon.add(new Location(1, 1));
			this.clippingPolygon.add(new Location(1, 0));
		}

		pickList = new CellPicker(graphs, 0.01).pick(r, 20);
	}

	@Override
	public void draw(BufferedImage im) {

		Graphics2D g = (Graphics2D) im.getGraphics();
		Rectangle clipBounds = g.getDeviceConfiguration().getBounds();

		//		Path2D.Double clip = new Path2D.Double();
		//		clip.moveTo(clippingPolygon.get(0).getX(), clippingPolygon.get(0).getY());
		//		for (int i = 1; i < clippingPolygon.size(); i++) {
		//			clip.lineTo(clippingPolygon.get(i).getX(), clippingPolygon.get(i).getY());
		//		}
		//		clip.closePath();
		//		clip.
		//		g.setClip(clip);	

		g.setColor(Color.blue);
		g.fill(clipBounds);

		Graphics2D graphics = (Graphics2D) selectionTexture.getGraphics();
		graphics.setColor(Color.white);
		graphics.fillRect(0, 0, selectionTexture.getWidth(), selectionTexture.getHeight());

		x0 = clipBounds.x;
		y0 = clipBounds.y;
		xWidth = clipBounds.width * 1;
		yHeight = clipBounds.height * 1;

		backBuffer1 = new BufferedImage((int) xWidth, (int) yHeight, BufferedImage.TYPE_4BYTE_ABGR);
		backBuffer2 = new BufferedImage((int) xWidth, (int) yHeight, BufferedImage.TYPE_4BYTE_ABGR);

		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

		g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		setBaseColors(graphs);

		averageNeighborColors(graphs);

		setVertexColors(graphs, Color.LIGHT_GRAY);

		distanceFromLand = new BufferedImage(clipBounds.width, clipBounds.height, BufferedImage.TYPE_3BYTE_BGR);

		initializeDistanceFromLand();

		drawCells(g, graphs, x0, y0, xWidth, yHeight);

		//		fillInDistanceFromLand3();

//		drawRoads(g, graphs, x0, y0, xWidth, yHeight);
//
//		drawSecondaryRoads(g, graphs, x0, y0, xWidth, yHeight);
//
//		drawRivers(g, graphs, x0, y0, xWidth, yHeight);
//
//		drawWater(g, graphs, x0, y0, xWidth, yHeight);
//
//		drawCoast(g, graphs, x0, y0, xWidth, yHeight);
//
//		drawPickListCircles(g, graphs, x0, y0, xWidth, yHeight);
//
//		//		drawLabels(g, graphs, x0, y0, xWidth, yHeight);
//
//		//		drawMountains(g, graphs, x0, y0, xWidth, yHeight);
//		clip(g, geometry);

	}

	private void initializeDistanceFromLand() {
		//		Graphics2D g = (Graphics2D) distanceFromLand.getGraphics();
		//		g.setColor(new Color(10000));
		//		g.fillRect(0, 0, (int) xWidth, (int) yHeight);

		for (int x = 0; x < xWidth; x++) {
			for (int y = 0; y < yHeight; y++) {
				distanceFromLand.getRaster().setPixel(x, y, new int[] { 255, 255, 255 });
				//				distanceFromLand.setRGB(x, y, 10000);
			}
		}
	}

	private void fillInDistanceFromLand3() {
		List<java.awt.Point> points = new ArrayList<java.awt.Point>();
		WritableRaster raster = distanceFromLand.getRaster();
		int[] pixel = new int[3];

		for (int x = 0; x < xWidth; x++) {
			for (int y = 0; y < yHeight; y++) {
				raster.getPixel(x, y, pixel);
				int current = (pixel[0] << 16) + (pixel[1] << 8) + pixel[2];
				if (current == 0) {
					points.add(new java.awt.Point(x, y));
				}
			}
		}

		while (points.size() > 0) {
			java.awt.Point point = points.remove(0);
			int x = point.x;
			int y = point.y;
			raster.getPixel(x, y, pixel);
			int current = (pixel[0] << 16) + (pixel[1] << 8) + pixel[2];

			for (int u = -1; u <= 1; u++) {
				for (int v = -1; v <= 1; v++) {
					int x1 = x + u;
					int y1 = y + v;
					if (x1 >= 0 && x1 < xWidth && y1 >= 0 && y1 < yHeight) {
						//								getPixel(rasterData, x1, y1, (int) xWidth, pixel);
						raster.getPixel(x1, y1, pixel);
						int thisone = (pixel[0] << 16) + (pixel[1] << 8) + pixel[2];
						if (thisone > current + 1) {
							int setTo = current + 1;
							pixel[0] = (setTo >> 16) & 255;
							pixel[1] = (setTo >> 8) & 255;
							pixel[2] = setTo & 255;
							raster.setPixel(x1, y1, pixel);
							points.add(new java.awt.Point(x1, y1));
						}
					}
				}
			}
		}
	}

	private void fillInDistanceFromLand() {
		WritableRaster raster = distanceFromLand.getRaster();
		//		byte[] rasterData = ((DataBufferByte) raster.getDataBuffer()).getData();
		int[] pixel = new int[3];

		int c = 0;
		boolean changed = true;
		while (changed && c < 400) {
			c++;
			//			System.out.println(c++);
			changed = false;
			for (int x = 0; x < xWidth; x++) {
				for (int y = 0; y < yHeight; y++) {
					//					getPixel(rasterData, x, y, (int) xWidth, pixel);
					raster.getPixel(x, y, pixel);
					int current = (pixel[0] << 16) + (pixel[1] << 8) + pixel[2];
					if (current == 0) {
						continue;
					}
					int min = Integer.MAX_VALUE;
					for (int u = -1; u <= 1; u++) {
						for (int v = -1; v <= 1; v++) {
							int x1 = x + u;
							int y1 = y + v;
							if (x1 >= 0 && x1 < xWidth && y1 >= 0 && y1 < yHeight) {
								//								getPixel(rasterData, x1, y1, (int) xWidth, pixel);
								raster.getPixel(x1, y1, pixel);
								int thisone = (pixel[0] << 16) + (pixel[1] << 8) + pixel[2];
								min = Math.min(min, thisone);
							}
						}

					}
					if (min + 1 < current) {
						if (c > 0) {
							int a = 0;
						}
						min++;
						changed = true;
						pixel[0] = (min >> 16) & 255;
						pixel[1] = (min >> 8) & 255;
						pixel[2] = min & 255;
						raster.setPixel(x, y, pixel);
						//						setPixel(rasterData, x, y, (int) xWidth, pixel);
						//						System.out.println(Arrays.toString(pixel));
					}
				}
			}
		}

	}

	private void fillInDistanceFromLand2() {
		WritableRaster raster = distanceFromLand.getRaster();
		//		byte[] rasterData = ((DataBufferByte) raster.getDataBuffer()).getData();
		int[] pixel = new int[3];

		int c = 0;
		boolean changed = true;
		while (changed && c < 400) {
			c++;
			//			System.out.println(c++);
			changed = false;
			for (int x = 0; x < xWidth; x++) {
				for (int y = 0; y < yHeight; y++) {
					//					getPixel(rasterData, x, y, (int) xWidth, pixel);
					raster.getPixel(x, y, pixel);
					int current = (pixel[0] << 16) + (pixel[1] << 8) + pixel[2];
					if (current == 0) {
						continue;
					}
					int min = Integer.MAX_VALUE;
					int u = 0;
					for (int v = -1; v <= 1; v++) {
						int x1 = x + u;
						int y1 = y + v;
						if (x1 >= 0 && x1 < xWidth && y1 >= 0 && y1 < yHeight) {
							//								getPixel(rasterData, x1, y1, (int) xWidth, pixel);
							raster.getPixel(x1, y1, pixel);
							int thisone = (pixel[0] << 16) + (pixel[1] << 8) + pixel[2];
							min = Math.min(min, thisone);
						}
					}

					if (min + 1 < current) {
						if (c > 0) {
							int a = 0;
						}
						min++;
						changed = true;
						pixel[0] = (min >> 16) & 255;
						pixel[1] = (min >> 8) & 255;
						pixel[2] = min & 255;
						raster.setPixel(x, y, pixel);
						//						setPixel(rasterData, x, y, (int) xWidth, pixel);
						//						System.out.println(Arrays.toString(pixel));
					}
				}
				for (int y = (int) (yHeight - 1); y >= 0; y--) {
					//					getPixel(rasterData, x, y, (int) xWidth, pixel);
					raster.getPixel(x, y, pixel);
					int current = (pixel[0] << 16) + (pixel[1] << 8) + pixel[2];
					if (current == 0) {
						continue;
					}
					int min = Integer.MAX_VALUE;
					int u = 0;
					for (int v = -1; v <= 1; v++) {
						int x1 = x + u;
						int y1 = y + v;
						if (x1 >= 0 && x1 < xWidth && y1 >= 0 && y1 < yHeight) {
							//								getPixel(rasterData, x1, y1, (int) xWidth, pixel);
							raster.getPixel(x1, y1, pixel);
							int thisone = (pixel[0] << 16) + (pixel[1] << 8) + pixel[2];
							min = Math.min(min, thisone);
						}
					}

					if (min + 1 < current) {
						if (c > 0) {
							int a = 0;
						}
						min++;
						changed = true;
						pixel[0] = (min >> 16) & 255;
						pixel[1] = (min >> 8) & 255;
						pixel[2] = min & 255;
						raster.setPixel(x, y, pixel);
						//						setPixel(rasterData, x, y, (int) xWidth, pixel);
						//						System.out.println(Arrays.toString(pixel));
					}
				}
			}

			for (int y = 0; y < yHeight; y++) {
				for (int x = 0; x < xWidth; x++) {
					//					getPixel(rasterData, x, y, (int) xWidth, pixel);
					raster.getPixel(x, y, pixel);
					int current = (pixel[0] << 16) + (pixel[1] << 8) + pixel[2];
					if (current == 0) {
						continue;
					}
					int min = Integer.MAX_VALUE;
					int v = 0;
					for (int u = -1; u <= 1; u++) {
						int x1 = x + u;
						int y1 = y + v;
						if (x1 >= 0 && x1 < xWidth && y1 >= 0 && y1 < yHeight) {
							//								getPixel(rasterData, x1, y1, (int) xWidth, pixel);
							raster.getPixel(x1, y1, pixel);
							int thisone = (pixel[0] << 16) + (pixel[1] << 8) + pixel[2];
							min = Math.min(min, thisone);
						}
					}

					if (min + 1 < current) {
						if (c > 0) {
							int a = 0;
						}
						min++;
						changed = true;
						pixel[0] = (min >> 16) & 255;
						pixel[1] = (min >> 8) & 255;
						pixel[2] = min & 255;
						raster.setPixel(x, y, pixel);
						//						setPixel(rasterData, x, y, (int) xWidth, pixel);
						//						System.out.println(Arrays.toString(pixel));
					}
				}
				for (int x = (int) (xWidth - 1); x >= 0; x--) {
					//					getPixel(rasterData, x, y, (int) xWidth, pixel);
					raster.getPixel(x, y, pixel);
					int current = (pixel[0] << 16) + (pixel[1] << 8) + pixel[2];
					if (current == 0) {
						continue;
					}
					int min = Integer.MAX_VALUE;
					int v = 0;
					for (int u = -1; u <= 1; u++) {
						int x1 = x + u;
						int y1 = y + v;
						if (x1 >= 0 && x1 < xWidth && y1 >= 0 && y1 < yHeight) {
							//								getPixel(rasterData, x1, y1, (int) xWidth, pixel);
							raster.getPixel(x1, y1, pixel);
							int thisone = (pixel[0] << 16) + (pixel[1] << 8) + pixel[2];
							min = Math.min(min, thisone);
						}
					}

					if (min + 1 < current) {
						if (c > 0) {
							int a = 0;
						}
						min++;
						changed = true;
						pixel[0] = (min >> 16) & 255;
						pixel[1] = (min >> 8) & 255;
						pixel[2] = min & 255;
						raster.setPixel(x, y, pixel);
						//						setPixel(rasterData, x, y, (int) xWidth, pixel);
						//						System.out.println(Arrays.toString(pixel));
					}
				}
			}
		}

	}

	//	private void setPixel(byte[] rasterData, int x, int y, int width, int[] pixel) {
	//		rasterData[3 * (y * width + x)] = (byte) pixel[0];
	//		rasterData[3 * (y * width + x) + 1] = (byte) pixel[1];
	//		rasterData[3 * (y * width + x) + 2] = (byte) pixel[2];
	//	}
	//
	//	private void getPixel(byte[] rasterData, int x, int y, int width, int[] pixel) {
	//		pixel[0] = rasterData[3 * (y * width + x)];
	//		pixel[1] = rasterData[3 * (y * width + x) + 1];
	//		pixel[2] = rasterData[3 * (y * width + x) + 2];
	//	}

	private void clip(Graphics2D g, WorldGeometry geometry) {

		Object save = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

		GeometryFactory geomFact = new GeometryFactory();
		Geometry clipGeometry = GraphBuilder.buildClipGeometry(clippingPolygon, geomFact, geometry);
		Geometry clipHull = clipGeometry.convexHull();
		Coordinate[] rectCoords = new Coordinate[] { new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1),
				new Coordinate(0, 1), new Coordinate(0, 0) };
		Geometry rect = geomFact.createPolygon(rectCoords);
		Geometry rectHull = rect.convexHull();
		Geometry inverseGeometry = rectHull.difference(clipHull);

		PointTransformation txfm = new PointTransformation() {

			@Override
			public void transform(Coordinate src, Point2D dest) {
				dest.setLocation(x0 + src.x * xWidth, y0 + src.y * yHeight);
			}
		};
		Shape shape = new ShapeWriter(txfm).toShape(inverseGeometry);

		Point p = geomFact.createPoint(new Coordinate(1, 1));
		double distance = p.distance(clipGeometry);

		g.setColor(Color.black);
		g.setStroke(new BasicStroke(4, 2, 2));
		g.draw(shape);

		g.setPaint(new DistanceGradientPaint(clipGeometry, xWidth));
		g.fill(shape);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, save);
	}

	//	private void drawMountains(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight) {
	//		List<Location> flatList = pickList.stream().flatMap((l) -> {
	//			return l.stream();
	//		}).filter((loc) -> {
	//			return loc.mountain && !loc.water;
	//		}).collect(Collectors.toList());
	//
	//		flatList.sort((l1, l2) -> {
	//			return (int) (100000 * (l1.y - l2.y));
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
			Shape outline = vector.getOutline((float) (x0 + xWidth * loc.getX() + 10),
					(float) (y0 + yHeight * loc.getY() + 5));
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
			Shape outline = vector.getOutline((float) (x0 + xWidth * loc.getX() + 10),
					(float) (y0 + yHeight * loc.getY() + 5));
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
			g.drawLine((int) (x0 + edge.loc1.getX() * xWidth), (int) (y0 + edge.loc1.getY() * yHeight),
					(int) (x0 + edge.loc2.getX() * xWidth), (int) (y0 + edge.loc2.getY() * yHeight));
		});
	}

	private void drawSecondaryRoads(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight) {
		g.setColor(Color.black);

		g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[] { 1, 6 }, 0));
		graphs.dualEdges.stream().filter((edge) -> {
			return edge.secondaryRoad && !edge.road;
		}).forEach((edge) -> {
			g.drawLine((int) (x0 + edge.loc1.getX() * xWidth), (int) (y0 + edge.loc1.getY() * yHeight),
					(int) (x0 + edge.loc2.getX() * xWidth), (int) (y0 + edge.loc2.getY() * yHeight));
		});
	}

	private void drawPickListCircles(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight) {
		pickList.sort((l1, l2) -> {
			return (int) (100000 * (l1.getY() - l2.getY()));
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

		pickList.stream().filter((loc) -> {
			return !loc.water;
		}).forEach((loc) -> {
			//			g.setColor(Color.blue);
			//			g.setStroke(new BasicStroke(1));
			//			g.drawOval((int) (x0 + (loc.getX() - loc.radius) * xWidth),
			//					(int) (y0 + (loc.getY() - loc.radius) * yHeight), (int) (x0 + 2 * loc.radius * xWidth),
			//					(int) (y0 + 2 * loc.radius * yHeight));

			if (loc.hill && !loc.mountain && !loc.water) {
				drawHill(g, x0, y0, xWidth, yHeight, 0.015, loc.getX() - loc.drawRadius / 2, loc.getY(), loc);
			} else if (loc.mountain && !loc.water) {
				drawMountain(g, graphs, x0, y0, xWidth, yHeight, MOUNTAIN_STYLE, loc);
				//				drawMountain(g, 1, x0, y0, xWidth, yHeight, 0.015, loc.x - loc.radius / 2, loc.y);
			} else if (loc.forest && !loc.water) {
				drawTree(g, x0, y0, xWidth, yHeight, loc.getY() + loc.drawRadius / 2, 0.015,
						loc.getX() - loc.drawRadius / 2);
			}
		});

		g.setColor(Color.black);
		graphs.dualVertices.stream().filter((loc) -> {
			return loc.city;
		}).forEach((loc) -> {
			g.fillOval((int) (x0 + loc.getX() * xWidth - 3), (int) (y0 + loc.getY() * yHeight - 3), 6, 6);
			g.drawOval((int) (x0 + loc.getX() * xWidth - 6), (int) (y0 + loc.getY() * yHeight - 6), 12, 12);
		});

		g.setColor(Color.black);
		graphs.dualVertices.stream().filter((loc) -> {
			return loc.town;
		}).forEach((loc) -> {
			g.drawOval((int) (x0 + loc.getX() * xWidth - 3), (int) (y0 + loc.getY() * yHeight - 3), 6, 6);
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
			g.drawLine((int) (x0 + xWidth * loc.getX()), (int) (y0 + yHeight * loc.getY()),
					(int) (x0 + xWidth * v.getX()), (int) (y0 + yHeight * v.getY()));
		});
	}

	private void drawMountainStyle4(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight,
			Location loc) {
		double jiggleX = (r.nextDouble() - 0.5) * loc.drawRadius * 0.5;
		double jiggleY = (r.nextDouble() - 0.5) * loc.drawRadius;
		double jiggleX2 = (r.nextDouble() - 0.5) * loc.drawRadius * 0.5;
		double jiggleY2 = (r.nextDouble() - 0.5) * loc.drawRadius;

		Path2D p = new Path2D.Double();
		p.moveTo(x0 + (loc.getX() - loc.drawRadius) * xWidth, y0 + loc.getY() * yHeight);
		//			p.lineTo(x0 + loc.x * xWidth, y0 + (loc.y - loc.radius) * yHeight);
		//			p.lineTo(x0 + (loc.x + loc.radius) * xWidth, y0 + loc.y * yHeight);

		p.curveTo(x0 + (loc.getX() - 2 * loc.drawRadius / 3 + jiggleX) * xWidth,
				y0 + (loc.getY() - loc.drawRadius / 3 + jiggleY) * yHeight,
				x0 + (loc.getX() - loc.drawRadius / 3 + jiggleX) * xWidth,
				y0 + (loc.getY() - 2 * loc.drawRadius / 3) * yHeight, x0 + (loc.getX()) * xWidth,
				y0 + (loc.getY() - loc.drawRadius) * yHeight);
		p.curveTo(x0 + (loc.getX() + loc.drawRadius / 3 - jiggleX2) * xWidth,
				y0 + (loc.getY() - 2 * loc.drawRadius / 3 + jiggleY2) * yHeight,
				x0 + (loc.getX() + 2 * loc.drawRadius / 3 - jiggleX2) * xWidth,
				y0 + (loc.getY() - loc.drawRadius / 3) * yHeight, x0 + (loc.getX() + loc.drawRadius) * xWidth,
				y0 + loc.getY() * yHeight);

		g.setPaint(new GradientPaint((float) (x0 + (loc.getX() - loc.drawRadius) * xWidth), (float) loc.getY(),
				new Color(0.9f, 0.9f, 0.9f), (float) (x0 + (loc.getX() + loc.drawRadius) * xWidth), (float) loc.getY(),
				new Color(0.5f, 0.5f, 0.5f), false));

		g.fill(p);
		g.setColor(Color.DARK_GRAY);
		g.setStroke(new BasicStroke(1.5f));
		g.draw(p);
	}

	private void drawMountainStyle0(Graphics2D g, double x0, double y0, double xWidth, double yHeight, Location loc) {
		Path2D p = new Path2D.Double();
		p.moveTo(x0 + (loc.getX() - loc.drawRadius) * xWidth, y0 + loc.getY() * yHeight);

		p.lineTo(x0 + (loc.getX()) * xWidth, y0 + (loc.getY() - loc.drawRadius) * yHeight);
		p.lineTo(x0 + (loc.getX() + loc.drawRadius) * xWidth, y0 + loc.getY() * yHeight);

		//		g.setColor(Color.LIGHT_GRAY);
		//		g.fill(p);
		g.setColor(Color.DARK_GRAY);
		g.draw(p);
	}

	private void drawMountainStyle3(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight,
			Location loc) {

		List<MapEdge> voronoiEdges = new ArrayList<>(loc.sides);
		voronoiEdges.sort((e1, e2) -> {
			double y1 = (e1.loc1.getY() + e1.loc2.getY()) / 2;
			double y2 = (e2.loc1.getY() + e2.loc2.getY()) / 2;
			return (int) (1000000 * (y1 - y2));
		});

		double minY = voronoiEdges.stream().mapToDouble((e) -> {
			return Math.min(e.loc1.getY(), e.loc2.getY());
		}).min().getAsDouble();
		double maxX = voronoiEdges.stream().mapToDouble((e) -> {
			return Math.max(e.loc1.getX(), e.loc2.getX());
		}).max().getAsDouble();
		double minX = voronoiEdges.stream().mapToDouble((e) -> {
			return Math.min(e.loc1.getX(), e.loc2.getX());
		}).min().getAsDouble();

		double avgX = (minX + maxX) / 2;

		g.setColor(Color.LIGHT_GRAY);
		double peakY = minY - (maxX - minX) / 8;

		Path2D.Double p2 = new Path2D.Double();
		p2.moveTo(x0 + xWidth * minX, y0 + yHeight * loc.getY());
		p2.lineTo(x0 + xWidth * loc.getX(), y0 + yHeight * peakY);
		p2.lineTo(x0 + xWidth * avgX, y0 + yHeight * loc.getY());
		g.setColor(Color.DARK_GRAY);
		g.fill(p2);

		Path2D.Double p3 = new Path2D.Double();
		p3.moveTo(x0 + xWidth * avgX, y0 + yHeight * loc.getY());
		p3.lineTo(x0 + xWidth * loc.getX(), y0 + yHeight * peakY);
		p3.lineTo(x0 + xWidth * maxX, y0 + yHeight * loc.getY());
		g.setColor(new Color(0.5f, 0.5f, 0.5f));
		g.fill(p3);

	}

	private void drawMountainStyle1(Graphics2D g, double x0, double y0, double xWidth, double yHeight, Location loc) {
		double jiggleX = (r.nextDouble() - 0.5) * loc.drawRadius * 0.5;
		double jiggleY = (r.nextDouble() - 0.5) * loc.drawRadius;
		double jiggleX2 = (r.nextDouble() - 0.5) * loc.drawRadius * 0.5;
		double jiggleY2 = (r.nextDouble() - 0.5) * loc.drawRadius;

		Path2D p = new Path2D.Double();
		p.moveTo(x0 + (loc.getX() - loc.drawRadius) * xWidth, y0 + loc.getY() * yHeight);
		//			p.lineTo(x0 + loc.x * xWidth, y0 + (loc.y - loc.radius) * yHeight);
		//			p.lineTo(x0 + (loc.x + loc.radius) * xWidth, y0 + loc.y * yHeight);

		p.curveTo(x0 + (loc.getX() - 2 * loc.drawRadius / 3 + jiggleX) * xWidth,
				y0 + (loc.getY() - loc.drawRadius / 3 + jiggleY) * yHeight,
				x0 + (loc.getX() - loc.drawRadius / 3 + jiggleX) * xWidth,
				y0 + (loc.getY() - 2 * loc.drawRadius / 3) * yHeight, x0 + (loc.getX()) * xWidth,
				y0 + (loc.getY() - loc.drawRadius) * yHeight);
		p.curveTo(x0 + (loc.getX() + loc.drawRadius / 3 - jiggleX2) * xWidth,
				y0 + (loc.getY() - 2 * loc.drawRadius / 3 + jiggleY2) * yHeight,
				x0 + (loc.getX() + 2 * loc.drawRadius / 3 - jiggleX2) * xWidth,
				y0 + (loc.getY() - loc.drawRadius / 3) * yHeight, x0 + (loc.getX() + loc.drawRadius) * xWidth,
				y0 + loc.getY() * yHeight);

		g.setStroke(new BasicStroke(1.5f));
		g.setColor(Color.LIGHT_GRAY);
		g.fill(p);
		g.setColor(Color.DARK_GRAY);
		g.draw(p);
	}

	private void drawMountainStyle5(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight,
			Location loc) {
		drawMountainStyle4(g, graphs, x0, y0, xWidth, yHeight, loc);

		double jiggleX = (r.nextDouble() - 0.5) * loc.drawRadius * 0.5;
		double jiggleY = (r.nextDouble() - 0.5) * loc.drawRadius;
		double jiggleX2 = (r.nextDouble() - 0.5) * loc.drawRadius * 0.5;
		double jiggleY2 = (r.nextDouble() - 0.5) * loc.drawRadius;

		Path2D p = new Path2D.Double();
		p.moveTo(x0 + (loc.getX() - loc.drawRadius / 2) * xWidth, y0 + loc.getY() * yHeight);
		//			p.lineTo(x0 + loc.x * xWidth, y0 + (loc.y - loc.radius) * yHeight);
		//			p.lineTo(x0 + (loc.x + loc.radius) * xWidth, y0 + loc.y * yHeight);

		p.curveTo(x0 + (loc.getX() - loc.drawRadius / 3 + jiggleX) * xWidth,
				y0 + (loc.getY() - loc.drawRadius / 3 + jiggleY) * yHeight,
				x0 + (loc.getX() - loc.drawRadius / 6 + jiggleX) * xWidth,
				y0 + (loc.getY() - 2 * loc.drawRadius / 3) * yHeight, x0 + (loc.getX()) * xWidth,
				y0 + (loc.getY() - loc.drawRadius) * yHeight);

		g.setStroke(new BasicStroke(0.5f));
		g.setColor(Color.DARK_GRAY);
		g.draw(p);
	}

	private void drawMountainStyle2(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight,
			Location loc) {
		List<MapEdge> voronoiEdges = loc.sides.stream().filter((e) -> {
			return e != null;
		}).collect(Collectors.toList());
		voronoiEdges.sort((e1, e2) -> {
			double y1 = (e1.loc1.getY() + e1.loc2.getY()) / 2;
			double y2 = (e2.loc1.getY() + e2.loc2.getY()) / 2;
			return (int) (100000 * (y1 - y2));
		});

		double minY = voronoiEdges.stream().mapToDouble((e) -> {
			return Math.min(e.loc1.getY(), e.loc2.getY());
		}).min().getAsDouble();
		double maxX = voronoiEdges.stream().mapToDouble((e) -> {
			return Math.max(e.loc1.getX(), e.loc2.getX());
		}).max().getAsDouble();
		double minX = voronoiEdges.stream().mapToDouble((e) -> {
			return Math.min(e.loc1.getX(), e.loc2.getX());
		}).min().getAsDouble();

		Vector2f light = new Vector2f(1, 0);

		Graphics2D g1 = (Graphics2D) backBuffer1.getGraphics();

		g1.setColor(new Color(0, 1, 1, 1));
		g1.fillRect(0, 0, (int) xWidth, (int) yHeight);
		double peakY = minY;// - (maxX - minX) / 16;

		int minDisplayX = Integer.MAX_VALUE;
		int maxDisplayX = Integer.MIN_VALUE;
		int minDisplayY = Integer.MAX_VALUE;
		int maxDisplayY = Integer.MIN_VALUE;

		for (MapEdge e : voronoiEdges) {
			Path2D.Double p2 = new Path2D.Double();

			Vector2f v0 = new Vector2f((float) (loc.getX() - (e.loc1.getX() + e.loc2.getX()) / 2),
					(float) (loc.getY() - (e.loc1.getY() + e.loc2.getY()) / 2)).normalise(null);
			Vector2f v1 = new Vector2f((float) (e.loc1.getX() - e.loc2.getX()), (float) (e.loc1.getY() - e.loc2.getY()))
					.normalise(null);
			float dot0 = Vector2f.dot(light, v0);
			float dot = Vector2f.dot(v0, v1);
			if (dot0 * dot > 0) {
				dot *= -1;
			}
			float c = 0.2f + (1 + dot) * 0.3f;
			g.setColor(new Color(c, c, c));

			int x1 = (int) (x0 + xWidth * e.loc1.getX());
			int y1 = (int) (y0 + yHeight * e.loc1.getY());
			p2.moveTo(x1, y1);
			int x2 = (int) (x0 + xWidth * e.loc2.getX());
			int y2 = (int) (y0 + yHeight * e.loc2.getY());
			p2.lineTo(x2, y2);
			int x3 = (int) (x0 + xWidth * loc.getX());
			int y3 = (int) (y0 + yHeight * peakY);
			p2.lineTo(x3, y3);

			minDisplayX = min(minDisplayX, x1, x2, x3);
			maxDisplayX = max(maxDisplayX, x1, x2, x3);
			minDisplayY = min(minDisplayY, y1, y2, y3);
			maxDisplayY = max(maxDisplayY, y1, y2, y3);

			//				g.setColor(Color.LIGHT_GRAY);
			g.fill(p2);
			//				g.setColor(Color.DARK_GRAY);
			//				g.draw(p2);
		}

		//		Graphics2D g2 = (Graphics2D) backBuffer1.getGraphics();
		//		Perlin p = new Perlin();
		//		double dx;
		//		double dy;
		//		for (int u = minDisplayX; u <= maxDisplayX; u++) {
		//			for (int v = minDisplayY; v <= maxDisplayY; v++) {
		//				dx = 8 * p.getValue(0.5, u * 0.001, v * 0.001);
		//				dy = 8 * p.getValue(1.5, u * 0.001, v * 0.001);
		//				double dx1 = 8 * p.getValue(2.5, u * 0.001, v * 0.001);
		//				double dy1 = 8 * p.getValue(3.5, u * 0.001, v * 0.001);
		//				int rgb = backBuffer1.getRGB(u, v);
		//				int rgb2 = backBuffer1.getRGB((int) (u + dx1), (int) (v + dy1));
		//				int rgb3 = averageColor(rgb, rgb2);
		//				backBuffer2.setRGB((int) (u + dx), (int) (v + dy), rgb3);
		//			}
		//		}
		//
		//		g.drawImage(backBuffer2, minDisplayX, minDisplayY, maxDisplayX, maxDisplayY, minDisplayX, minDisplayY,
		//				maxDisplayX, maxDisplayY, null);

	}

	private int averageColor(int rgb, int rgb2) {
		int a = (rgb >> 24) & 255;
		int r = (rgb >> 16) & 255;
		int g = (rgb >> 8) & 255;
		int b = rgb & 255;
		int a2 = (rgb2 >> 16) & 255;
		int r2 = (rgb2 >> 16) & 255;
		int g2 = (rgb2 >> 8) & 255;
		int b2 = rgb2 & 255;
		int a3 = (a + a2) / 2;
		int r3 = (r + r2) / 2;
		int g3 = (g + g2) / 2;
		int b3 = (b + b2) / 2;
		int rgb3 = (a << 24) + (r3 << 16) + (g3 << 8) + b3;
		return rgb3;
	}

	private int max(int... val) {
		int max = Integer.MIN_VALUE;
		for (int i : val) {
			max = Math.max(max, i);
		}
		return max;
	}

	private int min(int... val) {
		int min = Integer.MAX_VALUE;
		for (int i : val) {
			min = Math.min(min, i);
		}
		return min;
	}

	private void drawCoast(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight) {
		graphs.voronoiEdges.forEach((e) -> {
			g.setStroke(new BasicStroke(1.5f));
			Set<Location> adjacentCells = e.adjacentCells;
			if (adjacentCells.size() == 2) {
				Iterator<Location> it = adjacentCells.iterator();
				Location loc1 = it.next();
				Location loc2 = it.next();
				if (loc1.water != loc2.water) {
					g.setColor(Color.black);
					g.setStroke(new BasicStroke(1));
					g.drawLine((int) (x0 + e.loc1.getX() * xWidth), (int) (y0 + e.loc1.getY() * yHeight),
							(int) (x0 + e.loc2.getX() * xWidth), (int) (y0 + e.loc2.getY() * yHeight));
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
		g.setPaint(new GradientPaint((float) (x0 + xa * xWidth), (float) loc.getY(), hillColor,
				(float) (x0 + (xa + maxD) * xWidth), (float) loc.getY(), Color.lightGray, false));

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
		if (WATER_STYLE == 0) {
			drawWaterStyle0(g, graphs, x0, y0, xWidth, yHeight);
		} else if (WATER_STYLE == 1) {
			drawWaterStyle1(g, graphs, x0, y0, xWidth, yHeight);
		} else if (WATER_STYLE == 2) {
			drawWaterStyle2(g, graphs, x0, y0, xWidth, yHeight);
		}
	}

	private void drawWaterStyle1(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight) {
		g.setPaint(new TexturePaint(distanceFromLand, new Rectangle2D.Double(0, 0, xWidth, yHeight)));
		graphs.dualVertices.forEach((loc) -> {
			Set<MapEdge> edges = loc.sides;

			if (loc.water) {
				edges.forEach((e) -> {
					if (e != null) {
						Path2D.Double p = new Path2D.Double();
						p.moveTo(x0 + xWidth * loc.getX(), y0 + yHeight * loc.getY());
						p.lineTo(x0 + xWidth * e.loc1.getX(), y0 + yHeight * e.loc1.getY());
						p.lineTo(x0 + xWidth * e.loc2.getX(), y0 + yHeight * e.loc2.getY());
						p.closePath();
						g.fill(p);
					}
				});
			}
		});
		//		g.drawImage(distanceFromLand, 0, 0, null);
	}

	private void drawWaterStyle2(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight) {
		drawWaterStyle0(g, graphs, x0, y0, xWidth, yHeight);
		WritableRaster raster = distanceFromLand.getRaster();
		int[] pixel = new int[3];
		g.setColor(new Color(220, 220, 220));
		for (int x = 0; x < xWidth; x++) {
			for (int y = 0; y < yHeight; y++) {
				raster.getPixel(x, y, pixel);
				if (pixel[0] == 0 && pixel[1] == 0 && pixel[2] == 8) {
					g.drawRect(x, y, 1, 1);
				}
			}
		}
		g.setColor(new Color(210, 210, 210));
		for (int x = 0; x < xWidth; x++) {
			for (int y = 0; y < yHeight; y++) {
				raster.getPixel(x, y, pixel);
				if (pixel[0] == 0 && pixel[1] == 0 && pixel[2] == 6) {
					g.drawRect(x, y, 1, 1);
				}
			}
		}
		g.setColor(new Color(200, 200, 200));
		for (int x = 0; x < xWidth; x++) {
			for (int y = 0; y < yHeight; y++) {
				raster.getPixel(x, y, pixel);
				if (pixel[0] == 0 && pixel[1] == 0 && pixel[2] == 4) {
					g.drawRect(x, y, 1, 1);
				}
			}
		}
	}

	private void drawWaterStyle0(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight) {
		graphs.dualVertices.forEach((loc) -> {
			Set<MapEdge> edges = loc.sides;

			if (loc.water) {
				edges.forEach((e) -> {
					if (e != null) {
						Path2D.Double p = new Path2D.Double();
						p.moveTo(x0 + xWidth * loc.getX(), y0 + yHeight * loc.getY());
						p.lineTo(x0 + xWidth * e.loc1.getX(), y0 + yHeight * e.loc1.getY());
						p.lineTo(x0 + xWidth * e.loc2.getX(), y0 + yHeight * e.loc2.getY());
						p.closePath();
						g.setColor(Color.LIGHT_GRAY);
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
				g.drawLine((int) (x0 + xWidth * e.loc1.getX()), (int) (y0 + yHeight * e.loc1.getY()),
						(int) (x0 + xWidth * e.loc2.getX()), (int) (y0 + yHeight * e.loc2.getY()));
			}
		});
	}

	private void drawCells(Graphics2D g, Graphs graphs, double x0, double y0, double xWidth, double yHeight) {
		int[] i = new int[1];
		Graphics2D g2 = (Graphics2D) selectionTexture.getGraphics();
		Graphics2D g3 = (Graphics2D) distanceFromLand.getGraphics();
		graphs.dualVertices.forEach((loc) -> {
			Set<MapEdge> edgeList = loc.sides;
			loc.index = i[0];

			edgeList.forEach((e) -> {
				if (e != null) {
					Path2D.Double p = new Path2D.Double();
					p.moveTo(x0 + xWidth * loc.getX(), y0 + yHeight * loc.getY());
					p.lineTo(x0 + xWidth * e.loc1.getX(), y0 + yHeight * e.loc1.getY());
					p.lineTo(x0 + xWidth * e.loc2.getX(), y0 + yHeight * e.loc2.getY());
					p.closePath();

					Point2D.Double pt1 = new Point2D.Double(x0 + xWidth * loc.getX(), y0 + yHeight * loc.getY());
					Point2D.Double pt2 = new Point2D.Double(x0 + xWidth * e.loc1.getX(), y0 + yHeight * e.loc1.getY());
					Point2D.Double pt3 = new Point2D.Double(x0 + xWidth * e.loc2.getX(), y0 + yHeight * e.loc2.getY());

					if (loc.water) {
						g.setColor(loc.color);
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

					if (!loc.water) {
						g3.setColor(new Color(0));
						g3.fill(p);
						g3.draw(p);
					}
				}

				if (SHOW_MOUNTAIN_CELLS && loc.mountain && !loc.water) {
					g.setColor(Color.red);
					loc.sides.forEach((side) -> {
						g.drawLine((int) (x0 + side.loc1.getX() * xWidth), (int) (y0 + side.loc1.getY() * yHeight),
								(int) (x0 + side.loc2.getX() * xWidth), (int) (y0 + side.loc2.getY() * yHeight));
					});
					g.setColor(Color.blue);
					g.drawOval((int) (x0 + loc.getX() * xWidth - 2), (int) (y0 + loc.getY() * yHeight - 2), 4, 4);
				}
			});
			i[0]++;
		});

		//		graphs.dualVertices.forEach((loc) -> {
		//
		//			Color c = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
		//			List<MapEdge> edgeList = graphs.dualGraph.edgesOf(loc).stream().map((voronoiEdge) -> {
		//				return graphs.dualToVoronoi.get(voronoiEdge);
		//			}).collect(Collectors.toList());
		//			edgeList.forEach((e) -> {
		//				if (e != null) {
		//					Path2D.Double p = new Path2D.Double();
		//					p.moveTo(x0 + xWidth * loc.getX(), y0 + yHeight * minmax(0, 1, loc.getY()));
		//					p.lineTo(x0 + xWidth * e.loc1.getX(), y0 + yHeight * minmax(0, 1, e.loc1.getY()));
		//					p.lineTo(x0 + xWidth * e.loc2.getX(), y0 + yHeight * minmax(0, 1, e.loc2.getY()));
		//					p.closePath();
		//
		//					g.setColor(Color.black);
		//					g.draw(p);
		//					g.setColor(c);
		//					g.fill(p);
		//				}
		//			});
		//		});

	}

	private void averageNeighborColors(Graphs graphs) {
		graphs.dualVertices.forEach((loc) -> {
			List<Color> neighborColors = loc.adjacentCells.stream().map((v) -> {
				if (!graphs.dualVertices.contains(v)) {
					throw new IllegalStateException();
				}
				return v.baseColor;
			}).collect(Collectors.toList());

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
				//				loc.baseColor = Color.cyan;
				loc.baseColor = Color.lightGray;
			} else if (loc.hill) {
				loc.baseColor = hillColor;
			} else if (loc.mountain) {
				loc.baseColor = Color.LIGHT_GRAY;
			} else {
				loc.baseColor = Color.white;
			}
			//			loc.baseColor = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
		});
	}

	static double minmax(double min, double max, double value) {
		return Math.min(max, Math.max(min, value));
	}

}
