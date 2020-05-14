package voronoi;

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

import sun.awt.image.IntegerComponentRaster;

public class TriangleGradientPaintContext implements PaintContext {
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
	private Point2D p1;
	private Point2D p2;
	private Point2D p3;
	private double denominator;
	private int r1;
	private int g1;
	private int b1;
	private int r2;
	private int g2;
	private int b2;
	private int r3;
	private int g3;
	private int b3;
	private int a1;
	private int a2;
	private int a3;

	public TriangleGradientPaintContext(ColorModel cm, Point2D p1, Point2D p2, Point2D p3, AffineTransform xform,
			Color c1, Color c2, Color c3, boolean cyclic) {
		// our code
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;

		a1 = c1.getAlpha();
		r1 = c1.getRed();
		g1 = c1.getGreen();
		b1 = c1.getBlue();
		a2 = c2.getAlpha();
		r2 = c2.getRed();
		g2 = c2.getGreen();
		b2 = c2.getBlue();
		a3 = c3.getAlpha();
		r3 = c3.getRed();
		g3 = c3.getGreen();
		b3 = c3.getBlue();

		// end our code

		denominator = 1 / ((p2.getY() - p3.getY()) * (p1.getX() - p3.getX())
				+ (p3.getX() - p2.getX()) * (p1.getY() - p3.getY()));

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

		// Now calculate the (square of the) user space distance
		// between the anchor points. This value equals:
		//     (UserVec . UserVec)
		double udx = p2.getX() - p1.getX();
		double udy = p2.getY() - p1.getY();
		double ulenSq = udx * udx + udy * udy;

		if (ulenSq <= Double.MIN_VALUE) {
			dx = 0;
			dy = 0;
		} else {
			// Now calculate the proportional distance moved along the
			// vector from p1 to p2 when we move a unit along X & Y in
			// device space.
			//
			// The length of the projection of the Device Axis Vector is
			// its dot product with the Unit User Vector:
			//     (DevAxisVec . (UserVec / Len(UserVec))
			//
			// The "proportional" length is that length divided again
			// by the length of the User Vector:
			//     (DevAxisVec . (UserVec / Len(UserVec))) / Len(UserVec)
			// which simplifies to:
			//     ((DevAxisVec . UserVec) / Len(UserVec)) / Len(UserVec)
			// which simplifies to:
			//     (DevAxisVec . UserVec) / LenSquared(UserVec)
			dx = (xvec.getX() * udx + xvec.getY() * udy) / ulenSq;
			dy = (yvec.getX() * udx + yvec.getY() * udy) / ulenSq;

			if (cyclic) {
				dx = dx % 1.0;
				dy = dy % 1.0;
			} else {
				// We are acyclic
				if (dx < 0) {
					// If we are using the acyclic form below, we need
					// dx to be non-negative for simplicity of scanning
					// across the scan lines for the transition points.
					// To ensure that constraint, we negate the dx/dy
					// values and swap the points and colors.
					Point2D p = p1;
					p1 = p2;
					p2 = p;
					Color c = c1;
					c1 = c2;
					c2 = c;
					dx = -dx;
					dy = -dy;
				}
			}
		}

		Point2D dp1 = xform.transform(p1, null);
		this.x1 = dp1.getX();
		this.y1 = dp1.getY();

		this.cyclic = cyclic;
		int rgb1 = c1.getRGB();
		int rgb2 = c2.getRGB();
		int a1 = (rgb1 >> 24) & 0xff;
		int r1 = (rgb1 >> 16) & 0xff;
		int g1 = (rgb1 >> 8) & 0xff;
		int b1 = (rgb1) & 0xff;
		int da = ((rgb2 >> 24) & 0xff) - a1;
		int dr = ((rgb2 >> 16) & 0xff) - r1;
		int dg = ((rgb2 >> 8) & 0xff) - g1;
		int db = ((rgb2) & 0xff) - b1;
		if (a1 == 0xff && da == 0) {
			model = xrgbmodel;
			if (cm instanceof DirectColorModel) {
				DirectColorModel dcm = (DirectColorModel) cm;
				int tmp = dcm.getAlphaMask();
				if ((tmp == 0 || tmp == 0xff) && dcm.getRedMask() == 0xff && dcm.getGreenMask() == 0xff00
						&& dcm.getBlueMask() == 0xff0000) {
					model = xbgrmodel;
					tmp = r1;
					r1 = b1;
					b1 = tmp;
					tmp = dr;
					dr = db;
					db = tmp;
				}
			}
		} else {
			model = ColorModel.getRGBdefault();
		}
		interp = new int[cyclic ? 513 : 257];
		for (int i = 0; i <= 256; i++) {
			float rel = i / 256.0f;
			int rgb = (((int) (a1 + da * rel)) << 24) | (((int) (r1 + dr * rel)) << 16) | (((int) (g1 + dg * rel)) << 8)
					| (((int) (b1 + db * rel)));
			interp[i] = rgb;
			if (cyclic) {
				interp[512 - i] = rgb;
			}
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

		for (int u = 0; u < w; u++) {
			for (int v = 0; v < h; v++) {
				double w1 = ((p2.getY() - p3.getY()) * (x + u - p3.getX())
						+ (p3.getX() - p2.getX()) * (y + v - p3.getY())) * denominator;
				double w2 = ((p3.getY() - p1.getY()) * (x + u - p3.getX())
						+ (p1.getX() - p3.getX()) * (y + v - p3.getY())) * denominator;
				
				if (w1 < 0) {
					w1 = 0;
				}
				if (w1 > 1) {
					w1 = 1;
				}
				if (w2 < 0) {
					w2 = 0;
				}
				if (w2 > 1) {
					w2 = 1;
				}
				
				if (w1 + w2 > 1) {
					w1 = w1/(w1+w2);
					w2 = w2/(w1+w2);
				}
				
				
				double w3 = 1 - w1 - w2;

				int a = (int) (w1 * a1 + w2 * a2 + w3 * a3);
				int r = (int) (w1 * r1 + w2 * r2 + w3 * r3);
				int g = (int) (w1 * g1 + w2 * g2 + w3 * g3);
				int b = (int) (w1 * b1 + w2 * b2 + w3 * b3);

				int argb = b + (g << 8) + (r << 16) + (a << 24);
				
				
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
