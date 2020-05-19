package voronoi;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.flowpowered.noise.module.source.Perlin;

import javafx.scene.input.KeyCode;

public class MapperMain {

	public static final double SEALEVEL = 0.1;
	public static final double MOUNTAIN_THRESHOLD = 0.9;
	private static int screenWidth;
	private static int screenHeight;

	public static final int POINTS = 2500;

	public static void main(String[] args) {
		screenWidth = 800;
		screenHeight = 800;

		TerrainBuilder2 builder = new TerrainBuilder2(POINTS, TerrainBuilder2.CellType.VORONOI);
		int seed = new Random().nextInt();
//												seed = 13802760;
//		seed=56452391;
//		seed = -1433114302; // screwed up cell rendering
//		seed = -1029468384; // screwed up cell rendering
//		seed = -580132427;
		System.out.println("seed: " + seed);
		Random r = new Random(seed);
		Graphs buildResult = builder.run(r, 1);

		Perlin perlin = new Perlin();
		perlin.setFrequency(1);
		perlin.setOctaveCount(16);
		perlin.setSeed(r.nextInt());
		Perlin calmPerlin = new Perlin();
		calmPerlin.setFrequency(.25);
		calmPerlin.setOctaveCount(16);
		calmPerlin.setSeed(r.nextInt());
		Perlin wildPerlin = new Perlin();
		wildPerlin.setFrequency(4);
		wildPerlin.setOctaveCount(30);
		wildPerlin.setSeed(r.nextInt());

		Perlin moisturePerlin = new Perlin();
		moisturePerlin.setFrequency(.125);
		moisturePerlin.setOctaveCount(8);
		moisturePerlin.setSeed(r.nextInt());

		builder.generateValues(buildResult, r, perlin, (target, value) -> {
			target.wildness = value;
		});
		builder.generateValues(buildResult, r, calmPerlin, (target, value) -> {
			target.calmValue = value;
		});
		builder.generateValues(buildResult, r, wildPerlin, (target, value) -> {
			target.wildValue = value;
		});
		builder.forEachLocation(buildResult, (target) -> {
			target.elevation = (1 - target.wildness * target.wildness * target.wildness) * target.calmValue
					+ target.wildness * target.wildness * target.wildness * target.wildValue;
		});

		builder.smoothElevations(buildResult);
		builder.smoothElevations(buildResult);
		builder.smoothElevations(buildResult);

		builder.setVoronoiCornerElevations(buildResult);

		builder.setDualCornerElevations(buildResult);

		builder.normalizeElevations(buildResult);

		builder.setBaseMoisture(buildResult, r, moisturePerlin);
		//		builder.normalizeBaseMoisture(buildResult);

		builder.markWater(buildResult, SEALEVEL);

		builder.eliminateStrandedWaterAndFindLakes(buildResult, SEALEVEL);

		builder.raiseMountains(buildResult, POINTS);
		builder.fillInMountainGaps(buildResult);

		builder.fillDepressions(buildResult);

		builder.setVoronoiCornerElevations(buildResult);

		builder.normalizeElevations(buildResult);

		builder.runRivers(buildResult, 20);

		builder.calculateFinalMoisture(buildResult);
		builder.growForests(buildResult);

		CityScorer cityScorer = new CityScorer(POINTS);
		for (int i = 0; i < 5; i++) {
			cityScorer.scoreCitySites(buildResult);
		}

		builder.buildRoads(buildResult);

		TownPlanner townPlanner = new TownPlanner(POINTS);
		for (int i = 0; i < 7; i++) {
			townPlanner.placeTowns(buildResult);
		}

		builder.buildSecondaryRoads(buildResult);

		builder.relaxCoast(buildResult);
		builder.relaxEdges(buildResult, 20);

		List<List<Location>> pickList = new CellPicker(buildResult, 0.008).pick(r, 20);

		System.out.println("drawing...");

		List<DrawLayer> drawLayers = new ArrayList<>();
		//				drawLayers.add(new SeaLandDrawLayer3(r, (int) Math.sqrt(POINTS), pickList));
		//		drawLayers.add(new BoundaryCellDrawLayer());
		//								drawLayers.add(new GraphDrawLayer());
		//				drawLayers.add(new DualGraphDrawLayer());
		drawLayers.add(new GreeneryDrawLayer(r, (int) Math.sqrt(POINTS), 20));

		BufferedImage img = new BufferedImage((int) screenWidth, (int) screenHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = img.createGraphics();

		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setBackground(Color.white);
		g.setColor(Color.white);

		drawLayers.forEach((layer) -> {
			layer.draw((Graphics2D) img.getGraphics(), buildResult, img);
		});

		display(img, builder, g);

		BufferedImage img2 = new BufferedImage((int) screenWidth, (int) screenHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2 = img2.createGraphics();
		new SeaLandDrawLayer3(r, (int) Math.sqrt(POINTS), pickList, 20).draw(g2, buildResult, img2);
		//		new GraphDrawLayer().draw(g2, buildResult, img2);
		display(img2, builder, g2);
	}

	private static void display(BufferedImage img, TerrainBuilder2 builder, Graphics2D g) {
		JFrame frame = new JFrame();
		@SuppressWarnings("serial")
		Canvas canvas = new Canvas() {
			@Override
			public void paint(Graphics g) {
				((Graphics2D) g).scale(1, 1);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.drawImage(img, 0, 0, null);
			}
		};
		canvas.setMinimumSize(new Dimension(screenWidth, screenHeight));
		
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
