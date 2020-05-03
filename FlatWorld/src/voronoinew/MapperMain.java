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
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.flowpowered.noise.NoiseQuality;
import com.flowpowered.noise.module.source.Perlin;

public class MapperMain {

	public static final double SEALEVEL = 0;
	public static final double MOUNTAIN_THRESHOLD = 0.9;
	private static int screenWidth;
	private static int screenHeight;

	public static void main(String[] args) {
		screenWidth = 800;
		screenHeight = 800;

		TerrainBuilder builder = new TerrainBuilder(4000, TerrainBuilder.CellType.VORONOI);
		int seed = new Random().nextInt();
		System.out.println("seed: " + seed);
		Random r = new Random(seed);
		Graphs buildResult = builder.run(r);


		Perlin perlin = new Perlin();
		perlin.setFrequency(.25);
		perlin.setOctaveCount(2);
		perlin.setSeed(r.nextInt());
		Perlin calmPerlin = new Perlin();
		calmPerlin.setFrequency(.125);
		calmPerlin.setOctaveCount(4);
		calmPerlin.setSeed(r.nextInt());
		Perlin wildPerlin = new Perlin();
		wildPerlin.setFrequency(1);
		wildPerlin.setOctaveCount(8);
		wildPerlin.setSeed(r.nextInt());

//		builder.forEachLocation(buildResult, (x) -> {
//			System.out.println(x.x);
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
		});
		builder.normalizeElevations(buildResult);

		builder.fillDepressions(buildResult);
		
		builder.runRivers(buildResult);
		
		System.out.println("drawing...");

		List<DrawLayer> drawLayers = new ArrayList<>();
		drawLayers.add(new SeaLandDrawLayer());
//		drawLayers.add(new BoundaryCellDrawLayer());
//		drawLayers.add(new GraphDrawLayer());
//		drawLayers.add(new DualGraphDrawLayer());

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

	private static void display(BufferedImage img, TerrainBuilder builder, Graphics2D g) {
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
