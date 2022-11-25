package org.ajwerner.voronoi.ui;

import com.barrybecker4.ui.renderers.OfflineGraphics;
import edu.princeton.cs.introcs.StdDraw;
import org.ajwerner.voronoi.model.Arc;
import org.ajwerner.voronoi.model.ArcKey;
import org.ajwerner.voronoi.model.BreakPoint;
import org.ajwerner.voronoi.model.CircleEvent;
import org.ajwerner.voronoi.model.Parabola;
import org.ajwerner.voronoi.model.Point;
import org.ajwerner.voronoi.model.VoronoiEdge;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class VoronoiRenderer {

    public static final double MIN_DRAW_DIM = -5;
    public static final double MAX_DRAW_DIM = 5;
    // Ghetto but just for drawing stuff
    public static final double MAX_DIM = 10;
    public static final double MIN_DIM = -10;

    private int width;
    private int height;

    private OfflineGraphics offlineGraphics;


    public VoronoiRenderer(int width, int height, boolean useOfflineGraphics) {
        this.width = width;
        this.height = height;
        if (useOfflineGraphics) {
            offlineGraphics = new OfflineGraphics(new Dimension(width, height), Color.WHITE);
        } else {
            StdDraw.setCanvasSize(width, height);
        }
    }

    public void show(List<Point> sites, List<VoronoiEdge> edgeList) {
        StdDraw.clear();
        for (Point p : sites) {
            fillCircle(p, 0.01, Color.RED);
        }
        for (VoronoiEdge e : edgeList) {
            if (e.p1 != null && e.p2 != null) {
                double topY = (e.p1.y == Double.POSITIVE_INFINITY) ? MAX_DIM : e.p1.y; // HACK to draw from infinity
                drawLine(e.p1.x, topY, e.p2.x, e.p2.y);
            }
        }
        show();
    }

    public void draw(List<Point> sites, List<VoronoiEdge> edgeList,
                     Set<BreakPoint> breakPoints, TreeMap<ArcKey, CircleEvent> arcs, double sweepLoc) {
        clear();
        for (Point p : sites) {
            fillCircle(p, 0.01, Color.RED);
        }
        for (BreakPoint bp : breakPoints) {
            drawBreakPoint(bp);
        }
        for (ArcKey a : arcs.keySet()) {
            drawArc((Arc) a);
        }
        for (VoronoiEdge e : edgeList) {
            if (e.p1 != null && e.p2 != null) {
                double topY = (e.p1.y == Double.POSITIVE_INFINITY) ? MAX_DIM : e.p1.y; // HACK to draw from infinity
                drawLine(e.p1.x, topY, e.p2.x, e.p2.y);
            }
        }
        drawLine(MIN_DIM, sweepLoc, MAX_DIM, sweepLoc);
        show(1);
    }

    public void clear() {
        if (offlineGraphics != null) {
            offlineGraphics.clear();
        } else {
            StdDraw.clear();
        }
    }

    public void show() {
        if (offlineGraphics == null) {
            StdDraw.show();
        }
    }
    public void show(int value) {
        if (offlineGraphics == null) {
            StdDraw.show(value);
        }
    }

    public void fillCircle(Point p, double radius, Color color) {
        if (offlineGraphics != null) {
            offlineGraphics.setColor(color);
            fillCircle(p, radius);
        } else {
            Color old = StdDraw.getPenColor();
            StdDraw.setPenColor(color);
            fillCircle(p, radius);
            StdDraw.setPenColor(old);
        }
    }

    public void fillCircle(Point p, double radius) {
        if (offlineGraphics != null) {
            int x = (int) (width * p.x);
            int y = (int) (height * p.y);
            int rad = (int) (width * radius);
            offlineGraphics.fillCircle(x, y, rad);
        } else {
            StdDraw.setPenRadius(radius);
            StdDraw.point(p.x, p.y);
            StdDraw.setPenRadius();
        }
    }

    public void drawPoint(double x, double y) {
        if (offlineGraphics != null) {
            int xx = (int) (width * x);
            int yy = (int) (height * y);
            offlineGraphics.drawPoint(xx, yy);
        } else {
            StdDraw.point(x, y);
        }
    }

    public void drawBreakPoint(BreakPoint bp) {
        Point p = bp.getPoint();
        setColor(Color.BLUE);
        fillCircle(p,0.01);
        drawLine(bp.edgeBegin.x, bp.edgeBegin.y, p.x, p.y);
        setColor(Color.BLACK);
        if (bp.isEdgeLeft && bp.getEdge().p2 != null) {
            drawLine(bp.edgeBegin.x, bp.edgeBegin.y, bp.getEdge().p2.x, bp.getEdge().p2.y);
        }
        else if (!bp.isEdgeLeft && bp.getEdge().p1 != null) {
            drawLine(bp.edgeBegin.x, bp.edgeBegin.y, bp.getEdge().p1.x, bp.getEdge().p1.y);
        }
    }

    public void drawArc(Arc arc) {
        Point l = arc.getLeft();
        Point r = arc.getRight();

        Parabola par = new Parabola(arc.site, arc.getSweepLoc());
        double min = (l.x == Double.NEGATIVE_INFINITY) ? VoronoiRenderer.MIN_DRAW_DIM : l.x;
        double max = (r.x == Double.POSITIVE_INFINITY) ? VoronoiRenderer.MAX_DRAW_DIM : r.x;
        drawParabola(par, min, max);
        //par.draw(min, max);
    }

    public void drawParabola(Parabola par, double min, double max) {
        min = (min > -2) ? min : -2;
        max = (max < 2) ? max : 2;
        for (double x = min; x < max; x += .001) {
            double y = ((x - par.a) * (x - par.a) + (par.b * par.b) - (par.c * par.c)) / (2 * (par.b - par.c));
            drawPoint(x, y);
        }
    }

    public void setColor(Color color) {
        if (offlineGraphics != null) {
            offlineGraphics.setColor(color);
        }
        else {
            StdDraw.setPenColor(color);
        }
    }

    public void drawLine(double x1, double y1, double x2, double y2) {
        if (offlineGraphics != null) {
            int xx1 = (int) (width * x1);
            int yy1 = (int) (height * y1);
            int xx2 = (int) (width * x2);
            int yy2 = (int) (height * y2);
            offlineGraphics.drawLine(xx1, yy2, xx2, yy2);
        }
        else {
            StdDraw.line(x1, y1, x2, y2);
        }
    }
}
