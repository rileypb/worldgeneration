package voronoi;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public interface DrawLayer {

	void draw(Graphics2D graphics, Graphs buildResult, BufferedImage im);

}
