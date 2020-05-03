package voronoinew;


import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public enum Biome {
	TROPICAL_RAIN_FOREST(0, 128, 0, "palm-tree.png"), //
	TEMPERATE_RAIN_FOREST(0, 192, 0, "palm-tree.png"), //
	TEMPERATE_DECIDUOUS_FOREST(0, 255, 0, "tree.png"), //
	TAIGA(128,128,230, null), //
	TUNDRA(197, 197, 230, null), //
	SUBTROPICAL_DESERT(255, 255, 0, "cactus.png"), //
	SNOW(255, 255, 255, null), //
	TEMPERATE_DESERT(255, 255, 190, null), //
	GRASSLAND(128, 197, 64, "grass.png"), //
	SAVANNA(128,128,0, null);

	public final Color color;
	private String imagePath;
	public final BufferedImage image;

	private Biome(int r, int g, int b, String imagePath) {
		this.imagePath = imagePath;
		BufferedImage i;
		try {
			i = ImageIO.read(getClass().getResource("/images/landscape/" + imagePath));
		} catch (Exception e) {
			i = null;
		}
		image = i;
		this.color = new Color(r, g, b);
	}

}
