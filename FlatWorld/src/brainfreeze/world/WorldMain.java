package brainfreeze.world;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.flowpowered.noise.module.source.Perlin;

import brainfreeze.framework.HeightMap;
import brainfreeze.framework.PerlinHeightMap;
import brainfreeze.framework.RegionParameters;
import brainfreeze.framework.TechnicalParameters;
import brainfreeze.framework.WorldGeometry;
import brainfreeze.framework.WorldParameters;
import brainfreeze.world.FantasyLargeScaleDrawLayer.MapType;

public class WorldMain {

	private static final double SEALEVEL = 0.1;
	private static int worldSize;
	private static int screenWidth;
	private static int screenHeight;
	private static int POINTS = 500;

	public static void main(String[] args) {
		worldSize = 12000;

		screenWidth = 1200;
		screenHeight = 600;

		int seed = new Random().nextInt();
//		seed = 1323469862;
		System.out.println("seed: " + seed);

		Random r = new Random(seed);

		WorldBuilder builder = new WorldBuilder();

		Perlin perlin = new Perlin();
		perlin.setSeed(r.nextInt());
		WorldGeometry worldGeometry = WorldGeometry.cylinder(-worldSize, 3 * worldSize, 0, worldSize);
		HeightMap elevationMap = new PerlinHeightMap(2 * worldSize, worldSize, perlin,
				worldGeometry);

		WorldParameters wParams = new WorldParameters();
		wParams.elevationMap = elevationMap;
		wParams.rnd = r;
		wParams.seaLevel = SEALEVEL;
		wParams.width = 2 * worldSize;
		wParams.height = worldSize;
		wParams.numberOfPlates = 16;
		wParams.geometry = worldGeometry;

		RegionParameters rParams = new RegionParameters();
		rParams.numberOfPoints = POINTS;
		rParams.xMin = 0;
		rParams.xMax = wParams.width;
		rParams.yMin = 0;
		rParams.yMax = wParams.height;

		TechnicalParameters tParams = new TechnicalParameters();
		tParams.relaxations = 2;

		World world = builder.buildWorld(wParams, rParams, tParams);

		Graphs buildResult = world.graphs;

		BufferedImage img2 = new BufferedImage((int) screenWidth, (int) screenHeight, BufferedImage.TYPE_4BYTE_ABGR);
//		new GraphDrawLayer(buildResult, rParams.getBounds(), wParams.width, wParams.height, true).draw(img2);
		Graphics2D g2 = img2.createGraphics();
		BufferedImage selectionTexture = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_3BYTE_BGR);
		FantasyLargeScaleDrawLayer mapLayer = new FantasyLargeScaleDrawLayer(r, (int) Math.sqrt(POINTS), buildResult, 20, MapType.DEFAULT,
				selectionTexture, rParams.clippingPolygon, wParams.geometry);
						mapLayer.draw(img2);
		
		display(img2);
	}

	private static void display(BufferedImage img) {
		JFrame frame = new JFrame();
		@SuppressWarnings("serial")
		Canvas canvas = new Canvas() {
			public void paint(java.awt.Graphics g) {
				this.getGraphics().drawImage(img, 0, 0, null);
			}
		};
		canvas.setMinimumSize(new Dimension(screenWidth, screenHeight));

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(canvas, BorderLayout.CENTER);

		frame.setTitle("Generated World");
		frame.setVisible(true);
		Insets insets = frame.getInsets();
		frame.setSize(screenWidth + insets.left + insets.right, screenHeight + insets.bottom + insets.top);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		frame.repaint();
		canvas.repaint();
	}

}
