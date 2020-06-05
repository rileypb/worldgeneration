package brainfreeze.world;

import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

import org.locationtech.jts.geom.Geometry;

public class DistanceGradientPaint implements Paint {

	private Geometry geometry;
	private double unitScale;

	public DistanceGradientPaint(Geometry geometry, double xWidth) {
		this.geometry = geometry;
		this.unitScale = xWidth;
	}
	
	@Override
	public int getTransparency() {
        return OPAQUE;
	}

	@Override
	public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds,
			AffineTransform xform, RenderingHints hints) {
		return new DistanceGradientPaintContext(geometry, cm, xform, false, unitScale);
	}

}
