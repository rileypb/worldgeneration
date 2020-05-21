package brainfreeze.old.squareold;
import java.awt.Graphics2D;

public interface DrawLayer {

	void draw(Graphics2D graphics, Terrain terrain, double x0, double y0, double xWidth, double yHeight);

}
