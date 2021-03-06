package brainfreeze.world;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.flowpowered.noise.module.source.Perlin;

import brainfreeze.framework.CalmAndWildPerlinHeightMap;
import brainfreeze.framework.HeightMap;
import brainfreeze.framework.PerlinHeightMap;
import brainfreeze.framework.Region;
import brainfreeze.framework.RegionBuilder;
import brainfreeze.framework.RegionParameters;
import brainfreeze.framework.TechnicalParameters;
import brainfreeze.framework.WorldGeometry;
import brainfreeze.framework.WorldParameters;
import brainfreeze.world.FantasyLargeScaleDrawLayer.MapType;

public class MapperMain {

	private static final class MapCanvas extends Canvas {
		private final BufferedImage img;
		private int selectionIndex;
		private Graphs graphs;
		private Location[] cells;

		private MapCanvas(BufferedImage img, Graphs graphs) {
			this.img = img;
			this.graphs = graphs;
			indexCells();
		}

		private void indexCells() {
			cells = new Location[graphs.dualVertices.size()];
			graphs.dualVertices.forEach((loc) -> {
				cells[loc.index] = loc;
			});
		}

		@Override
		public void paint(Graphics g) {
			((Graphics2D) g).scale(1, 1);
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(img, 0, 0, null);

			if (cells.length > selectionIndex) {
				Location loc = cells[selectionIndex];
				Set<MapEdge> edgeList = loc.sides;
				edgeList.forEach((e) -> {
					if (e != null) {
						g.setColor(Color.green);
						((Graphics2D) g).drawLine((int) (mapLayer.x0 + mapLayer.xWidth * e.loc1.getX()),
								(int) (mapLayer.y0
										+ mapLayer.yHeight * FantasyLargeScaleDrawLayer.minmax(0, 1, e.loc1.getY())),
								(int) (mapLayer.x0 + mapLayer.xWidth * e.loc2.getX()), (int) (mapLayer.y0
										+ mapLayer.yHeight * FantasyLargeScaleDrawLayer.minmax(0, 1, e.loc2.getY())));
					}
				});
			}

		}

		public void setSelectionIndex(int selectionIndex) {
			this.selectionIndex = selectionIndex;
		}
	}

	public static final double SEALEVEL = 0.1;
	public static final double MOUNTAIN_THRESHOLD = 0.9;
	private static int screenWidth;
	private static int screenHeight;

	public static final int POINTS = 12000;
	private static BufferedImage selectionTexture;
	private static FantasyLargeScaleDrawLayer mapLayer;

