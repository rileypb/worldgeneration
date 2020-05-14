package voronoi;

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.beans.ConstructorProperties;

public class TriangleGradientPaint implements Paint {

    Point2D.Float p1;
    Point2D.Float p2;
    Color color1;
    Color color2;
    boolean cyclic;
	private Float p3;
	private Color color3;

    /**
     * Constructs a simple acyclic <code>GradientPaint</code> object.
     * @param x1 x coordinate of the first specified
     * <code>Point</code> in user space
     * @param y1 y coordinate of the first specified
     * <code>Point</code> in user space
     * @param color1 <code>Color</code> at the first specified
     * <code>Point</code>
     * @param x2 x coordinate of the second specified
     * <code>Point</code> in user space
     * @param y2 y coordinate of the second specified
     * <code>Point</code> in user space
     * @param color2 <code>Color</code> at the second specified
     * <code>Point</code>
     * @throws NullPointerException if either one of colors is null
     */
    public TriangleGradientPaint(float x1,
                         float y1,
                         Color color1,
                         float x2,
                         float y2,
                         Color color2,
                         float x3,
                         float y3,
                         Color color3) {
        if ((color1 == null) || (color2 == null) || (color3 == null)) {
            throw new NullPointerException("Colors cannot be null");
        }

        p1 = new Point2D.Float(x1, y1);
        p2 = new Point2D.Float(x2, y2);
        p3 = new Point2D.Float(x3, y3);
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
    }

    /**
     * Constructs a simple acyclic <code>GradientPaint</code> object.
     * @param pt1 the first specified <code>Point</code> in user space
     * @param color1 <code>Color</code> at the first specified
     * <code>Point</code>
     * @param pt2 the second specified <code>Point</code> in user space
     * @param color2 <code>Color</code> at the second specified
     * <code>Point</code>
     * @throws NullPointerException if either one of colors or points
     * is null
     */
    public TriangleGradientPaint(Point2D pt1,
                         Color color1,
                         Point2D pt2,
                         Color color2,
                         Point2D pt3,
                         Color color3) {
        if ((color1 == null) || (color2 == null) ||
            (pt1 == null) || (pt2 == null)) {
            throw new NullPointerException("Colors and points should be non-null");
        }

        p1 = new Point2D.Float((float)pt1.getX(), (float)pt1.getY());
        p2 = new Point2D.Float((float)pt2.getX(), (float)pt2.getY());
        p3 = new Point2D.Float((float)pt3.getX(), (float)pt3.getY());
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
    }

    /**
     * Constructs either a cyclic or acyclic <code>GradientPaint</code>
     * object depending on the <code>boolean</code> parameter.
     * @param x1 x coordinate of the first specified
     * <code>Point</code> in user space
     * @param y1 y coordinate of the first specified
     * <code>Point</code> in user space
     * @param color1 <code>Color</code> at the first specified
     * <code>Point</code>
     * @param x2 x coordinate of the second specified
     * <code>Point</code> in user space
     * @param y2 y coordinate of the second specified
     * <code>Point</code> in user space
     * @param color2 <code>Color</code> at the second specified
     * <code>Point</code>
     * @param cyclic <code>true</code> if the gradient pattern should cycle
     * repeatedly between the two colors; <code>false</code> otherwise
     */
    public TriangleGradientPaint(float x1,
                         float y1,
                         Color color1,
                         float x2,
                         float y2,
                         Color color2,
                         float x3, 
                         float y3,
                         Color color3,
                         boolean cyclic) {
        this (x1, y1, color1, x2, y2, color2, x3, y3, color3);
        this.cyclic = cyclic;
    }

    /**
     * Constructs either a cyclic or acyclic <code>GradientPaint</code>
     * object depending on the <code>boolean</code> parameter.
     * @param pt1 the first specified <code>Point</code>
     * in user space
     * @param color1 <code>Color</code> at the first specified
     * <code>Point</code>
     * @param pt2 the second specified <code>Point</code>
     * in user space
     * @param color2 <code>Color</code> at the second specified
     * <code>Point</code>
     * @param cyclic <code>true</code> if the gradient pattern should cycle
     * repeatedly between the two colors; <code>false</code> otherwise
     * @throws NullPointerException if either one of colors or points
     * is null
     */
    @ConstructorProperties({ "point1", "color1", "point2", "color2", "cyclic" })
    public TriangleGradientPaint(Point2D pt1,
                         Color color1,
                         Point2D pt2,
                         Color color2,
                         Point2D pt3,
                         Color color3,
                         boolean cyclic) {
        this (pt1, color1, pt2, color2, pt3, color3);
        this.cyclic = cyclic;
    }

    /**
     * Returns a copy of the point P1 that anchors the first color.
     * @return a {@link Point2D} object that is a copy of the point
     * that anchors the first color of this
     * <code>GradientPaint</code>.
     */
    public Point2D getPoint1() {
        return new Point2D.Float(p1.x, p1.y);
    }

    /**
     * Returns the color C1 anchored by the point P1.
     * @return a <code>Color</code> object that is the color
     * anchored by P1.
     */
    public Color getColor1() {
        return color1;
    }

    /**
     * Returns a copy of the point P2 which anchors the second color.
     * @return a {@link Point2D} object that is a copy of the point
     * that anchors the second color of this
     * <code>GradientPaint</code>.
     */
    public Point2D getPoint2() {
        return new Point2D.Float(p2.x, p2.y);
    }

    /**
     * Returns the color C2 anchored by the point P2.
     * @return a <code>Color</code> object that is the color
     * anchored by P2.
     */
    public Color getColor2() {
        return color2;
    }

    /**
     * Returns <code>true</code> if the gradient cycles repeatedly
     * between the two colors C1 and C2.
     * @return <code>true</code> if the gradient cycles repeatedly
     * between the two colors; <code>false</code> otherwise.
     */
    public boolean isCyclic() {
        return cyclic;
    }

    /**
     * Creates and returns a {@link PaintContext} used to
     * generate a linear color gradient pattern.
     * See the {@link Paint#createContext specification} of the
     * method in the {@link Paint} interface for information
     * on null parameter handling.
     *
     * @param cm the preferred {@link ColorModel} which represents the most convenient
     *           format for the caller to receive the pixel data, or {@code null}
     *           if there is no preference.
     * @param deviceBounds the device space bounding box
     *                     of the graphics primitive being rendered.
     * @param userBounds the user space bounding box
     *                   of the graphics primitive being rendered.
     * @param xform the {@link AffineTransform} from user
     *              space into device space.
     * @param hints the set of hints that the context object can use to
     *              choose between rendering alternatives.
     * @return the {@code PaintContext} for
     *         generating color patterns.
     * @see Paint
     * @see PaintContext
     * @see ColorModel
     * @see Rectangle
     * @see Rectangle2D
     * @see AffineTransform
     * @see RenderingHints
     */
    public PaintContext createContext(ColorModel cm,
                                      Rectangle deviceBounds,
                                      Rectangle2D userBounds,
                                      AffineTransform xform,
                                      RenderingHints hints) {

        return new TriangleGradientPaintContext(cm, p1, p2, p3, xform,
                                        color1, color2, color3, cyclic);
    }

    /**
     * Returns the transparency mode for this <code>GradientPaint</code>.
     * @return an integer value representing this <code>GradientPaint</code>
     * object's transparency mode.
     * @see Transparency
     */
    public int getTransparency() {
        int a1 = color1.getAlpha();
        int a2 = color2.getAlpha();
        return (((a1 & a2) == 0xff) ? OPAQUE : TRANSLUCENT);
    }

}
