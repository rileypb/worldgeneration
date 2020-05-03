package square;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import javafx.scene.input.MouseButton;

public class MapperMain {

	public static final double SEALEVEL = 0.3;
	public static final double MOUNTAIN_THRESHOLD = 0.9;
	private static int screenWidth;
	private static int screenHeight;
	private static Canvas canvas;
	private static BufferedImage img;
	private static JFrame frame;
	private static Terrain terrain;

	private static List<TerrainWindow> windowStack = new ArrayList<>();

	public static void main(String[] args) {
		screenWidth = 1200;
		screenHeight = 600;

		int seed = new Random().nextInt();
		System.out.println("seed: " + seed);
		Random r = new Random(seed);

		TerrainBuilder builder = new TerrainBuilder(1200, 600, r, SEALEVEL);
		windowStack.add(0, new TerrainWindow(0, 0, 1200, 600, screenWidth));
		buildAndDisplay(builder, windowStack.get(0));
		display(builder);
	}

	private static BufferedImage buildAndDisplay(TerrainBuilder builder, TerrainWindow window) {
		terrain = builder.run(window);

		System.out.println("drawing...");

		List<DrawLayer> drawLayers = new ArrayList<>();
		//		drawLayers.add(new ElevationDrawLayer(SEALEVEL));
		drawLayers.add(new OutlineDrawLayer(SEALEVEL));
//		drawLayers.add(new BiomeDrawLayer(SEALEVEL));
//		drawLayers.add(new GreeneryDrawLayer(SEALEVEL));
//		drawLayers.add(new SnowDrawLayer(SEALEVEL));
//		drawLayers.add(new CoastalShadingDrawLayer(SEALEVEL));

		if (img == null) {
			img = new BufferedImage((int) screenWidth, (int) screenHeight, BufferedImage.TYPE_4BYTE_ABGR);
		}
		Graphics2D g = (Graphics2D) img.getGraphics();
		//		g.setColor(Color.black);
		//		g.fillRect(0, 0, screenWidth, screenHeight);
		//		g.setColor(Color.gray);
		//		g.fillRect(20, 20, screenWidth - 40, screenHeight - 40);
		//		g.setClip(25, 25, screenWidth - 50, screenHeight - 50);


		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setBackground(Color.white);
		g.setColor(Color.white);
		Rectangle clipBounds = g.getDeviceConfiguration().getBounds();
		g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
		
		double x0 = clipBounds.x - 0;
		double y0 = clipBounds.y - 0;
		double xWidth = clipBounds.width + 0;
		double yHeight = clipBounds.height + 0;
		
		drawLayers.forEach((layer) -> {
			layer.draw((Graphics2D) g, terrain, x0, y0, xWidth, yHeight);
		});
		return img;

	}

	private static void display(TerrainBuilder builder) {
		if (frame == null) {
			frame = new JFrame();
			canvas = new Canvas() {
				@Override
				public void paint(Graphics g) {
					((Graphics2D) g).scale(1, 1);
					((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
							RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g.drawImage(img, 0, 0, null);
				}
			};
			canvas.setMinimumSize(new Dimension(screenWidth, screenHeight));

			canvas.addMouseMotionListener(new MouseMotionListener() {

				@Override
				public void mouseMoved(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseDragged(MouseEvent e) {
					if (mousePressedEvent != null) {
						canvas.getGraphics().drawImage(img, 0,0,null);
						canvas.getGraphics().drawRect(mousePressedEvent.getX(), mousePressedEvent.getY(),
								e.getX() - mousePressedEvent.getX(), e.getY() - mousePressedEvent.getY());
					}
				}
			});

			canvas.addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {
					if (e.getButton() == MouseButton.SECONDARY.ordinal()) {
						if (windowStack.size() == 1) {
							return;
						}
						windowStack.remove(0);
						buildAndDisplay(builder, windowStack.get(0));
						canvas.repaint();
						frame.repaint();
					} else {
						MouseEvent mouseReleasedEvent = e;

						double windowWidth = Math.abs(mousePressedEvent.getX() - mouseReleasedEvent.getX());
						double windowHeight = Math.abs(mousePressedEvent.getY() - mouseReleasedEvent.getY());

						if (windowWidth < 2 * windowHeight) {
							windowWidth = 2 * windowHeight;
						} else {
							windowHeight = windowWidth / 2;
						}

						if (windowWidth == 0) {
							windowWidth = 1200;
						}
						if (windowHeight == 0) {
							windowHeight = 600;
						}

						int x0 = mousePressedEvent.getX() - canvas.getX();
						int y0 = mousePressedEvent.getY() - canvas.getY();

						TerrainWindow newWindow = windowStack.get(0).deriveNewWindow(x0, y0, windowWidth, windowHeight);

						windowStack.add(0, newWindow);
						buildAndDisplay(builder, newWindow);
						//						buildAndDisplay(builder, x0, y0, windowWidth, windowHeight);
						canvas.repaint();
						frame.repaint();
					}
					mousePressedEvent = null;
				}

				@Override
				public void mousePressed(MouseEvent e) {
					mousePressedEvent = e;
				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseClicked(MouseEvent e) {
					// TODO Auto-generated method stub

				}
			});

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

			frame.setTitle("FlatWorld");
			frame.setVisible(true);
			Insets insets = frame.getInsets();
			frame.setSize(screenWidth + insets.left + insets.right, screenHeight + insets.bottom + insets.top);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			frame.repaint();
		}
	}

	private static MouseEvent mousePressedEvent;
}
