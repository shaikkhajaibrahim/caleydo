/**
 * 
 */
package setvis.shape;

import static setvis.VecUtil.addVec;
import static setvis.VecUtil.middleVec;
import static setvis.VecUtil.mulVec;
import static setvis.VecUtil.normVec;
import static setvis.VecUtil.subVec;
import static setvis.VecUtil.vecLengthSqr;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import setvis.SetOutline;

/**
 * Generates a bezier interpolated {@link Shape} for the vertices generated by
 * {@link SetOutline#createOutline(Rectangle2D[], Rectangle2D[])}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * 
 */
public class BezierShapeGenerator extends RoundShapeGenerator {

    /**
     * Indicates that there should be a maximal radius for the curves.
     */
    private final boolean hasMaxRadius;

    /**
     * Creates an {@link BezierShapeGenerator} with a given set outline creator.
     * The ordering is assumed to be clock-wise and the generator will use a
     * maximal radius for the curves.
     * 
     * @param outline
     *            The creator of the set outlines.
     */
    public BezierShapeGenerator(final SetOutline outline) {
        this(outline, true, true);
    }

    /**
     * Creates an {@link BezierShapeGenerator} with a given set outline creator.
     * 
     * @param outline
     *            The creator of the set outlines.
     * @param clockwise
     *            Whether the result of the set outlines are interpreted in
     *            clockwise order.
     * @param hasMaxRadius
     *            Whether there should be a maximal radius for the curves.
     */
    public BezierShapeGenerator(final SetOutline outline,
            final boolean clockwise, final boolean hasMaxRadius) {
        super(outline, clockwise);
        this.hasMaxRadius = hasMaxRadius;
    }

    @Override
    public Shape convertToShape(final Point2D[] points, final boolean closed) {
        final GeneralPath res = new GeneralPath();
        final int len = points.length;
        boolean first = true;
        for (int i = 0; i < len; ++i) {
            if (!closed && !hasMaxRadius && i >= len - 1) {
                continue;
            }
            final Point2D a = points[i]; // point
            final Point2D b = points[getOtherIndex(i, len, false)]; // left
            final Point2D c = points[getOtherIndex(i, len, true)]; // right
            final Point2D[] vertices = hasMaxRadius ? getRestrictedBezier(a, b,
                    c) : getBezierForPoint(a, b, c);
            final Point2D p = vertices[0];
            if (first) {
                res.moveTo(p.getX(), p.getY());
                first = false;
            } else if (hasMaxRadius) {
                res.lineTo(p.getX(), p.getY());
            }
            final Point2D s1 = vertices[2];
            if ((a.getX() == b.getX() && b.getX() == c.getX())
                    || (a.getY() == b.getY() && b.getY() == c.getY())) {
                res.lineTo(s1.getX(), s1.getY());
                continue;
            }
            final Point2D s0 = vertices[1];
            res.curveTo(s0.getX(), s0.getY(), s0.getX(), s0.getY(), s1.getX(),
                    s1.getY());
        }
        if (!first && hasMaxRadius && closed) {
            res.closePath();
        }
        return res;
    }

    /**
     * Creates bezier points by using the middle points of two points of the
     * outline as reference points.
     * 
     * @param point
     *            The current point.
     * @param left
     *            The point left of the current point.
     * @param right
     *            The point right of the current point.
     * @return The bezier reference points.
     */
    private static Point2D[] getBezierForPoint(final Point2D point,
            final Point2D left, final Point2D right) {
        return new Point2D[] { middleVec(point, left), point,
                middleVec(point, right) };
    }

    /**
     * Cached radius helper for
     * {@link #getRestrictedBezier(Point2D, Point2D, Point2D)}.
     */
    private double rad;

    /**
     * Cached squared radius helper for
     * {@link #getRestrictedBezier(Point2D, Point2D, Point2D)}.
     */
    private double qrad;

    @Override
    public void setRadius(final double radius) {
        super.setRadius(radius);
        // even with four times the radius the curve won't cut the original
        // vertices
        rad = radius * 4.0;
        qrad = rad * rad;
    }

    public boolean hasMaxRadius() {
        return hasMaxRadius;
    }

    /**
     * Creates bezier points by going a certain radius away from the current
     * point in the directions of the neighbors.
     * 
     * @param point
     *            The current point.
     * @param left
     *            The point left of the current point.
     * @param right
     *            The point right of the current point.
     * @return The bezier reference points.
     */
    private Point2D[] getRestrictedBezier(final Point2D point,
            final Point2D left, final Point2D right) {
        Point2D lp = middleVec(point, left);
        final Point2D dlp = subVec(lp, point);
        if (vecLengthSqr(dlp) > qrad) {
            lp = addVec(mulVec(normVec(dlp), rad), point);
        }
        Point2D rp = middleVec(point, right);
        final Point2D drp = subVec(rp, point);
        if (vecLengthSqr(drp) > qrad) {
            rp = addVec(mulVec(normVec(drp), rad), point);
        }
        return new Point2D[] { lp, point, rp };
    }

}