	public static void main(String[] args) {
		screenWidth = (int) (800d*Math.sqrt(POINTS)/Math.sqrt(12000));
		screenHeight = (int) (800d*Math.sqrt(POINTS)/Math.sqrt(12000));

		int seed = new Random().nextInt();
		//												seed = 13802760;
		//				seed = 1473019236;
		//		seed = 156788987;
		//				seed = -25911778;
		//		seed = 177265899;
		//		seed = 1842613904;
		//				seed = 1876874932;
		//		seed = -1931648976;
		//		seed = 1929928950; // mountains
		//		seed = 803011674;
		//		seed = -342907174; // lots of mountains
		
//		seed = -1191350163; // isthmus
		
//		 seed = 634863326; // disconnected towns
		
		// seed = 964928612; // mountain pass
		
		//seed = 329957084; // all in a row
		
		System.out.println("seed: " + seed);
		Random r = new Random(seed);

		Perlin perlin = new Perlin();
		perlin.setFrequency(1);
		perlin.setOctaveCount(1);
		perlin.setSeed(r.nextInt());
		Perlin calmPerlin = new Perlin();
		calmPerlin.setFrequency(.25);
		calmPerlin.setOctaveCount(16);
		calmPerlin.setSeed(r.nextInt());
		Perlin wildPerlin = new Perlin();
		wildPerlin.setFrequency(4);
		wildPerlin.setOctaveCount(30);
		wildPerlin.setSeed(r.nextInt());

//		WorldGeometry geometry = WorldGeometry.cylinder(0,30,0,10);
		WorldGeometry geometry = WorldGeometry.plane();
		HeightMap wildMap = new PerlinHeightMap(1, 1, wildPerlin, geometry);
		HeightMap mixtureMap = new PerlinHeightMap(1, 1, perlin, geometry);
		HeightMap calmMap = new PerlinHeightMap(1, 1, calmPerlin, geometry);

		HeightMap elevationMap = new CalmAndWildPerlinHeightMap(mixtureMap, calmMap, wildMap);

		RegionBuilder builder = new RegionBuilder();
		WorldParameters wParams = new WorldParameters();
		wParams.elevationMap = elevationMap;
		wParams.rnd = r;
		wParams.seaLevel = SEALEVEL;
		wParams.geometry = WorldGeometry.plane();
		RegionParameters rParams = new RegionParameters();
		rParams.numberOfPoints = POINTS;
		List<Location> clippingPolygon = new ArrayList<Location>();
		clippingPolygon.add(new Location(0, 0));
		clippingPolygon.add(new Location(0.2, 0));
		clippingPolygon.add(new Location(0.6, 0.3));
		clippingPolygon.add(new Location(0.7, 0.8));
		clippingPolygon.add(new Location(0, 1));
//				rParams.clippingPolygon = clippingPolygon;
		rParams.xMin = -0.25;
		rParams.yMin = -0.25;
		rParams.xMax = 1.25;
		rParams.yMax = 1.25;
		TechnicalParameters tParams = new TechnicalParameters();
		tParams.relaxations = 2;
		Region region = builder.buildRegion(wParams, rParams, tParams);

		Graphs buildResult = region.graphs;

		System.out.println("drawing...");

		List<DrawLayer> drawLayers = new ArrayList<>();
		//		drawLayers.add(new BoundaryCellDrawLayer());
		//								drawLayers.add(new GraphDrawLayer());
		//				drawLayers.add(new DualGraphDrawLayer());
		drawLayers.add(new SatelliteDrawLayer(r, buildResult, (int) Math.sqrt(POINTS), 20));

		BufferedImage img = new BufferedImage((int) screenWidth, (int) screenHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = img.createGraphics();

		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setBackground(Color.white);
		g.setColor(Color.white);

		drawLayers.forEach((layer) -> {
			layer.draw(img);
		});

		display(img, g, null, buildResult);

		BufferedImage img2 = new BufferedImage((int) screenWidth, (int) screenHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2 = img2.createGraphics();
		selectionTexture = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_3BYTE_BGR);
		mapLayer = new FantasyLargeScaleDrawLayer(r, (int) Math.sqrt(POINTS), buildResult, 20, MapType.DEFAULT,
				selectionTexture, rParams.clippingPolygon, wParams.geometry);
						mapLayer.draw(img2);
//		new GraphDrawLayer(buildResult, rParams.getBounds()).draw(img2);
		//										new DualGraphDrawLayer(buildResult).draw(img2);
		display(img2, g2, selectionTexture, buildResult);
	}

	private static void display(BufferedImage img, Graphics2D g, BufferedImage selectionTexture, Graphs graphs) {
		JFrame frame = new JFrame();
		@SuppressWarnings("serial")
		MapCanvas canvas = new MapCanvas(img, graphs);
		canvas.setMinimumSize(new Dimension(screenWidth, screenHeight));

		if (selectionTexture != null) {
			canvas.addMouseMotionListener(new MouseMotionListener() {

				@Override
				public void mouseMoved(MouseEvent e) {
					Point pt = e.getPoint();
					long rgb = 0xFFFFFF & selectionTexture.getRGB(pt.x, pt.y);
					canvas.setSelectionIndex((int) rgb);
					canvas.repaint();
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					// TODO Auto-generated method stub

				}
			});
		}

		canvas.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			private void save() {
				File outputfile = new File("saved.png");
				try {
					ImageIO.write(img, "png", outputfile);
					System.out.println("Saved to " + outputfile.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.isMetaDown() && e.getKeyCode() == KeyEvent.VK_S) {
					save();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(canvas, BorderLayout.CENTER);

		frame.setTitle("Generated World");
		frame.setVisible(true);
		Insets insets = frame.getInsets();
		frame.setSize(screenWidth + insets.left + insets.right, screenHeight + insets.bottom + insets.top);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		frame.repaint();
	}
}
