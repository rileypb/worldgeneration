package voronoinew;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.flowpowered.noise.module.source.Perlin;

public class MapperMain {

	public static final double SEALEVEL = 0.1;
	public static final double MOUNTAIN_THRESHOLD = 0.9;
	private static int screenWidth;
	private static int screenHeight;

	public static void main(String[] args) {
		screenWidth = 800;
		screenHeight = 800;

		TerrainBuilder2 builder = new TerrainBuilder2(5000, TerrainBuilder2.CellType.VORONOI);
		int seed = new Random().nextInt();
//								seed = 1425845856;
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

		//		builder.forEachLocation(buildResult, (x) -> {
		//			System.out.println(x.x);
		//		});

		//		builder.generateValues(buildResult, r, perlin, (target, value) -> {
		//			target.elevation = value;
		//		});

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
			//					System.out.println(target.elevation);
		});
		
		builder.smoothElevations(buildResult);
		builder.smoothElevations(buildResult);
		builder.smoothElevations(buildResult);

		builder.setVoronoiCornerElevations(buildResult);
		
		builder.setDualCornerElevations(buildResult);
		
		builder.normalizeElevations(buildResult);
		
		builder.markWater(buildResult, SEALEVEL);
		
		builder.raiseMountains(buildResult);
		
		builder.fillDepressions(buildResult);

		builder.setVoronoiCornerElevations(buildResult);
		
		builder.normalizeElevations(buildResult);

		builder.runRivers(buildResult);
		

		System.out.println("drawing...");

		List<DrawLayer> drawLayers = new ArrayList<>();
		drawLayers.add(new SeaLandDrawLayer3());
		//		drawLayers.add(new BoundaryCellDrawLayer());
		//						drawLayers.add(new GraphDrawLayer());
		//				drawLayers.add(new DualGraphDrawLayer());

		BufferedImage img = new BufferedImage((int) screenWidth, (int) screenHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = img.createGraphics();
		//		g.setColor(Color.black);
		//		g.fillRect(0, 0, screenWidth, screenHeight);
		//		g.setColor(Color.gray);
		//		g.fillRect(20, 20, screenWidth - 40, screenHeight - 40);
		//		g.setClip(25, 25, screenWidth - 50, screenHeight - 50);

		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setBackground(Color.white);
		g.setColor(Color.white);

		drawLayers.forEach((layer) -> {
			layer.draw((Graphics2D) img.getGraphics(), buildResult);
		});

		display(img, builder, g);
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

		//		Timer timer = new Timer("foo");
		//		timer.schedule(new TimerTask() {
		//			@Override
		//			public void run() {
		//				builder.drawImage(g, true);
		//				canvas.repaint();
		//			}
		//		}, 0, 200);

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(canvas, BorderLayout.CENTER);

		frame.setTitle("Contours");
		frame.setVisible(true);
		Insets insets = frame.getInsets();
		frame.setSize(screenWidth + insets.left + insets.right, screenHeight + insets.bottom + insets.top);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		frame.repaint();
	}
}
