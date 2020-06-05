package brainfreeze.world;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.lang.ref.WeakReference;
import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import sun.awt.image.IntegerComponentRaster;

public class DistanceGradientPaintContext implements PaintContext {

	private Geometry geometry;

	static ColorModel xrgbmodel = new DirectColorModel(24, 0x00ff0000, 0x0000ff00, 0x000000ff);
	static ColorModel xbgrmodel = new DirectColorModel(24, 0x000000ff, 0x0000ff00, 0x00ff0000);

	static ColorModel cachedModel;
	static WeakReference<Raster> cached;

	static synchronized Raster getCachedRaster(ColorModel cm, int w, int h) {
		if (cm == cachedModel) {
			if (cached != null) {
				Raster ras = (Raster) cached.get();
				if (ras != null && ras.getWidth() >= w && ras.getHeight() >= h) {
					cached = null;
					return ras;
				}
			}
		}
		return cm.createCompatibleWritableRaster(w, h);
	}

	static synchronized void putCachedRaster(ColorModel cm, Raster ras) {
		if (cached != null) {
			Raster cras = (Raster) cached.get();
			if (cras != null) {
				int cw = cras.getWidth();
				int ch = cras.getHeight();
				int iw = ras.getWidth();
				int ih = ras.getHeight();
				if (cw >= iw && ch >= ih) {
					return;
				}
				if (cw * ch >= iw * ih) {
					return;
				}
			}
		}
		cachedModel = cm;
		cached = new WeakReference<>(ras);
	}

	double x1;
	double y1;
	double dx;
	double dy;
	boolean cyclic;
	int interp[];
	Raster saved;
	ColorModel model;

	private double unitScale;

	public DistanceGradientPaintContext(Geometry geometry, ColorModel cm, AffineTransform xform, boolean cyclic,
			double unitScale) {
		this.geometry = geometry;
		this.unitScale = unitScale;

		// First calculate the distance moved in user space when
		// we move a single unit along the X & Y axes in device space.
		Point2D xvec = new Point2D.Double(1, 0);
		Point2D yvec = new Point2D.Double(0, 1);
		try {
			AffineTransform inverse = xform.createInverse();
			inverse.deltaTransform(xvec, xvec);
			inverse.deltaTransform(yvec, yvec);
		} catch (NoninvertibleTransformException e) {
			xvec.setLocation(0, 0);
			yvec.setLocation(0, 0);
		}

		this.cyclic = cyclic;
		model = ColorModel.getRGBdefault();
		interp = new int[cyclic ? 513 : 257];
		for (int i = 0; i <= 256; i++) {
			float rel = i / 256.0f;
		}
	}

	/**
	 * Release the resources allocated for the operation.
	 */
	public void dispose() {
		if (saved != null) {
			putCachedRaster(model, saved);
			saved = null;
		}
	}

	/**
	 * Return the ColorModel of the output.
	 */
	public ColorModel getColorModel() {
		return model;
	}

	/**
	 * Return a Raster containing the colors generated for the graphics
	 * operation.
	 * @param x,y,w,h The area in device space for which colors are
	 * generated.
	 */
	public Raster getRaster(int x, int y, int w, int h) {
		double rowrel = (x - x1) * dx + (y - y1) * dy;

		Raster rast = saved;
		if (rast == null || rast.getWidth() < w || rast.getHeight() < h) {
			rast = getCachedRaster(model, w, h);
			saved = rast;
		}
		IntegerComponentRaster irast = (IntegerComponentRaster) rast;
		int off = irast.getDataOffset(0);
		int adjust = irast.getScanlineStride() - w;
		int[] pixels = irast.getDataStorage();

		Arrays.fill(pixels, 0xFFFFFFFF);

		//	        if (cyclic) {
		//	            cycleFillRaster(pixels, off, adjust, w, h, rowrel, dx, dy);
		//	        } else {
		//	            clipFillRaster(pixels, off, adjust, w, h, rowrel, dx, dy);
		//	        }

		GeometryFactory geomFact = new GeometryFactory();
		Coordinate coord = new Coordinate();
		Point p = geomFact.createPoint(coord);
		for (int u = 0; u < w; u++) {
			for (int v = 0; v < h; v++) {
				x1 = x + u;
				y1 = y + v;

				coord.x = x1/unitScale;
				coord.y = y1/unitScale;

				double distance = p.distance(geometry);

				int rgbval = (int) Math.min(255, distance * 255);

				int argb = (255 << 24) + (rgbval << 16) + (rgbval << 8) + rgbval;

				pixels[off++] = argb;
			}
		}

		irast.markDirty();

		return rast;
	}

	void cycleFillRaster(int[] pixels, int off, int adjust, int w, int h, double rowrel, double dx, double dy) {
		rowrel = rowrel % 2.0;
		int irowrel = ((int) (rowrel * (1 << 30))) << 1;
		int idx = (int) (-dx * (1 << 31));
		int idy = (int) (-dy * (1 << 31));
		while (--h >= 0) {
			int icolrel = irowrel;
			for (int j = w; j > 0; j--) {
				pixels[off++] = interp[icolrel >>> 23];
				icolrel += idx;
			}

			off += adjust;
			irowrel += idy;
		}
	}

	void clipFillRaster(int[] pixels, int off, int adjust, int w, int h, double rowrel, double dx, double dy) {
		while (--h >= 0) {
			double colrel = rowrel;
			int j = w;
			if (colrel <= 0.0) {
				int rgb = interp[0];
				do {
					pixels[off++] = rgb;
					colrel += dx;
				} while (--j > 0 && colrel <= 0.0);
			}
			while (colrel < 1.0 && --j >= 0) {
				pixels[off++] = interp[(int) (colrel * 256)];
				colrel += dx;
			}
			if (j > 0) {
				int rgb = interp[256];
				do {
					pixels[off++] = rgb;
				} while (--j > 0);
			}

			off += adjust;
			rowrel += dy;
		}
	}
}
